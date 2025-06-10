package com.yanolja.areas.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 분산 락 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end";

    /**
     * 락 획득
     * 
     * @param key 락 키
     * @param timeout 타임아웃 (초)
     * @param leaseTime 락 보유 시간 (초)
     * @return 락 토큰 (성공시), null (실패시)
     */
    public String tryLock(String key, long timeout, long leaseTime) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        
        long startTime = System.currentTimeMillis();
        long waitTime = timeout * 1000;
        
        try {
            while (System.currentTimeMillis() - startTime < waitTime) {
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(leaseTime));
                
                if (Boolean.TRUE.equals(success)) {
                    log.debug("락 획득 성공: key={}, value={}", lockKey, lockValue);
                    return lockValue;
                }
                
                // 100ms 대기 후 재시도
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 획득 중 인터럽트 발생: key={}", lockKey, e);
        }
        
        log.debug("락 획득 실패: key={}", lockKey);
        return null;
    }

    /**
     * 락 해제
     * 
     * @param key 락 키
     * @param lockValue 락 토큰
     * @return 해제 성공 여부
     */
    public boolean unlock(String key, String lockValue) {
        if (lockValue == null) {
            return false;
        }
        
        String lockKey = LOCK_PREFIX + key;
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, 
                    Collections.singletonList(lockKey), 
                    lockValue);
            
            boolean success = Long.valueOf(1).equals(result);
            log.debug("락 해제: key={}, success={}", lockKey, success);
            return success;
            
        } catch (Exception e) {
            log.error("락 해제 중 오류 발생: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 락 상태 확인
     */
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * 락 강제 해제 (관리자용)
     */
    public boolean forceUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.delete(lockKey));
    }
} 