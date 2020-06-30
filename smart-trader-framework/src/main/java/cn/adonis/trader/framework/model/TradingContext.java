package cn.adonis.trader.framework.model;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TradingContext {

    private final Series originalData;

    private final BackTestParameter parameter;

    private BigDecimal surplusFunds; // 剩余资金

    private final List<Transaction> transactions; // 交易

    private BigDecimal holdVolumes;  // 持有数

    private Candle entryPoint;

    public static Builder builder() {
        return new Builder();
    }

    private TradingContext(Series originalData, BackTestParameter parameter) {
        this.originalData = originalData;
        this.parameter = parameter;

        // 初始化过程参数
        this.transactions = new ArrayList<>();
        this.holdVolumes = BigDecimal.ZERO;
        this.surplusFunds = parameter.getInitialFunds();
    }

    public Series getOriginalData() {
        return originalData;
    }

    public BackTestParameter getParameter() {
        return parameter;
    }

    public BigDecimal getSurplusFunds() {
        return surplusFunds;
    }

    public void setSurplusFunds(BigDecimal surplusFunds) {
        this.surplusFunds = surplusFunds;
    }

    public BigDecimal getHoldVolumes() {
        return holdVolumes;
    }

    public void setHoldVolumes(BigDecimal holdVolumes) {
        this.holdVolumes = holdVolumes;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public Candle getEntryPoint() {
        return entryPoint;
    }

    public TradingContext setEntryPoint(Candle entryPoint) {
        this.entryPoint = entryPoint;
        return this;
    }

    public static class Builder {
        private Series originalData;

        private BackTestParameter parameter;

        public TradingContext build() {
            return new TradingContext(this.originalData, this.parameter);
        }

        public Builder setOriginalData(Series originalData) {
            this.originalData = originalData;
            return this;
        }

        public Builder setParameter(BackTestParameter parameter) {
            this.parameter = parameter;
            return this;
        }
    }
}
