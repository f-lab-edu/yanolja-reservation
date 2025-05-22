package com.yanolja.areas.accommodation.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccommodationConcurrencyTest {

    private static final int THREAD_COUNT = 100;

    @Test
    @DisplayName("동시에 여러 스레드에서 리뷰 수 증가 시 정확히 반영되는지 테스트")
    void incrementReviewCount_ConcurrentAccess_ShouldIncrementCorrectly() throws InterruptedException {
        // 준비
        Accommodation accommodation = new Accommodation();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // 실행 - THREAD_COUNT 개의 동시 증가 시뮬레이션
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    accommodation.incrementReviewCount();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 모든 스레드를 동시에 실행
        endLatch.await(); // 모든 스레드가 완료될 때까지 대기
        executorService.shutdown();

        // 검증
        assertEquals(THREAD_COUNT, accommodation.getReviewCount());
    }

    @Test
    @DisplayName("동시에 여러 스레드에서 리뷰 수 감소 시 정확히 반영되는지 테스트")
    void decrementReviewCount_ConcurrentAccess_ShouldDecrementCorrectly() throws InterruptedException {
        // 준비
        Accommodation accommodation = new Accommodation();
        // 초기 리뷰 수를 THREAD_COUNT로 설정
        for (int i = 0; i < THREAD_COUNT; i++) {
            accommodation.incrementReviewCount();
        }
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // 실행 - THREAD_COUNT 개의 동시 감소 시뮬레이션
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    accommodation.decrementReviewCount();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // 검증
        assertEquals(0, accommodation.getReviewCount());
    }

} 