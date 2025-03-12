package org.kiru.user.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.kiru.user.auth.mail.exception.AsyncMailExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
@EnableAsync
public class ExecutorConfig implements AsyncConfigurer {
    @Bean
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncMailExceptionHandler();
    }
}