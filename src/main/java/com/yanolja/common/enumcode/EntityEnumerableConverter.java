package com.yanolja.common.enumcode;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Objects;

/**
 * EntityEnumerable 구현체를 데이터베이스 컬럼 값으로 변환하는 컨버터
 * @param <T> EntityEnumerable 구현 Enum 타입
 */
@Converter
public abstract class EntityEnumerableConverter<T extends EntityEnumerable> implements AttributeConverter<T, String> {

    private final Class<T> clazz;

    public EntityEnumerableConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (Objects.isNull(attribute)) {
            return null;
        }
        return attribute.getType();
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return null;
        }
        T[] enumConstants = clazz.getEnumConstants();
        for (T constant : enumConstants) {
            if (StringUtils.equals(constant.getType(), dbData)) {
                return constant;
            }
        }

        throw new UnsupportedOperationException(String.format("\'%s\' 지원하지 않는 enum 형식입니다.", dbData));
    }
} 