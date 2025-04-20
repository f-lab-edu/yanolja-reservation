package com.yanolja.common.enumcode;

/**
 * Enum 타입이 데이터베이스와 애플리케이션 간에 변환될 때 사용되는 인터페이스
 */
public interface EntityEnumerable {
    /**
     * 데이터베이스에 저장될 타입 값 반환
     * @return 타입 문자열
     */
    String getType();
    
    /**
     * UI에 표시될 이름 반환
     * @return 표시 이름
     */
    String getName();
} 