package cn.adonis.trader.framework.model;

import java.math.BigDecimal;

public class FuturesTradingFee implements TradingFee {

    private final BigDecimal multiplier; // 乘数
    private final BigDecimal depositPercentage; // 保证金比例

    public static FuturesTradingFee of(String multiplier, String depositPercentage) {
        return new FuturesTradingFee(new BigDecimal(multiplier), new BigDecimal(depositPercentage));
    }

    private FuturesTradingFee(BigDecimal multiplier, BigDecimal depositPercentage) {
        this.multiplier = multiplier;
        this.depositPercentage = depositPercentage;
    }

    @Override
    public BigDecimal getFee() {
        return multiplier.multiply(depositPercentage);
    }

    @Override
    public BigDecimal getLever() {
        return multiplier;
    }
}
