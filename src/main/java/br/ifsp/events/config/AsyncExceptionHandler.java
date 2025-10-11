package br.ifsp.events.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import java.util.Arrays;

@Configuration
public class AsyncExceptionHandler implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            logger.error("Exceção não capturada no Async");
            logger.error("Método: {}", method.getName());
            logger.error("Parâmetros: {}", Arrays.toString(params));
            logger.error("Mensagem da Exceção: {}", ex.getMessage());
        };
    }
}