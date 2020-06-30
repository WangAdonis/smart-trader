package cn.adonis.trader.framework.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class BackTestParameter {

    private final BigDecimal initialFunds; // 初始资金

    private final TradingFee tradingFee; // 手续费

    private final BigDecimal overspendRate; // 超支率

    private final boolean throwExceptionIfSurplusFundsUnqualified; // 剩余资金不合法时是否抛出异常

    private final LocalDateTime startTime;

    private final LocalDateTime endTime;

    public static Builder builder() {
        return new Builder();
    }


    private BackTestParameter(BigDecimal initialFunds, TradingFee tradingFee,
                              boolean throwExceptionIfSurplusFundsUnqualified,
                              BigDecimal overspendRate, LocalDateTime startTime,
                              LocalDateTime endTime) {
        this.initialFunds = initialFunds;
        this.tradingFee = tradingFee;
        this.throwExceptionIfSurplusFundsUnqualified = throwExceptionIfSurplusFundsUnqualified;
        this.overspendRate = overspendRate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public BigDecimal getInitialFunds() {
        return initialFunds;
    }

    public TradingFee getTradingFee() {
        return tradingFee;
    }

    public boolean isThrowExceptionIfSurplusFundsUnqualified() {
        return throwExceptionIfSurplusFundsUnqualified;
    }

    public BigDecimal getOverspendRate() {
        return overspendRate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public static class Builder{
        private String initialFunds;
        private TradingFee tradingFee;
        private String overspendRate;
        private boolean throwExceptionIfSurplusFundsUnqualified;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public Builder setInitialFunds(String initialFunds) {
            this.initialFunds = initialFunds;
            return this;
        }

        public Builder setTradingFee(TradingFee tradingFee) {
            this.tradingFee = tradingFee;
            return this;
        }

        public Builder setOverspendRate(String overspendRate) {
            this.overspendRate = overspendRate;
            return this;
        }

        public Builder setThrowExceptionIfSurplusFundsUnqualified(boolean throwExceptionIfSurplusFundsUnqualified) {
            this.throwExceptionIfSurplusFundsUnqualified = throwExceptionIfSurplusFundsUnqualified;
            return this;
        }

        public Builder setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public BackTestParameter build() {
            return new BackTestParameter(Optional.ofNullable(initialFunds).map(BigDecimal::new).orElse(BigDecimal.ZERO),
                    tradingFee, throwExceptionIfSurplusFundsUnqualified,
                    Optional.ofNullable(overspendRate).map(BigDecimal::new).orElse(BigDecimal.ZERO),
                    startTime, endTime);
        }
    }
}
