package com.kaddy.functional;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class FunctionalUtils {

    public static <T, R> CompletableFuture<R> executeAsync(T input, Function<T, R> operation, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Executing async operation for input: {}", input);
                return operation.apply(input);
            } catch (Exception e) {
                log.error("Error in async operation: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public static <T, R> List<R> processParallel(List<T> items, Function<T, R> processor, int batchSize) {

        return partitionList(items, batchSize).parallelStream().flatMap(batch -> batch.stream().map(processor))
                .collect(Collectors.toList());
    }

    public static <T, R> CompletableFuture<List<R>> processListAsync(List<T> items,
            Function<T, CompletableFuture<R>> asyncProcessor) {

        List<CompletableFuture<R>> futures = items.stream().map(asyncProcessor).collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    public static <T, R> List<R> filterAndTransform(List<T> items, Predicate<T> filter, Function<T, R> transformer) {

        return items.stream().filter(filter).map(transformer).collect(Collectors.toList());
    }

    public static <T, K, R> Map<K, R> groupAndAggregate(List<T> items, Function<T, K> keyExtractor,
            Function<List<T>, R> aggregator) {

        return items.stream().collect(Collectors.groupingBy(keyExtractor)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> aggregator.apply(e.getValue())));
    }

    @SafeVarargs
    public static <T> CompletableFuture<T> pipeline(T initial, Function<T, T>... operations) {

        return CompletableFuture.supplyAsync(() -> {
            T result = initial;
            for (Function<T, T> operation : operations) {
                try {
                    result = operation.apply(result);
                } catch (Exception e) {
                    log.error("Pipeline operation failed: {}", e.getMessage(), e);
                    throw new RuntimeException("Pipeline execution failed", e);
                }
            }
            return result;
        });
    }

    public static <T, R> R retry(T input, Function<T, R> operation, int maxAttempts, long initialDelayMs) {

        int attempt = 0;
        long delay = initialDelayMs;

        while (attempt < maxAttempts) {
            try {
                return operation.apply(input);
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    log.error("Operation failed after {} attempts", maxAttempts, e);
                    throw new RuntimeException("Max retry attempts reached", e);
                }

                try {
                    log.warn("Operation failed, retrying in {}ms (attempt {}/{})", delay, attempt, maxAttempts);
                    Thread.sleep(delay);
                    delay *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw new RuntimeException("Unexpected retry failure");
    }

    public static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    public static <T> CompletableFuture<List<T>> waitAll(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    public static <T, R> Optional<R> executeWithTimeout(T input, Function<T, R> operation, long timeoutMs) {

        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> operation.apply(input));

        try {
            return Optional.of(future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            log.error("Operation timed out or failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @SafeVarargs
    public static <T> Function<T, T> compose(Function<T, T>... functions) {
        return input -> {
            T result = input;
            for (Function<T, T> function : functions) {
                result = function.apply(result);
            }
            return result;
        };
    }
}
