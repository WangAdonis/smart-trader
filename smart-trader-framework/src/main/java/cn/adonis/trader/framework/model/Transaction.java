package cn.adonis.trader.framework.model;

import cn.adonis.trader.framework.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final BigDecimal volume; // 数额
    private final BigDecimal price; // 单价
    private final LocalDateTime time;// 交易时间
    private final Decision decision;


    public static Builder builder() {
        return new Builder();
    }

    public static Transaction buildTimeKey(LocalDateTime time) {
        return Transaction.builder().setTime(time).build();
    }

    public Transaction(BigDecimal volume, BigDecimal price, LocalDateTime time, Decision decision) {
        this.volume = volume;
        this.price = price;
        this.time = time;
        this.decision = decision;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Decision getDecision() {
        return decision;
    }

    public static class Builder {
        private BigDecimal volume;
        private BigDecimal price;
        private LocalDateTime time;
        private Decision decision;

        public Transaction build() {
            return new Transaction(volume, price, time, decision);
        }

        public Builder setVolume(BigDecimal volume) {
            this.volume = volume;
            return this;
        }

        public Builder setPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder setTime(LocalDateTime time) {
            this.time = time;
            return this;
        }

        public Builder setDecision(Decision decision) {
            this.decision = decision;
            return this;
        }
    }

    public enum Type {
        BUY, SELL, DO_NOTHING;
    }

    public Type getType() {
        BigDecimalUtil.CompareResult compareResult = BigDecimalUtil.compare(volume, BigDecimal.ZERO);
        switch (compareResult) {
            case GREATER: return Type.BUY;
            case LESS: return Type.SELL;
        }
        return Type.DO_NOTHING;
    }
}
