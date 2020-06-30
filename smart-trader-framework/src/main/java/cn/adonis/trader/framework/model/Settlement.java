package cn.adonis.trader.framework.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算
 */
public class Settlement {
    private final BigDecimal profit;
    private final LocalDateTime time;

    public static Settlement of(BigDecimal profit, LocalDateTime time) {
        return new Settlement(profit, time);
    }

    private Settlement(BigDecimal profit, LocalDateTime time) {
        this.profit = profit;
        this.time = time;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
