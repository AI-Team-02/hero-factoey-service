package ai.herofactoryservice.create_game_resource_service.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JsonJdbcType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonJdbcType jsonJdbcType = JsonJdbcType.INSTANCE;

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            if (attribute == null) {
                return "[]";
            }
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("Error converting list to JSON", e);
            return "[]";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error converting JSON to list", e);
            return new ArrayList<>();
        }
    }
}