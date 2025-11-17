package com.kaddy.service.batch;

import com.kaddy.functional.FunctionalUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic batch processing service with multi-threading support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {

    private final Executor batchExecutor;

    /**
     * Process large dataset in batches with parallel execution
     */
    @Async("batchExecutor")
    public <T, R> CompletableFuture<BatchResult<R>> processBatch(
            List<T> items,
            Function<T, R> processor,
            int batchSize) {

        log.info("Starting batch processing for {} items with batch size {}", items.size(), batchSize);

        return CompletableFuture.supplyAsync(() -> {
            List<R> results = new ArrayList<>();
            List<BatchError> errors = new ArrayList<>();
            AtomicInteger processedCount = new AtomicInteger(0);

            List<List<T>> batches = FunctionalUtils.partitionList(items, batchSize);

            batches.parallelStream().forEach(batch -> {
                try {
                    List<R> batchResults = batch.stream()
                        .map(item -> {
                            try {
                                R result = processor.apply(item);
                                processedCount.incrementAndGet();
                                return result;
                            } catch (Exception e) {
                                errors.add(new BatchError(item.toString(), e.getMessage()));
                                log.error("Error processing item: {}", item, e);
                                return null;
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .toList();

                    synchronized (results) {
                        results.addAll(batchResults);
                    }

                    log.debug("Processed batch of {} items", batch.size());
                } catch (Exception e) {
                    log.error("Error processing batch", e);
                    batch.forEach(item ->
                        errors.add(new BatchError(item.toString(), "Batch processing failed: " + e.getMessage())));
                }
            });

            log.info("Batch processing completed. Processed: {}, Errors: {}",
                processedCount.get(), errors.size());

            return new BatchResult<>(results, errors, processedCount.get(), items.size());
        }, batchExecutor);
    }

    /**
     * Process items with side effects (no return value)
     */
    @Async("batchExecutor")
    public <T> CompletableFuture<BatchSummary> processBatchWithSideEffects(
            List<T> items,
            Consumer<T> processor,
            int batchSize) {

        log.info("Starting batch processing with side effects for {} items", items.size());

        return CompletableFuture.supplyAsync(() -> {
            List<BatchError> errors = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);

            List<List<T>> batches = FunctionalUtils.partitionList(items, batchSize);

            batches.parallelStream().forEach(batch -> {
                batch.forEach(item -> {
                    try {
                        processor.accept(item);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        synchronized (errors) {
                            errors.add(new BatchError(item.toString(), e.getMessage()));
                        }
                        log.error("Error processing item: {}", item, e);
                    }
                });
                log.debug("Processed batch of {} items", batch.size());
            });

            int failureCount = items.size() - successCount.get();

            log.info("Batch processing completed. Success: {}, Failures: {}",
                successCount.get(), failureCount);

            return new BatchSummary(items.size(), successCount.get(), failureCount, errors);
        }, batchExecutor);
    }

    /**
     * Process with retry logic
     */
    @Async("batchExecutor")
    public <T, R> CompletableFuture<BatchResult<R>> processBatchWithRetry(
            List<T> items,
            Function<T, R> processor,
            int batchSize,
            int maxRetries) {

        log.info("Starting batch processing with retry for {} items", items.size());

        return CompletableFuture.supplyAsync(() -> {
            List<R> results = new ArrayList<>();
            List<BatchError> errors = new ArrayList<>();
            AtomicInteger processedCount = new AtomicInteger(0);

            List<List<T>> batches = FunctionalUtils.partitionList(items, batchSize);

            batches.parallelStream().forEach(batch -> {
                batch.forEach(item -> {
                    try {
                        R result = FunctionalUtils.retry(item, processor, maxRetries, 1000);
                        synchronized (results) {
                            results.add(result);
                        }
                        processedCount.incrementAndGet();
                    } catch (Exception e) {
                        synchronized (errors) {
                            errors.add(new BatchError(item.toString(),
                                "Failed after " + maxRetries + " retries: " + e.getMessage()));
                        }
                        log.error("Item failed after retries: {}", item, e);
                    }
                });
            });

            log.info("Batch processing with retry completed. Processed: {}, Errors: {}",
                processedCount.get(), errors.size());

            return new BatchResult<>(results, errors, processedCount.get(), items.size());
        }, batchExecutor);
    }

    /**
     * Parallel batch processing with progress tracking
     */
    @Async("batchExecutor")
    public <T, R> CompletableFuture<BatchResult<R>> processBatchWithProgress(
            List<T> items,
            Function<T, R> processor,
            int batchSize,
            Consumer<ProgressUpdate> progressCallback) {

        log.info("Starting batch processing with progress tracking for {} items", items.size());

        return CompletableFuture.supplyAsync(() -> {
            List<R> results = new ArrayList<>();
            List<BatchError> errors = new ArrayList<>();
            AtomicInteger processedCount = new AtomicInteger(0);

            List<List<T>> batches = FunctionalUtils.partitionList(items, batchSize);
            int totalBatches = batches.size();
            AtomicInteger completedBatches = new AtomicInteger(0);

            batches.parallelStream().forEach(batch -> {
                try {
                    List<R> batchResults = batch.stream()
                        .map(item -> {
                            try {
                                R result = processor.apply(item);
                                int processed = processedCount.incrementAndGet();

                                // Report progress every 10% or every 100 items
                                if (processed % Math.max(items.size() / 10, 100) == 0) {
                                    double progress = (double) processed / items.size() * 100;
                                    progressCallback.accept(new ProgressUpdate(
                                        processed, items.size(), progress, errors.size()));
                                }

                                return result;
                            } catch (Exception e) {
                                synchronized (errors) {
                                    errors.add(new BatchError(item.toString(), e.getMessage()));
                                }
                                return null;
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .toList();

                    synchronized (results) {
                        results.addAll(batchResults);
                    }

                    int completed = completedBatches.incrementAndGet();
                    log.debug("Completed batch {}/{}", completed, totalBatches);

                } catch (Exception e) {
                    log.error("Error processing batch", e);
                }
            });

            // Final progress update
            progressCallback.accept(new ProgressUpdate(
                processedCount.get(), items.size(), 100.0, errors.size()));

            log.info("Batch processing with progress completed");

            return new BatchResult<>(results, errors, processedCount.get(), items.size());
        }, batchExecutor);
    }

    // Result classes

    public record BatchResult<R>(
        List<R> results,
        List<BatchError> errors,
        int successCount,
        int totalCount
    ) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public double successRate() {
            return totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        }
    }

    public record BatchSummary(
        int totalCount,
        int successCount,
        int failureCount,
        List<BatchError> errors
    ) {
        public double successRate() {
            return totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        }
    }

    public record BatchError(
        String itemIdentifier,
        String errorMessage
    ) {}

    public record ProgressUpdate(
        int processedCount,
        int totalCount,
        double progressPercentage,
        int errorCount
    ) {}
}
