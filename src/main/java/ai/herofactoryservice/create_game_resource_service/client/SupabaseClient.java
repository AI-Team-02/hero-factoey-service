//package ai.herofactoryservice.create_game_resource_service.client;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import okhttp3.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class SupabaseClient {
//    private final OkHttpClient client;
//    private final String apiUrl;
//    private final String apiKey;
//    private final ObjectMapper objectMapper;
//
//    public SupabaseClient(@Value("${supabase.url}") String apiUrl,
//                          @Value("${supabase.key}") String apiKey,
//                          ObjectMapper objectMapper) {
//        this.client = new OkHttpClient();
//        this.apiUrl = apiUrl;
////        this.apiKey = apiKey;
//        this.objectMapper = objectMapper;
//    }
//
//    public String get(String path) throws IOException {
//        return get(path, null);
//    }
//
//    public String get(String path, Long id) throws IOException {
//        String url = apiUrl + path;
//        if (id != null) {
//            url += "?id=eq." + id;
//        }
//
//        Request request = new Request.Builder()
//                .url(url)
//                .addHeader("apikey", apiKey)
//                .addHeader("Authorization", "Bearer " + apiKey)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            return response.body().string();
//        }
//    }
//
//    public String post(String path, Object body) throws IOException {
//        return post(path, null, body);
//    }
//
//    public String post(String path, Long id, Object body) throws IOException {
//        String url = apiUrl + path;
//        if (id != null) {
//            url += "?id=eq." + id;
//        }
//
//        RequestBody requestBody = RequestBody.create(
//                objectMapper.writeValueAsString(body),
//                MediaType.get("application/json; charset=utf-8")
//        );
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .addHeader("apikey", apiKey)
//                .addHeader("Authorization", "Bearer " + apiKey)
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            return response.body().string();
//        }
//    }
//}