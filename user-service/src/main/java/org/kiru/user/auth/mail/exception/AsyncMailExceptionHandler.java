package org.kiru.user.auth.mail.exception;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
public class AsyncMailExceptionHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
    log.error("비동기 메서드 '{}' 실행 중 예외 발생. 매개변수: {}", method.getName(), objects, throwable);
  }
}
