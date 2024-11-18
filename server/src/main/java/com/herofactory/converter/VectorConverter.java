package com.herofactory.create_game_resource_service.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Converter(autoApply = true)
public class VectorConverter implements AttributeConverter<double[], Object> {

    @Override
    public Object convertToDatabaseColumn(double[] attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("vector");
            // Convert double array to PostgreSQL vector format [1,2,3]
            String vectorString = "[" + Arrays.stream(attribute)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(",")) + "]";
            pgObject.setValue(vectorString);
            return pgObject;
        } catch (SQLException e) {
            log.error("벡터 변환 중 오류 발생", e);
            throw new RuntimeException("벡터 변환 실패", e);
        }
    }

    @Override
    public double[] convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            String vectorString = dbData.toString();
            // Remove brackets and split by comma
            String[] values = vectorString.substring(1, vectorString.length() - 1).split(",");
            return Arrays.stream(values)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (Exception e) {
            log.error("데이터베이스 벡터 변환 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 벡터 변환 실패", e);
        }
    }
}