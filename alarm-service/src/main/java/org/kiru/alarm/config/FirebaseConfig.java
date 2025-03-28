package org.kiru.alarm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        // 서비스 계정 키 경로
        Resource resource = new ClassPathResource("serviceAccountKey.json");
        try ( InputStream serviceAccount = resource.getInputStream()){
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 이미 초기화된 앱이 없으면 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp 초기화 완료");
            }

        } catch (IOException e) {
            log.error("FirebaseApp 초기화 실패", e);
        }
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
            
            // JSON 파서의 lenient 모드 활성화
            JsonParser parser = new JsonParser();
            parser.setLenient(true);
            
            var options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();
            
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
