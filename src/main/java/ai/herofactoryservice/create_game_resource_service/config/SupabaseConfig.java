//package ai.herofactoryservice.create_game_resource_service.config;
//
//import okhttp3.OkHttpClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SupabaseConfig {
//
//    @Value("${supabase.url}")
//    private String supabaseUrl;
//
//    @Value("${supabase.key}")
//    private String supabaseKey;
//
//    @Bean
//    public OkHttpClient httpClient() {
//        return new OkHttpClient.Builder()
//                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
//                        .addHeader("apikey", supabaseKey)
//                        .addHeader("Authorization", "Bearer " + supabaseKey)
//                        .build()))
//                .build();
//    }
//
//    public String getSupabaseUrl() {
//        return supabaseUrl;
//    }
//}