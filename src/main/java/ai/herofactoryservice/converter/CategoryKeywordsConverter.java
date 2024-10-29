package ai.herofactoryservice.create_game_resource_service.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Converter(autoApply = true)
public class CategoryKeywordsConverter implements AttributeConverter<List<Map<String, List<String>>>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String, List<String>>> attribute) {
        try {
            if (attribute == null) {
                return null;
            }
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("카테고리 키워드를 JSON으로 변환 중 오류 발생", e);
            throw new RuntimeException("카테고리 키워드 변환 실패", e);
        }
    }

    @Override
    public List<Map<String, List<String>>> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) {
                return null;
            }
            return objectMapper.readValue(dbData,
                    new TypeReference<List<Map<String, List<String>>>>() {});
        } catch (Exception e) {
            log.error("JSON을 카테고리 키워드로 변환 중 오류 발생", e);
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}