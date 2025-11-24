package com.kaddy.functional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FunctionalInterfaces {

    @FunctionalInterface
    public interface MedicalOperation<T, R> {
        R execute(T input) throws Exception;
    }

    @FunctionalInterface
    public interface AsyncMedicalOperation<T, R> {
        CompletableFuture<R> executeAsync(T input);
    }

    @FunctionalInterface
    public interface ValidationRule<T> {
        boolean validate(T input);

        default ValidationRule<T> and(ValidationRule<T> other) {
            return input -> this.validate(input) && other.validate(input);
        }

        default ValidationRule<T> or(ValidationRule<T> other) {
            return input -> this.validate(input) || other.validate(input);
        }
    }

    @FunctionalInterface
    public interface DataTransformer<S, T> {
        T transform(S source);

        default <R> DataTransformer<S, R> andThen(DataTransformer<T, R> after) {
            return source -> after.transform(this.transform(source));
        }
    }

    @FunctionalInterface
    public interface BatchProcessor<T> {
        void processBatch(List<T> batch);
    }

    @FunctionalInterface
    public interface AlertTrigger<T> {
        boolean shouldTrigger(T data);

        default AlertTrigger<T> and(AlertTrigger<T> other) {
            return data -> this.shouldTrigger(data) && other.shouldTrigger(data);
        }
    }

    @FunctionalInterface
    public interface ResourceHandler<T, R> {
        R handle(T resource) throws Exception;

        default R handleSafely(T resource, R defaultValue) {
            try {
                return handle(resource);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    @FunctionalInterface
    public interface AsyncEventHandler<T> {
        CompletableFuture<Void> handle(T event);
    }
}
