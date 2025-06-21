package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationDto;
import com.yanolja.areas.accommodation.dto.AccommodationImageDto;
import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.entity.*;
import com.yanolja.areas.accommodation.repository.*;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.OptimisticLockingFailureException;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final AccommodationImageService accommodationImageService;
    private final AmenityService amenityService;
    private final RoomService roomService;
    
    @Value("${app.retry.base-delay:50}")
    private long baseDelay;
    
    @Value("${app.retry.max-delay:2000}")
    private long maxDelay;

    @Transactional
    public AccommodationDto.Response createAccommodation(AccommodationDto.Request request) {
        // 1. 숙소 기본 정보 생성
        Accommodation accommodation = Accommodation.createAccommodation(
            request.getName(),
            request.getDescription(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            request.getPricePerNight()
        );

        accommodation = accommodationRepository.save(accommodation);
        
        return AccommodationDto.Response.fromEntity(accommodation);
    }

    @Transactional(readOnly = true)
    public List<AccommodationDto.ListResponse> getAllAccommodations() {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return accommodations.stream()
            .map(accommodation -> {
                String mainImageUrl = accommodationImageService.getMainImageUrl(accommodation.getId());
                return AccommodationDto.ListResponse.fromEntityWithMainImage(accommodation, mainImageUrl);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationDto.Response getAccommodationById(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        
        // 숙소 이미지 목록 조회
        List<AccommodationImage> images = accommodationImageRepository.findByAccommodationId(id);
        
        // 편의시설 목록 조회
        List<AmenityDto.Response> amenities = amenityService.getAmenitiesByAccommodationId(id);
        
        // 객실 목록 조회
        List<RoomDto.ListResponse> rooms = roomService.getRoomsByAccommodationId(id);
        
        // DTO 변환 및 반환
        AccommodationDto.Response responseDto = AccommodationDto.Response.fromEntity(accommodation);
        
        // 이미지와 편의시설 목록 설정
        responseDto.setImages(
            images.stream()
                .map(AccommodationImageDto.Response::fromEntity)
                .collect(Collectors.toList())
        );
        
        responseDto.setAmenities(amenities);
        
        // 객실 목록 설정
        responseDto.setRooms(rooms);
        
        return responseDto;
    }
    
    @Transactional
    public AccommodationDto.Response updateAccommodation(Long id, AccommodationDto.Request request) {
        Accommodation accommodation = findAccommodationById(id);
        
        accommodation.updateInfo(
            request.getName(),
            request.getDescription(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            request.getPricePerNight()
        );
        
        return AccommodationDto.Response.fromEntity(accommodation);
    }
    
    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        accommodation.markAsDeleted();
    }

    /**
     * 숙소의 리뷰 수를 증가시킵니다.
     * 낙관적 락킹을 통해 동시성을 제어합니다.
     * 
     * @param accommodationId 숙소 ID
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     * @throws OptimisticLockingFailureException 동시성 충돌 시
     */
    @Transactional
    public void incrementReviewCount(Long accommodationId) {
        Accommodation accommodation = findAccommodationById(accommodationId);
        accommodation.incrementReviewCount();
        try {
            accommodationRepository.saveAndFlush(accommodation);
        } catch (OptimisticLockingFailureException e) {
            log.debug("리뷰 수 증가 중 충돌 발생 - accommodationId: {}", accommodationId);
            throw e;
        }
    }

    /**
     * 숙소의 리뷰 수를 증가시킵니다. (재시도 로직 포함)
     * 낙관적 락킹 충돌 시 자동으로 재시도합니다.
     * 
     * @param accommodationId 숙소 ID
     * @param maxRetries 최대 재시도 횟수
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     * @throws RuntimeException 최대 재시도 횟수 초과 시
     */
    public void incrementReviewCountWithRetry(Long accommodationId, int maxRetries) {
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                incrementReviewCount(accommodationId);
                
                if (retryCount > 0) {
                    log.info("리뷰 수 증가 성공 - accommodationId: {}, 재시도 횟수: {}", accommodationId, retryCount);
                }
                return; // 성공 시 바로 리턴
                
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                
                if (retryCount >= maxRetries) {
                    log.error("리뷰 수 증가 최종 실패 - accommodationId: {}, 최대 재시도 횟수: {} 초과", 
                             accommodationId, maxRetries, e);
                    throw new RuntimeException("리뷰 수 증가 실패: 최대 재시도 횟수(" + maxRetries + ")를 초과했습니다.", e);
                }
                
                try {
                    // 백오프 전략: 지수 백오프 + 랜덤 지터
                    long exponentialDelay = (long) (baseDelay * Math.pow(2, Math.min(retryCount - 1, 4)));
                    long jitter = (long) (Math.random() * exponentialDelay * 0.1); // 10% 지터
                    long totalDelay = exponentialDelay + jitter;
                    
                    if (totalDelay > maxDelay) {
                        totalDelay = maxDelay;
                    }
                    
                    log.debug("리뷰 수 증가 재시도 대기 - accommodationId: {}, 재시도: {}/{}, 대기시간: {}ms", 
                             accommodationId, retryCount, maxRetries, totalDelay);
                    
                    Thread.sleep(totalDelay);
                    
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("리뷰 수 증가 중 스레드 중단 - accommodationId: {}", accommodationId, ie);
                    throw new RuntimeException("리뷰 수 증가 중 스레드가 중단되었습니다.", ie);
                }
            }
        }
    }

    /**
     * 숙소의 평점을 업데이트합니다.
     * 낙관적 락킹을 통해 동시성을 제어합니다.
     * 
     * @param accommodationId 숙소 ID
     * @param newRating 새로운 평균 평점
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     * @throws OptimisticLockingFailureException 동시성 충돌 시
     */
    @Transactional
    public void updateRating(Long accommodationId, BigDecimal newRating) {
        Accommodation accommodation = findAccommodationById(accommodationId);
        accommodation.updateRating(newRating);
        accommodationRepository.save(accommodation);
    }

    /**
     * 숙소의 평점을 업데이트합니다. (재시도 로직 포함)
     * 낙관적 락킹 충돌 시 자동으로 재시도합니다.
     * 
     * @param accommodationId 숙소 ID
     * @param newRating 새로운 평균 평점
     * @param maxRetries 최대 재시도 횟수
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     * @throws RuntimeException 최대 재시도 횟수 초과 시
     */
    public void updateRatingWithRetry(Long accommodationId, BigDecimal newRating, int maxRetries) {
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                updateRating(accommodationId, newRating);
                
                if (retryCount > 0) {
                    log.info("평점 업데이트 성공 - accommodationId: {}, 평점: {}, 재시도 횟수: {}", 
                            accommodationId, newRating, retryCount);
                }
                return; // 성공 시 바로 리턴
                
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                
                if (retryCount >= maxRetries) {
                    log.error("평점 업데이트 최종 실패 - accommodationId: {}, 평점: {}, 최대 재시도 횟수: {} 초과", 
                             accommodationId, newRating, maxRetries, e);
                    throw new RuntimeException("평점 업데이트 실패: 최대 재시도 횟수(" + maxRetries + ")를 초과했습니다.", e);
                }
                
                try {
                    // 백오프 전략: 지수 백오프 + 랜덤 지터
                    long exponentialDelay = (long) (baseDelay * Math.pow(2, Math.min(retryCount - 1, 4)));
                    long jitter = (long) (Math.random() * exponentialDelay * 0.1); // 10% 지터
                    long totalDelay = exponentialDelay + jitter;
                    
                    if (totalDelay > maxDelay) {
                        totalDelay = maxDelay;
                    }
                    
                    log.debug("평점 업데이트 재시도 대기 - accommodationId: {}, 평점: {}, 재시도: {}/{}, 대기시간: {}ms", 
                             accommodationId, newRating, retryCount, maxRetries, totalDelay);
                    
                    Thread.sleep(totalDelay);
                    
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("평점 업데이트 중 스레드 중단 - accommodationId: {}", accommodationId, ie);
                    throw new RuntimeException("평점 업데이트 중 스레드가 중단되었습니다.", ie);
                }
            }
        }
    }

    /**
     * 숙소의 리뷰 수를 감소시킵니다.
     * 낙관적 락킹을 통해 동시성을 제어합니다.
     * 
     * @param accommodationId 숙소 ID
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     * @throws OptimisticLockingFailureException 동시성 충돌 시
     */
    @Transactional
    public void decrementReviewCount(Long accommodationId) {
        Accommodation accommodation = findAccommodationById(accommodationId);
        accommodation.decrementReviewCount();
        try {
            accommodationRepository.saveAndFlush(accommodation);
        } catch (OptimisticLockingFailureException e) {
            log.debug("리뷰 수 감소 중 충돌 발생 - accommodationId: {}", accommodationId);
            throw e;
        }
    }

    /**
     * 숙소의 리뷰 수를 감소시킵니다. (재시도 로직 포함)
     * 낙관적 락킹 충돌 시 자동으로 재시도합니다.
     * 
     * @param accommodationId 숙소 ID
     * @param maxRetries 최대 재시도 횟수
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     * @throws RuntimeException 최대 재시도 횟수 초과 시
     */
    public void decrementReviewCountWithRetry(Long accommodationId, int maxRetries) {
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                decrementReviewCount(accommodationId);
                
                if (retryCount > 0) {
                    log.info("리뷰 수 감소 성공 - accommodationId: {}, 재시도 횟수: {}", 
                            accommodationId, retryCount);
                }
                return; // 성공 시 바로 리턴
                
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                
                if (retryCount >= maxRetries) {
                    log.error("리뷰 수 감소 최종 실패 - accommodationId: {}, 최대 재시도 횟수: {} 초과", 
                             accommodationId, maxRetries, e);
                    throw new RuntimeException("리뷰 수 감소 실패: 최대 재시도 횟수(" + maxRetries + ")를 초과했습니다.", e);
                }
                
                try {
                    // 백오프 전략: 지수 백오프 + 랜덤 지터
                    long exponentialDelay = (long) (baseDelay * Math.pow(2, Math.min(retryCount - 1, 4)));
                    long jitter = (long) (Math.random() * exponentialDelay * 0.1); // 10% 지터
                    long totalDelay = exponentialDelay + jitter;
                    
                    if (totalDelay > maxDelay) {
                        totalDelay = maxDelay;
                    }
                    
                    log.debug("리뷰 수 감소 재시도 대기 - accommodationId: {}, 재시도: {}/{}, 대기시간: {}ms", 
                             accommodationId, retryCount, maxRetries, totalDelay);
                    
                    Thread.sleep(totalDelay);
                    
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("리뷰 수 감소 중 스레드 중단 - accommodationId: {}", accommodationId, ie);
                    throw new RuntimeException("리뷰 수 감소 중 스레드가 중단되었습니다.", ie);
                }
            }
        }
    }
    
    private Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
    }
} 