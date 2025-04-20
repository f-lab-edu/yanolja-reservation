package com.yanolja.areas.user.service;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.UserRole;
import com.yanolja.areas.user.dto.LoginRequest;
import com.yanolja.areas.user.dto.LogoutRequest;
import com.yanolja.areas.user.dto.RegisterRequest;
import com.yanolja.areas.user.dto.TokenResponse;
import com.yanolja.areas.user.repository.UserRepository;
import com.yanolja.common.jwt.JwtTokenProvider;
import com.yanolja.common.jwt.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;


    /**
     * 사용자 회원가입 처리
     * @param request 회원가입 요청 정보
     * @return 등록된 사용자
     */
    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        log.debug("회원가입 처리 시작: {}", request.getEmail());

        // 이메일 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("이미 사용 중인 이메일: {}", request.getEmail());
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 엔티티 생성
        User user = new User();
        user.changeName(request.getName());
        user.changeEmail(request.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.changePassword(encodedPassword);
        log.debug("비밀번호 암호화 완료");

        // 전화번호 설정
        user.changePhone(request.getPhone());

        // 기본 역할 설정 (USER)
        user.changeRole(UserRole.USER);

        // 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * 사용자 로그인 처리 및 토큰 발급
     * @param request 로그인 요청 정보
     * @return 발급된 JWT 토큰 정보
     */
    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        
        // 리프레시 토큰을 Redis에 저장
        tokenRepository.saveRefreshToken(authentication.getName(), refreshToken);
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }


    /**
     * 사용자 로그아웃 처리
     * @param request 로그아웃 요청 정보
     * @return 로그아웃 성공 여부
     */
    @Override
    @Transactional
    public boolean logout(LogoutRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            // 토큰에서 사용자 이름(이메일) 추출
            String username = jwtTokenProvider.getUsernameFromToken(
                    jwtTokenProvider.resolveToken(refreshToken)
            );
            
            log.debug("로그아웃 처리 시작: {}", username);
            
            // Redis에서 리프레시 토큰 삭제
            if (tokenRepository.existsRefreshToken(username)) {
                tokenRepository.removeRefreshToken(username);
                log.info("로그아웃 완료: {}", username);
                return true;
            } else {
                log.warn("리프레시 토큰이 존재하지 않음: {}", username);
                return false;
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            return false;
        }
    }
} 