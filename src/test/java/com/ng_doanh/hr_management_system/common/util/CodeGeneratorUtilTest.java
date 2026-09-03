package com.ng_doanh.hr_management_system.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CodeGeneratorUtil Unit Tests")
class CodeGeneratorUtilTest {

    @Test
    @DisplayName("Generate sequential code when no collision exists")
    void generateCode_NoCollision_ReturnsFirstCandidate() {
        String code = CodeGeneratorUtil.generateCode("DEPT-", 5, 0, exists -> false);
        assertThat(code).isEqualTo("DEPT-00001");

        String code2 = CodeGeneratorUtil.generateCode("POS-", 5, 42, exists -> false);
        assertThat(code2).isEqualTo("POS-00043");
    }

    @Test
    @DisplayName("Generate code skipping existing codes on collision")
    void generateCode_WithCollision_SkipsExisting() {
        Set<String> existingCodes = new HashSet<>(Set.of("EMP-00001", "EMP-00002", "EMP-00003"));

        String code = CodeGeneratorUtil.generateCode("EMP-", 5, 0, existingCodes::contains);
        assertThat(code).isEqualTo("EMP-00004");
    }

    @Test
    @DisplayName("Thread safety under concurrent generation with synchronized store")
    void generateCode_ConcurrentExecution_GeneratesUniqueCodes() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> generatedCodes = ConcurrentHashMap.newKeySet();
        Object lock = new Object();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    synchronized (lock) {
                        String code = CodeGeneratorUtil.generateCode("SS-", 5, 0, generatedCodes::contains);
                        generatedCodes.add(code);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(generatedCodes).hasSize(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            String expected = String.format("SS-%05d", i);
            assertThat(generatedCodes).contains(expected);
        }
    }
}
