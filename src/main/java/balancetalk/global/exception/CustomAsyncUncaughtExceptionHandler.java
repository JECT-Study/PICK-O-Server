package balancetalk.global.exception;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
public class CustomAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("exception message - {} {}", ex.getMessage(), ex.getStackTrace());
        log.error("method name - {}", method.getName());
        for (Object param : params) {
            log.error("param value - {}", param);
        }
    }
}
