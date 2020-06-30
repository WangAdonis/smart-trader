package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.BackTestException;
import cn.adonis.trader.framework.indicator.Average;
import cn.adonis.trader.framework.predictor.DoubleLinearRegression;
import cn.adonis.trader.framework.predictor.TrendPredictor;
import cn.adonis.trader.framework.model.*;
import cn.adonis.trader.framework.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class AverageStrategy extends AbstractFuturesTradingStrategy {

    private final Parameter parameter;
    private int addTimes = 0;

    public static AverageStrategy newAverageStrategy(Parameter parameter) {
        return new AverageStrategy(parameter);
    }

    private AverageStrategy(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    protected Decision makeDecision(Candle candle, TradingContext tradingContext) {
        // 当前持仓类型
        VolumeType holdVolumeType = getVolumeType(tradingContext.getHoldVolumes());
        switch (holdVolumeType) {
            case EMPTY:
                return entryDecision(candle, tradingContext);
            case LONG:
                return longPositionDecision(candle, tradingContext);
            case SHORT:
                return shortPositionDecision(candle, tradingContext);
        }
        return Decision.DO_NOTHING;
    }

    /**
     * 建仓决策
     *
     * @param candle
     * @param tradingContext
     * @return
     */
    private Decision entryDecision(Candle candle, TradingContext tradingContext) {
        Series series = tradingContext.getOriginalData();

        LocalDateTime avgStart = candle.getTime().minusSeconds(parameter.getAvgInterval().toSeconds());
        LocalDateTime avgEnd = candle.getTime().minusSeconds(series.getTimeInterval().toSeconds()); // 均价计算至当前时间点的前一个时间点
        Series historySeries = tradingContext.getOriginalData().find(avgStart, avgEnd);
        // 计算均价
        BigDecimal avgPrice = Average.calculate(historySeries, Candle::getClose);

        LocalDateTime linearRegressionShortStart = candle.getTime().minusSeconds(parameter.getTrendPredictInterval().toSeconds());
        LocalDateTime linearRegressionLongStart = candle.getTime().minusSeconds(parameter.getTrendPredictInterval().toSeconds() * 10);
        LocalDateTime linearRegressionEnd = candle.getTime(); // 趋势预测包含当前时间点
        Series linearRegressionSeriesShort = tradingContext.getOriginalData().find(linearRegressionShortStart, linearRegressionEnd);
        Series linearRegressionSeriesLong = tradingContext.getOriginalData().find(linearRegressionLongStart, linearRegressionEnd);
        // 线性回归，判断增长趋势
        TrendPredictor trendPredictor = DoubleLinearRegression.fit(linearRegressionSeriesShort, linearRegressionSeriesLong);

        // 当前价格高于均价，且呈增长趋势，则多开
        if (BigDecimalUtil.greaterThan(candle.getClose(), avgPrice)
                && trendPredictor.isGoingUp()) {
            return Decision.buy(parameter.getEnterVolumes(), Decision.Reason.ENTRY);
        }

        // 当前价格低于均价，且呈下落趋势，则空开
        if (BigDecimalUtil.lessThan(candle.getClose(), avgPrice)
                && trendPredictor.isGoingDown()) {
            return Decision.sell(parameter.getEnterVolumes(), Decision.Reason.ENTRY);
        }

        return Decision.DO_NOTHING;
    }

    /**
     * 多头策略
     *
     * @param candle
     * @param tradingContext
     * @return
     */
    private Decision longPositionDecision(Candle candle, TradingContext tradingContext) {

        Decision entryDecision = entryDecision(candle, tradingContext);
        if (entryDecision.isSell()) {
            addTimes = 0;
            return Decision.sell(tradingContext.getHoldVolumes(), Decision.Reason.STOP_LOSS);
        }

        Optional<Transaction> lastLong = findLastSpecificTransactionByType(tradingContext, Transaction.Type.BUY);

        if (!lastLong.isPresent()) {
            throw new BackTestException("can not find specific transaction by type");
        }

        // 加仓：价格相比于上一次多开的价格上涨超过addInterval and addTimes小于限制
        if (BigDecimalUtil.greaterThanOrEquals(candle.getClose().subtract(lastLong.get().getPrice()), parameter.getAddInterval())
                && addTimes < parameter.getMaxAddTimes()) {
            addTimes++;
            return Decision.buy(parameter.getEnterVolumes(), Decision.Reason.ADD);
        }

        // 止损：价格相比于上一次多开的价格下跌超过stopLoss
        if (BigDecimalUtil.greaterThanOrEquals(lastLong.get().getPrice().subtract(candle.getClose()), parameter.getStopLoss())) {
            addTimes = 0;
            return Decision.sell(tradingContext.getHoldVolumes(), Decision.Reason.STOP_LOSS);
        }

        // 止盈：自持有头寸起，如果价格相比于期间最高价下跌超过stopProfit
        Candle entryPoint = tradingContext.getEntryPoint();
        Optional<Candle> highestPoint = tradingContext.getOriginalData().findHighestClosedPrice(entryPoint.getTime(), candle.getTime());
        if (!highestPoint.isPresent()) {
            throw new BackTestException("can not find highest point");
        }
        if (BigDecimalUtil.greaterThanOrEquals(highestPoint.get().getClose().subtract(entryPoint.getClose()), parameter.getStopProfit())) {
            addTimes = 0;
            return Decision.sell(tradingContext.getHoldVolumes(), Decision.Reason.STOP_PROFIT);
        }

        return Decision.DO_NOTHING;
    }


    /**
     * 空头策略
     *
     * @param candle
     * @param tradingContext
     * @return
     */
    private Decision shortPositionDecision(Candle candle, TradingContext tradingContext) {

        Decision entryDecision = entryDecision(candle, tradingContext);
        if (entryDecision.isBuy()) {
            addTimes = 0;
            return Decision.buy(tradingContext.getHoldVolumes(), Decision.Reason.STOP_LOSS);
        }

        Optional<Transaction> lastShort = findLastSpecificTransactionByType(tradingContext, Transaction.Type.SELL);

        if (!lastShort.isPresent()) {
            throw new BackTestException("can not find specific transaction by type");
        }

        // 加仓：价格相比于上一次多开的价格下跌超过addInterval and addTimes小于限制
        if (BigDecimalUtil.greaterThanOrEquals(lastShort.get().getPrice().subtract(candle.getClose()), parameter.getAddInterval())
                && addTimes < parameter.getMaxAddTimes()) {
            addTimes++;
            return Decision.sell(parameter.getEnterVolumes(), Decision.Reason.ADD);
        }

        // 止损：价格相比于上一次多开的价格上涨超过stopLoss
        if (BigDecimalUtil.greaterThanOrEquals(candle.getClose().subtract(lastShort.get().getPrice()), parameter.getStopLoss())) {
            addTimes = 0;
            return Decision.buy(tradingContext.getHoldVolumes(), Decision.Reason.STOP_LOSS);
        }

        // 止盈：自持有头寸起，如果价格相比于期间最高价下跌超过stopProfit
        Candle entryPoint = tradingContext.getEntryPoint();
        Optional<Candle> lowestPoint = tradingContext.getOriginalData().findLowestClosedPrice(entryPoint.getTime(), candle.getTime());
        if (!lowestPoint.isPresent()) {
            throw new BackTestException("can not find lowest point");
        }
        if (BigDecimalUtil.greaterThanOrEquals(entryPoint.getClose().subtract(lowestPoint.get().getClose()), parameter.getStopProfit())) {
            addTimes = 0;
            return Decision.buy(tradingContext.getHoldVolumes(), Decision.Reason.STOP_PROFIT);
        }

        return Decision.DO_NOTHING;
    }

    public static class Parameter {

        private final TimeInterval avgInterval;
        private final TimeInterval trendPredictInterval;
        private final BigDecimal enterVolumes; // 建仓手数
        private final BigDecimal addInterval; // 加仓间隔
        private final int maxAddTimes; // 最大加仓次数
        private final BigDecimal stopProfit; // 止盈点
        private final BigDecimal stopLoss; // 止损点

        public static Builder builder() {
            return new Builder();
        }

        private Parameter(TimeInterval avgInterval, TimeInterval trendPredictInterval, BigDecimal enterVolumes, BigDecimal addInterval, int maxAddTimes, BigDecimal stopProfit, BigDecimal stopLoss) {
            this.avgInterval = avgInterval;
            this.trendPredictInterval = trendPredictInterval;
            this.enterVolumes = enterVolumes;
            this.addInterval = addInterval;
            this.maxAddTimes = maxAddTimes;
            this.stopProfit = stopProfit;
            this.stopLoss = stopLoss;
        }

        public TimeInterval getAvgInterval() {
            return avgInterval;
        }

        public TimeInterval getTrendPredictInterval() {
            return trendPredictInterval;
        }

        public BigDecimal getEnterVolumes() {
            return enterVolumes;
        }

        public BigDecimal getAddInterval() {
            return addInterval;
        }

        public int getMaxAddTimes() {
            return maxAddTimes;
        }

        public BigDecimal getStopProfit() {
            return stopProfit;
        }

        public BigDecimal getStopLoss() {
            return stopLoss;
        }

        public static class Builder {
            private TimeInterval avgInterval;
            private TimeInterval trendPredictInterval;
            private String enterVolumes;
            private String addInterval;
            private int maxAddTimes;
            private String stopProfit;
            private String stopLoss;

            public Builder setAvgInterval(TimeInterval avgInterval) {
                this.avgInterval = avgInterval;
                return this;
            }

            public Builder setTrendPredictInterval(TimeInterval trendPredictInterval) {
                this.trendPredictInterval = trendPredictInterval;
                return this;
            }

            public Builder setEnterVolumes(String enterVolumes) {
                this.enterVolumes = enterVolumes;
                return this;
            }

            public Builder setAddInterval(String addInterval) {
                this.addInterval = addInterval;
                return this;
            }

            public Builder setMaxAddTimes(int maxAddTimes) {
                this.maxAddTimes = maxAddTimes;
                return this;
            }

            public Builder setStopProfit(String stopProfit) {
                this.stopProfit = stopProfit;
                return this;
            }

            public Builder setStopLoss(String stopLoss) {
                this.stopLoss = stopLoss;
                return this;
            }

            public Parameter build() {
                return new Parameter(avgInterval, trendPredictInterval,
                        Optional.ofNullable(enterVolumes).map(BigDecimal::new).orElse(BigDecimal.ZERO),
                        Optional.ofNullable(addInterval).map(BigDecimal::new).orElse(BigDecimal.ZERO),
                        maxAddTimes,
                        Optional.ofNullable(stopProfit).map(BigDecimal::new).orElse(BigDecimal.ZERO),
                        Optional.ofNullable(stopLoss).map(BigDecimal::new).orElse(BigDecimal.ZERO));
            }
        }
    }
}
