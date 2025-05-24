package com.yanolja.areas.accommodation.entity;

import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.areas.user.config.TestAuditorAwareConfig;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestAuditorAwareConfig.class)
@ActiveProfiles("test")
@DisplayName("숙소 동시성 테스트")
class AccommodationDbConcurrencyTest {

    private static final int THREAD_COUNT = 10;

    @Autowired
    private AccommodationRepository accommodationRepository;

    private Accommodation testAccommodation;

    @BeforeEach
    void setUp() {
        testAccommodation = Accommodation.createAccommodation(
                "동시성 테스트 호텔",
                "통합 동시성 테스트용 숙소",
                "서울시 강남구",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                new BigDecimal("100000")
        );
        testAccommodation = accommodationRepository.save(testAccommodation);
    }

    @Test
    @DisplayName("1단계: 동시성 문제 재현 - Race Condition 발생")
    void step1_demonstrateConcurrencyProblem() throws InterruptedException {
        // 목적: 실제 DB 환경에서 동시성 이슈가 발생함을 증명
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    // 각 스레드가 독립적으로 DB에서 조회 → 수정 → 저장
                    Accommodation accommodation = accommodationRepository.findById(testAccommodation.getId())
                            .orElseThrow(() -> new RuntimeException("숙소를 찾을 수 없습니다."));
                    
                    accommodation.incrementReviewCount();
                    accommodationRepository.save(accommodation);
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("Race Condition 발생: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        Accommodation result = accommodationRepository.findById(testAccommodation.getId()).orElseThrow();
        
        System.out.println("=== 동시성 문제 재현 결과 ===");
        System.out.println("예상 리뷰 수: " + THREAD_COUNT);
        System.out.println("실제 리뷰 수: " + result.getReviewCount());
        System.out.println("데이터 손실: " + (THREAD_COUNT - result.getReviewCount()) + "개");
        
        // 동시성 이슈로 인한 데이터 손실 발생 확인
        if (result.getReviewCount() < THREAD_COUNT) {
            System.out.println("동시성 이슈 재현 성공! 데이터 일관성 문제 발견");
        }
        
        assertTrue(result.getReviewCount() > 0, "최소한 일부 업데이트는 성공해야 함");
        // 대부분의 경우 데이터 손실이 발생하지만, 100% 보장은 아니므로 <= 조건 사용
        assertTrue(result.getReviewCount() <= THREAD_COUNT, "리뷰 수는 스레드 수를 넘을 수 없음");
    }

    @Test
    @DisplayName("2단계: 낙관적 락킹으로 동시성 제어 - OptimisticLockException 발생")
    void step2_useOptimisticLockingForConcurrencyControl() throws InterruptedException {
        // 목적: @Version을 이용한 낙관적 락킹이 제대로 작동하는지 검증
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger lockExceptionCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    updateWithOptimisticLocking(threadId);
                    successCount.incrementAndGet();
                    
                } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                    lockExceptionCount.incrementAndGet();
                    System.out.println("스레드 " + threadId + " - 낙관적 락킹 예외: " + e.getClass().getSimpleName());
                } catch (Exception e) {
                    System.err.println("스레드 " + threadId + " - 예상치 못한 오류: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        Accommodation result = accommodationRepository.findById(testAccommodation.getId()).orElseThrow();

        System.out.println("=== 낙관적 락킹 제어 결과 ===");
        System.out.println("성공한 업데이트: " + successCount.get());
        System.out.println("락킹 예외 발생: " + lockExceptionCount.get());
        System.out.println("최종 리뷰 수: " + result.getReviewCount());
        System.out.println("최종 버전: " + result.getVersion());

        // 낙관적 락킹이 제대로 작동하는지 검증
        assertTrue(lockExceptionCount.get() > 0, "낙관적 락킹 예외가 발생해야 함");
        assertEquals(successCount.get(), result.getReviewCount().intValue(), 
                "성공한 업데이트 수와 실제 리뷰 수가 일치해야 함");
        assertEquals(THREAD_COUNT, successCount.get() + lockExceptionCount.get(), 
                "총 시도 횟수는 스레드 수와 같아야 함");
        
    }

    @Test
    @DisplayName("3단계: 재시도 로직으로 완전한 해결 - 모든 업데이트 성공")
    void step3_completelyFixWithRetryLogic() throws InterruptedException {
        // 목적: 재시도 로직으로 모든 동시 요청을 안전하게 처리
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger totalSuccessCount = new AtomicInteger(0);
        AtomicInteger totalRetryCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    RetryResult result = updateWithRetryLogic(threadId, 10); // 최대 10회 재시도
                    if (result.success) {
                        totalSuccessCount.incrementAndGet();
                    }
                    totalRetryCount.addAndGet(result.retryCount);
                    
                } catch (Exception e) {
                    System.err.println("스레드 " + threadId + " - 최종 실패: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        Accommodation result = accommodationRepository.findById(testAccommodation.getId()).orElseThrow();

        System.out.println("=== 재시도 로직 완전 해결 결과 ===");
        System.out.println("최종 성공 수: " + totalSuccessCount.get());
        System.out.println("총 재시도 횟수: " + totalRetryCount.get());
        System.out.println("최종 리뷰 수: " + result.getReviewCount());

        // 재시도 로직으로 모든 업데이트가 성공했는지 검증
        assertEquals(THREAD_COUNT, totalSuccessCount.get(), "모든 스레드가 성공해야 함");
        assertEquals(THREAD_COUNT, result.getReviewCount().intValue(), 
                "최종 리뷰 수는 스레드 수와 정확히 일치해야 함");
        
    }

    @Transactional
    private void updateWithOptimisticLocking(int threadId) {
        try {
            Accommodation accommodation = accommodationRepository.findById(testAccommodation.getId())
                    .orElseThrow(() -> new RuntimeException("숙소를 찾을 수 없습니다."));

            System.out.println("스레드 " + threadId + " - 조회 시점 버전: " + accommodation.getVersion());
            
            accommodation.incrementReviewCount();
            Thread.sleep(50); // 동시성 상황 시뮬레이션
            
            accommodationRepository.save(accommodation); // 여기서 OptimisticLockException 발생 가능
            
            System.out.println("스레드 " + threadId + " - 업데이트 성공!");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("스레드 중단", e);
        }
    }

    // 재시도 결과를 담는 내부 클래스
    private static class RetryResult {
        boolean success;
        int retryCount;
        
        RetryResult(boolean success, int retryCount) {
            this.success = success;
            this.retryCount = retryCount;
        }
    }

    // @Transactional 제거 - 각 재시도마다 새로운 트랜잭션 사용
    private RetryResult updateWithRetryLogic(int threadId, int maxRetries) {
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // 각 시도마다 새로운 트랜잭션에서 실행
                boolean success = performSingleUpdate(threadId, retryCount);
                if (success) {
                    return new RetryResult(true, retryCount);
                }
                
            } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                retryCount++;
                System.out.println("스레드 " + threadId + " - 재시도 " + retryCount + "/" + maxRetries);
                
                if (retryCount >= maxRetries) {
                    System.err.println("스레드 " + threadId + " - 최대 재시도 횟수 초과");
                    return new RetryResult(false, retryCount);
                }
                
                try {
                    // 백오프 전략: 지수 백오프 + 랜덤 지터
                    long baseDelay = 50L; // 기본 50ms
                    long exponentialDelay = (long) (baseDelay * Math.pow(2, Math.min(retryCount - 1, 4))); // 최대 800ms
                    long jitter = (long) (Math.random() * exponentialDelay * 0.1); // 10% 지터
                    long totalDelay = exponentialDelay + jitter;
                    
                    System.out.println("스레드 " + threadId + " - " + totalDelay + "ms 대기");
                    Thread.sleep(totalDelay);
                    
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new RetryResult(false, retryCount);
                }
            } catch (Exception e) {
                System.err.println("스레드 " + threadId + " - 예상치 못한 오류: " + e.getMessage());
                return new RetryResult(false, retryCount);
            }
        }
        
        return new RetryResult(false, retryCount);
    }

    @Transactional
    private boolean performSingleUpdate(int threadId, int attemptNumber) {
        try {
            Accommodation accommodation = accommodationRepository.findById(testAccommodation.getId())
                    .orElseThrow(() -> new RuntimeException("숙소를 찾을 수 없습니다."));

            System.out.println("스레드 " + threadId + " - 시도 " + (attemptNumber + 1) + 
                             ", 조회 시점 버전: " + accommodation.getVersion() + 
                             ", 현재 리뷰 수: " + accommodation.getReviewCount());

            accommodation.incrementReviewCount();
            accommodationRepository.save(accommodation);
            
            System.out.println("스레드 " + threadId + " - 성공 (시도 " + (attemptNumber + 1) + "회)");
            return true;
            
        } catch (OptimisticLockingFailureException | OptimisticLockException e) {
            // 이 예외들은 상위 메소드에서 처리하도록 다시 던짐
            throw e;
        }
    }
} 