package ai.herofactoryservice.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> error(String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}