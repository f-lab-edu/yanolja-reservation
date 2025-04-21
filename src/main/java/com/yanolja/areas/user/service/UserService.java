package com.yanolja.areas.user.service;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.UserRole;
import com.yanolja.areas.user.dto.LoginRequest;
import com.yanolja.areas.user.dto.LogoutRequest;
import com.yanolja.areas.user.dto.RegisterRequest;
import com.yanolja.areas.user.dto.TokenRefreshRequest;
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

/**
 * 사용자 인증 및 관리를 위한 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

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
    @Transactional
    public User registerUser(RegisterRequest request) {

        // 이메일 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("이미 사용 중인 이메일: {}", request.getEmail());
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 사용자 엔티티 생성
        User user = User.createUser(
            request.getName(),
            request.getEmail(),
            encodedPassword,
            request.getPhone()
        );

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
    @Transactional
    public boolean logout(LogoutRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            // 토큰에서 사용자 이름(이메일) 추출
            String username = jwtTokenProvider.getUsernameFromToken(
                    jwtTokenProvider.resolveToken(refreshToken)
            );
                        
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

    /**
     * 액세스 토큰 갱신
     * @param request 토큰 갱신 요청 정보
     * @return 새로 발급된 토큰 정보
     */
    @Transactional
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            // 리프레시 토큰에서 사용자 이름 추출
            String token = jwtTokenProvider.resolveToken(refreshToken);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                log.warn("유효하지 않은 리프레시 토큰");
                throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            
            // Redis에 저장된 리프레시 토큰과 비교
            String savedRefreshToken = tokenRepository.getRefreshToken(username);
            if (savedRefreshToken == null || !savedRefreshToken.equals(token)) {
                log.warn("저장된 리프레시 토큰과 일치하지 않음: {}", username);
                throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
            }
            
            // 사용자 정보로 인증 객체 생성
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("사용자를 찾을 수 없음: {}", username);
                        return new RuntimeException("사용자를 찾을 수 없습니다.");
                    });
            
            // UserDetailsService를 통해 로드하지 않고 직접 Authentication 객체 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 새로운 액세스 토큰 생성 (리프레시 토큰은 그대로 유지)
            String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
            
            log.info("토큰 갱신 완료: {}", username);
            
            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(token)  // 기존 리프레시 토큰 유지
                    .tokenType("Bearer")
                    .build();
            
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            throw new RuntimeException("토큰 갱신 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 