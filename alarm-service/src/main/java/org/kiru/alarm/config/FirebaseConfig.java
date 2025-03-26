package org.kiru.alarm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            // 서비스 계정 키 경로
            String absolutePath = new File("alarm-service/src/main/resources/serviceAccountKey.json").getAbsolutePath();
            FileInputStream serviceAccount= new FileInputStream(absolutePath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 이미 초기화된 앱이 없으면 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ FirebaseApp 초기화 완료");
            }

        } catch (IOException e) {
            log.error("❌ FirebaseApp 초기화 실패", e);
        }
    }
}