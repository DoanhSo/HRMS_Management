package com.ng_doanh.hr_management_system.common.util;

import java.util.function.Predicate;

public final class CodeGeneratorUtil {

    private CodeGeneratorUtil() {
        // Private constructor
    }

    public static synchronized String generateCode(
            String prefix,
            int digits,
            long initialCount,
            Predicate<String> existsChecker
    ) {
        long sequence = initialCount + 1;
        while (true) {
            String candidate = String.format("%s%0" + digits + "d", prefix, sequence);
            if (!existsChecker.test(candidate)) {
                return candidate;
            }
            sequence++;
        }
    }
}
