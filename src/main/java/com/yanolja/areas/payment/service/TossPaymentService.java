package com.yanolja.areas.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {

    private final RestTemplate restTemplate;
    
    @Value("${payment.pg.toss.api-url}")
    private String tossApiUrl;
    
    @Value("${payment.pg.toss.secret-key}")
    private String tossSecretKey;

    /**
     * 토스 페이먼츠 결제 승인
     */
    @Transactional
    public Map<String, Object> approvePayment(String paymentKey, String orderId, Long amount) {
        log.info("토스 페이먼츠 결제 승인 시작 - paymentKey: {}, orderId: {}, amount: {}", 
                paymentKey, orderId, amount);

        try {
            // 1. 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + 
                    Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8)));

            // 2. 요청 바디 설정
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount);
            requestBody.put("paymentKey", paymentKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. 토스 페이먼츠 API 호출
            String approveUrl = tossApiUrl + "/v1/payments/confirm";
            ResponseEntity<Map> response = restTemplate.postForEntity(approveUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("토스 페이먼츠 결제 승인 성공 - response: {}", responseBody);
                return responseBody;
            } else {
                log.error("토스 페이먼츠 결제 승인 실패 - status: {}", response.getStatusCode());
                throw new RuntimeException("토스 페이먼츠 결제 승인에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 승인 중 오류 발생", e);
            throw new RuntimeException("결제 승인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 토스 페이먼츠 결제 취소
     */
    @Transactional
    public Map<String, Object> cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        log.info("토스 페이먼츠 결제 취소 시작 - paymentKey: {}, reason: {}, amount: {}", 
                paymentKey, cancelReason, cancelAmount);

        try {
            // 1. 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + 
                    Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8)));

            // 2. 요청 바디 설정
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);
            if (cancelAmount != null) {
                requestBody.put("cancelAmount", cancelAmount);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. 토스 페이먼츠 API 호출
            String cancelUrl = tossApiUrl + "/v1/payments/" + paymentKey + "/cancel";
            ResponseEntity<Map> response = restTemplate.postForEntity(cancelUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("토스 페이먼츠 결제 취소 성공 - response: {}", responseBody);
                return responseBody;
            } else {
                log.error("토스 페이먼츠 결제 취소 실패 - status: {}", response.getStatusCode());
                throw new RuntimeException("토스 페이먼츠 결제 취소에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 취소 중 오류 발생", e);
            throw new RuntimeException("결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 토스 페이먼츠 결제 조회
     */
    public Map<String, Object> getPayment(String paymentKey) {
        log.info("토스 페이먼츠 결제 조회 - paymentKey: {}", paymentKey);

        try {
            // 1. 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + 
                    Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8)));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 2. 토스 페이먼츠 API 호출
            String getUrl = tossApiUrl + "/v1/payments/" + paymentKey;
            ResponseEntity<Map> response = restTemplate.exchange(getUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("토스 페이먼츠 결제 조회 성공 - response: {}", responseBody);
                return responseBody;
            } else {
                log.error("토스 페이먼츠 결제 조회 실패 - status: {}", response.getStatusCode());
                throw new RuntimeException("토스 페이먼츠 결제 조회에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 조회 중 오류 발생", e);
            throw new RuntimeException("결제 조회 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 