package com.kaddy.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Slf4j
public class PerformanceMonitor {

    private final Map<String, MethodMetrics> metricsMap = new ConcurrentHashMap<>();

    @Around("@annotation(org.springframework.scheduling.annotation.Async)")
    public Object monitorAsyncMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();
            recordSuccess(methodName, start);
            return result;
        } catch (Throwable e) {
            recordFailure(methodName, start, e);
            throw e;
        }
    }

    @Around("execution(* com.kaddy.service.async..*(..))")
    public Object monitorAsyncService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();
            recordSuccess(methodName, start);
            return result;
        } catch (Throwable e) {
            recordFailure(methodName, start, e);
            throw e;
        }
    }

    private void recordSuccess(String methodName, Instant start) {
        Duration duration = Duration.between(start, Instant.now());
        MethodMetrics metrics = metricsMap.computeIfAbsent(methodName, k -> new MethodMetrics());

        metrics.incrementSuccess();
        metrics.recordDuration(duration.toMillis());

        log.debug("Method {} completed in {}ms", methodName, duration.toMillis());
    }

    private void recordFailure(String methodName, Instant start, Throwable error) {
        Duration duration = Duration.between(start, Instant.now());
        MethodMetrics metrics = metricsMap.computeIfAbsent(methodName, k -> new MethodMetrics());

        metrics.incrementFailure();
        metrics.recordDuration(duration.toMillis());

        log.error("Method {} failed after {}ms: {}", methodName, duration.toMillis(), error.getMessage());
    }

    public Map<String, MethodMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }

    public MethodMetrics getMetrics(String methodName) {
        return metricsMap.get(methodName);
    }

    public void resetMetrics() {
        metricsMap.clear();
        log.info("All metrics reset");
    }

    public static class MethodMetrics {
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong(0);

        public void incrementSuccess() {
            successCount.incrementAndGet();
        }

        public void incrementFailure() {
            failureCount.incrementAndGet();
        }

        public void recordDuration(long durationMs) {
            totalDuration.addAndGet(durationMs);
            minDuration.updateAndGet(current -> Math.min(current, durationMs));
            maxDuration.updateAndGet(current -> Math.max(current, durationMs));
        }

        public long getSuccessCount() {
            return successCount.get();
        }

        public long getFailureCount() {
            return failureCount.get();
        }

        public long getTotalCalls() {
            return successCount.get() + failureCount.get();
        }

        public double getSuccessRate() {
            long total = getTotalCalls();
            return total > 0 ? (double) successCount.get() / total * 100 : 0;
        }

        public double getAverageDuration() {
            long total = getTotalCalls();
            return total > 0 ? (double) totalDuration.get() / total : 0;
        }

        public long getMinDuration() {
            return minDuration.get() == Long.MAX_VALUE ? 0 : minDuration.get();
        }

        public long getMaxDuration() {
            return maxDuration.get();
        }

        @Override
        public String toString() {
            return String.format(
                    "Metrics{calls=%d, success=%d, failure=%d, successRate=%.2f%%, avgDuration=%.2fms, min=%dms, max=%dms}",
                    getTotalCalls(), getSuccessCount(), getFailureCount(), getSuccessRate(), getAverageDuration(),
                    getMinDuration(), getMaxDuration());
        }
    }
}
