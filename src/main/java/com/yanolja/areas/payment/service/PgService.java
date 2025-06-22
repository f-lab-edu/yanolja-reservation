package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.PaymentDto;
import com.yanolja.areas.payment.entity.Payment;
import com.yanolja.areas.payment.entity.PaymentMethod;
import com.yanolja.config.PaymentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PG사 연동 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgService {

    private final PaymentConfig paymentConfig;
    private final RestTemplate restTemplate;

    /**
     * 결제 요청 (토스페이먼츠)
     */
    public PgResult requestTossPayment(Payment payment, PaymentDto.Request request) {
        try {
            String url = paymentConfig.getPg().getToss().getApiUrl() + "/v1/payments";
            
            // 결제 요청 데이터 구성
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", payment.getAmount().intValue());
            paymentData.put("orderId", payment.getOrder().getOrderNumber());
            paymentData.put("orderName", "야놀자 숙소 예약");
            paymentData.put("successUrl", paymentConfig.getPg().getToss().getSuccessUrl());
            paymentData.put("failUrl", paymentConfig.getPg().getToss().getFailUrl());
            paymentData.put("customerEmail", "customer@example.com");
            paymentData.put("customerName", "고객명");
            
            if (request.getCardInfo() != null) {
                Map<String, Object> cardInfo = new HashMap<>();
                cardInfo.put("number", request.getCardInfo().getCardNumber());
                cardInfo.put("installmentMonths", request.getCardInfo().getInstallmentMonths());
                paymentData.put("card", cardInfo);
            }
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                    (paymentConfig.getPg().getToss().getSecretKey() + ":").getBytes()));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentData, headers);
            
            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return PgResult.builder()
                        .success(true)
                        .pgTransactionId((String) responseBody.get("transactionKey"))
                        .approvalNumber((String) responseBody.get("approvalNumber"))
                        .receiptUrl((String) responseBody.get("receipt_url"))
                        .build();
            } else {
                return PgResult.builder()
                        .success(false)
                        .failureReason("토스페이먼츠 API 호출 실패: " + response.getStatusCode())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 요청 실패", e);
            return PgResult.builder()
                    .success(false)
                    .failureReason("토스페이먼츠 연동 오류: " + e.getMessage())
                    .build();
        }
    }



    /**
     * 결제 취소 요청 (토스페이먼츠)
     */
    public boolean cancelTossPayment(Payment payment, String reason) {
        try {
            // paymentKey를 사용 (pgTransactionId 대신)
            String paymentKeyToUse = payment.getPaymentKey();
            if (paymentKeyToUse == null || paymentKeyToUse.isEmpty()) {
                log.error("PaymentKey가 없어서 취소할 수 없습니다 - payment.id: {}", payment.getId());
                return false;
            }
            
            String url = paymentConfig.getPg().getToss().getApiUrl() + 
                        "/v1/payments/" + paymentKeyToUse + "/cancel";
            
            Map<String, Object> cancelData = new HashMap<>();
            cancelData.put("cancelReason", reason);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                    (paymentConfig.getPg().getToss().getSecretKey() + ":").getBytes()));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancelData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패", e);
            return false;
        }
    }

    /**
     * 부분 환불 요청 (토스페이먼츠)
     */
    public boolean refundTossPayment(Payment payment, BigDecimal refundAmount, String reason) {
        try {
            // paymentKey를 사용 (pgTransactionId 대신)
            String paymentKeyToUse = payment.getPaymentKey();
            if (paymentKeyToUse == null || paymentKeyToUse.isEmpty()) {
                log.error("PaymentKey가 없어서 환불할 수 없습니다 - payment.id: {}", payment.getId());
                return false;
            }
            
            String url = paymentConfig.getPg().getToss().getApiUrl() + 
                        "/v1/payments/" + paymentKeyToUse + "/cancel";
            
            Map<String, Object> refundData = new HashMap<>();
            refundData.put("cancelAmount", refundAmount.intValue());
            refundData.put("cancelReason", reason);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                    (paymentConfig.getPg().getToss().getSecretKey() + ":").getBytes()));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(refundData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            log.error("토스페이먼츠 부분 환불 실패", e);
            return false;
        }
    }

    /**
     * 웹훅 서명 검증
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (!paymentConfig.getWebhook().isVerifySignature()) {
            return true;
        }
        
        try {
            String secretKey = paymentConfig.getWebhook().getSecretKey();
            String expectedSignature = generateHmacSha256(payload, secretKey);
            return expectedSignature.equals(signature);
            
        } catch (Exception e) {
            log.error("웹훅 서명 검증 실패", e);
            return false;
        }
    }

    /**
     * HMAC-SHA256 서명 생성
     */
    private String generateHmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 결제 방법에 따른 PG사 결제 요청
     */
    public PgResult requestPayment(Payment payment, PaymentDto.Request request) {
        PaymentMethod paymentMethod = request.getPaymentMethod();
        
        switch (paymentMethod) {
            case CARD:
            case BANK_TRANSFER:
            case VIRTUAL_ACCOUNT:
            case TOSS:
                return requestTossPayment(payment, request);
            default:
                return PgResult.builder()
                        .success(false)
                        .failureReason("지원되지 않는 결제 방법입니다: " + paymentMethod)
                        .build();
        }
    }

    /**
     * PG사 결제 결과 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class PgResult {
        private boolean success;
        private String pgTransactionId;
        private String approvalNumber;
        private String receiptUrl;
        private String paymentUrl;  // 리다이렉션 URL (카카오페이 등)
        private String failureReason;
    }
} 