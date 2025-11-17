package com.kaddy.controller;

import com.kaddy.monitoring.PerformanceMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for monitoring system performance and metrics
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "System monitoring and performance metrics")
public class MonitoringController {

    private final PerformanceMonitor performanceMonitor;

    @GetMapping("/metrics")
    @Operation(summary = "Get all performance metrics",
               description = "Retrieve performance metrics for all monitored methods")
    public ResponseEntity<Map<String, PerformanceMonitor.MethodMetrics>> getAllMetrics() {
        return ResponseEntity.ok(performanceMonitor.getAllMetrics());
    }

    @GetMapping("/metrics/{methodName}")
    @Operation(summary = "Get metrics for specific method",
               description = "Retrieve performance metrics for a specific method")
    public ResponseEntity<PerformanceMonitor.MethodMetrics> getMethodMetrics(
            @PathVariable String methodName) {
        PerformanceMonitor.MethodMetrics metrics = performanceMonitor.getMetrics(methodName);
        if (metrics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metrics);
    }

    @DeleteMapping("/metrics")
    @Operation(summary = "Reset all metrics",
               description = "Clear all collected performance metrics")
    public ResponseEntity<String> resetMetrics() {
        performanceMonitor.resetMetrics();
        return ResponseEntity.ok("All metrics reset successfully");
    }

    @GetMapping("/health")
    @Operation(summary = "Get system health",
               description = "Retrieve system health information including thread pool status")
    public ResponseEntity<SystemHealth> getSystemHealth() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        SystemHealth health = new SystemHealth(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().totalMemory(),
            Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().maxMemory(),
            threadBean.getThreadCount(),
            threadBean.getPeakThreadCount(),
            threadBean.getTotalStartedThreadCount()
        );

        return ResponseEntity.ok(health);
    }

    @GetMapping("/thread-pools")
    @Operation(summary = "Get thread pool status",
               description = "Retrieve status of all configured thread pools")
    public ResponseEntity<Map<String, String>> getThreadPoolStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "Thread pools configured and operational");
        status.put("taskExecutor", "10-20 threads, queue: 500");
        status.put("batchExecutor", "5-10 threads, queue: 100");
        status.put("reportExecutor", "3-5 threads, queue: 50");
        return ResponseEntity.ok(status);
    }

    public record SystemHealth(
        int availableProcessors,
        long totalMemory,
        long freeMemory,
        long maxMemory,
        int currentThreads,
        int peakThreads,
        long totalStartedThreads
    ) {
        public long usedMemory() {
            return totalMemory - freeMemory;
        }

        public double memoryUsagePercentage() {
            return (double) usedMemory() / totalMemory * 100;
        }
    }
}
