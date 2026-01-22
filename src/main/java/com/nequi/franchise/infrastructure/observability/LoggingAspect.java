package com.nequi.franchise.infrastructure.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.nequi.franchise.application.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String correlationId = MDC.get("correlationId");
        String traceId = MDC.get("traceId");

        logger.info("[{}] [{}] Executing {}.{} with args: {}", 
            correlationId, traceId, className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            if (result instanceof Mono) {
                return ((Mono<?>) result)
                    .doOnSuccess(value -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.info("[{}] [{}] Completed {}.{} in {}ms", 
                            correlationId, traceId, className, methodName, duration);
                    })
                    .doOnError(error -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.error("[{}] [{}] Failed {}.{} after {}ms. Error: {}", 
                            correlationId, traceId, className, methodName, duration, error.getMessage());
                    });
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("[{}] [{}] Completed {}.{} in {}ms", 
                correlationId, traceId, className, methodName, duration);
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] [{}] Failed {}.{} after {}ms. Error: {}", 
                correlationId, traceId, className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.nequi.franchise.infrastructure.adapter.input.rest.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String correlationId = MDC.get("correlationId");
        String traceId = MDC.get("traceId");

        logger.debug("[{}] [{}] REST endpoint called: {}.{}", 
            correlationId, traceId, className, methodName);

        return joinPoint.proceed();
    }
}
