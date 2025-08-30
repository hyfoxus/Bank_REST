package com.gnemirko.bank_rest.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Aspect
@Configuration
public class ServiceLoggingAspect {
    @Around("execution(public * com.gnemirko.bank_rest.service..*(..))")
    public Object aroundService(ProceedingJoinPoint pjp) throws Throwable {
        Logger targetLog = LoggerFactory.getLogger(pjp.getTarget().getClass());
        String sig = pjp.getSignature().toShortString();
        long t0 = System.nanoTime();

        if (targetLog.isDebugEnabled()) {
            targetLog.debug("→ {}", sig); // вход
        }

        try {
            Object result = pjp.proceed();
            if (targetLog.isDebugEnabled()) {
                long ms = (System.nanoTime() - t0) / 1_000_000;
                targetLog.debug("← {} ({} ms)", sig, ms); // выход
            }
            return result;
        } catch (Throwable ex) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            targetLog.warn("✖ {} failed after {} ms: {}", sig, ms, ex.toString());
            throw ex;
        }
    }
}