package com.findmyflight.findmyflight.utils;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class TransactionHelper {
    private final EntityManager entityManager;

    @Transactional
    public void runInTransaction(Consumer<EntityManager> consumer) {
        consumer.accept(entityManager);
    }

    @Transactional
    public <T> T runInTransactionAndReturn(Function<EntityManager, T> consumer) {
        return consumer.apply(entityManager);
    }
}
