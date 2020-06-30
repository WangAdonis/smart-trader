package cn.adonis.trader.framework.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    public static CompareResult compare(BigDecimal b1, BigDecimal b2) {
        int compare = b1.compareTo(b2);
        if (compare == 0) {
            return CompareResult.EQUALS;
        }
        return compare > 0 ? CompareResult.GREATER : CompareResult.LESS;
    }

    public static boolean greaterThan(BigDecimal b1, BigDecimal b2) {
        return compare(b1, b2) == CompareResult.GREATER;
    }

    public static boolean greaterThanOrEquals(BigDecimal b1, BigDecimal b2) {
        CompareResult compareResult = compare(b1, b2);
        return compareResult == CompareResult.GREATER || compareResult == CompareResult.EQUALS;
    }

    public static boolean greaterThanZero(BigDecimal b) {
        return greaterThan(b, BigDecimal.ZERO);
    }

    public static boolean greaterThanOrEqualsZero(BigDecimal b) {
        return greaterThanOrEquals(b, BigDecimal.ZERO);
    }

    public static boolean lessThan(BigDecimal b1, BigDecimal b2) {
        return compare(b1, b2) == CompareResult.LESS;
    }

    public static boolean lessThanOrEquals(BigDecimal b1, BigDecimal b2) {
        CompareResult compareResult = compare(b1, b2);
        return compareResult == CompareResult.LESS || compareResult == CompareResult.EQUALS;
    }

    public static boolean lessThanZero(BigDecimal b) {
        return lessThan(b, BigDecimal.ZERO);
    }

    public static boolean lessThanOrEqualsZero(BigDecimal b) {
        return lessThanOrEquals(b, BigDecimal.ZERO);
    }

    public static boolean equals(BigDecimal b1, BigDecimal b2) {
        return b1.equals(b2);
    }

    public static boolean equalsZero(BigDecimal b) {
        return equals(b, BigDecimal.ZERO);
    }

    public enum CompareResult {
        EQUALS, GREATER, LESS;
    }
}
