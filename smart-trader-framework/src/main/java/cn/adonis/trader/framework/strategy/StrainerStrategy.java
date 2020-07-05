package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.BackTestException;
import cn.adonis.trader.framework.indicator.MovingAverage;
import cn.adonis.trader.framework.model.*;
import cn.adonis.trader.framework.predictor.LinearRegression;
import cn.adonis.trader.framework.predictor.TrendPredictor;
import cn.adonis.trader.framework.util.BigDecimalUtil;
import cn.adonis.trader.framework.util.SeriesUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class StrainerStrategy extends AbstractFuturesTradingStrategy implements InitializeStrategy {

    private final Parameter parameter;

    private MovingAverage dailyMa20;
    private MovingAverage fiveMinutesMa60;

    public static StrainerStrategy newStrainerStrategy(StrainerStrategy.Parameter parameter) {
        return new StrainerStrategy(parameter);
    }

    private StrainerStrategy(StrainerStrategy.Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void preFit(Candle candle, TradingContext tradingContext) {
        // 日线ma20
        dailyMa20.add(candle);
        // 5min线ma60
        fiveMinutesMa60.add(candle);
    }

    @Override
    protected Decision entryDecision(Candle candle, TradingContext tradingContext) {

        TrendPredictor longTrendPredictor = LinearRegression.fit(subListForPredictor(dailyMa20));
        TrendPredictor shortTrendPredictor = LinearRegression.fit(subListForPredictor(fiveMinutesMa60));

        // 日线ma20上涨，只做多
        if (longTrendPredictor.isGoingUp()) {
            return Decision.buy(parameter.getEnterVolumes(), Decision.Reason.ENTRY);
        } else if (shortTrendPredictor.isGoingDown()
                && BigDecimalUtil.lessThan(candle.getClose(), fiveMinutesMa60.getDataList().last().getValue())) {
            // 5min ma60下跌，且当前价格跌破当前ma60线时，只做空
            return Decision.sell(parameter.getEnterVolumes(), Decision.Reason.ENTRY);
        }
        return Decision.DO_NOTHING;
    }

    @Override
    protected Decision longPositionDecision(Candle candle, TradingContext tradingContext) {
        Optional<Transaction> lastLong = findLastSpecificTransactionByType(tradingContext, Transaction.Type.BUY);

        if (!lastLong.isPresent()) {
            throw new BackTestException("can not find specific transaction by type");
        }

        // 止损：价格相比于上一次多开的价格下跌超过stopLoss 或 价格跌破5min ma60时
        if (BigDecimalUtil.greaterThanOrEquals(lastLong.get().getPrice().subtract(candle.getClose()), parameter.getStopLoss())
                || BigDecimalUtil.lessThan(candle.getClose(), fiveMinutesMa60.getDataList().last().getValue())) {
            return Decision.sell(tradingContext.getHoldVolumes(), Decision.Reason.STOP_LOSS);
        }

        // 止盈：自持有头寸起，如果价格相比于期间最高价下跌超过stopProfit
        Candle entryPoint = tradingContext.getEntryPoint();
        Optional<Candle> highestPoint = SeriesUtil.findHighestClosedPrice(tradingContext.getOriginalData(), entryPoint.getTime(), candle.getTime());
        if (!highestPoint.isPresent()) {
            throw new BackTestException("can not find highest point");
        }
        if (BigDecimalUtil.greaterThanOrEquals(highestPoint.get().getClose().subtract(entryPoint.getClose()), parameter.getStopProfit())) {
            return Decision.sell(tradingContext.getHoldVolumes(), Decision.Reason.STOP_PROFIT);
        }
        return Decision.DO_NOTHING;
    }

    @Override
    protected Decision shortPositionDecision(Candle candle, TradingContext tradingContext) {

        Optional<Transaction> lastShort = findLastSpecificTransactionByType(tradingContext, Transaction.Type.SELL);

        if (!lastShort.isPresent()) {
            throw new BackTestException("can not find specific transaction by type");
        }

        // 止损：价格相比于上一次多开的价格上涨超过stopLoss 或 价格涨破5min ma60时
        if (BigDecimalUtil.greaterThanOrEquals(candle.getClose().subtract(lastShort.get().getPrice()), parameter.getStopLoss())
                || BigDecimalUtil.greaterThan(candle.getClose(), fiveMinutesMa60.getDataList().last().getValue())) {
            return Decision.buy(tradingContext.getHoldVolumes(), Decision.Reason.STOP_LOSS);
        }

        // 止盈：自持有头寸起，如果价格相比于期间最高价下跌超过stopProfit
        Candle entryPoint = tradingContext.getEntryPoint();
        Optional<Candle> lowestPoint = SeriesUtil.findLowestClosedPrice(tradingContext.getOriginalData(), entryPoint.getTime(), candle.getTime());
        if (!lowestPoint.isPresent()) {
            throw new BackTestException("can not find lowest point");
        }
        if (BigDecimalUtil.greaterThanOrEquals(entryPoint.getClose().subtract(lowestPoint.get().getClose()), parameter.getStopProfit())) {
            return Decision.buy(tradingContext.getHoldVolumes(), Decision.Reason.STOP_PROFIT);
        }

        return Decision.DO_NOTHING;
    }

    private List<TimeDataPoint> subListForPredictor(MovingAverage movingAverage) {
        if (CollectionUtils.size(movingAverage.getDataList()) < parameter.getTrendPredictPreviousCount()) {
            throw new BackTestException("there is no enough point to predict the trend!");
        }
        List<TimeDataPoint> dataList = Lists.newArrayList(movingAverage.getDataList());
        return dataList.subList(dataList.size() - parameter.getTrendPredictPreviousCount(), dataList.size());
    }

    @Override
    public void init(TradingContext tradingContext) {
        this.dailyMa20 = MovingAverage.create(tradingContext.getOriginalData(), MovingAverage.Type.of(20, TimeInterval.ONE_DAY));
        this.fiveMinutesMa60 = MovingAverage.create(tradingContext.getOriginalData(), MovingAverage.Type.of(60, TimeInterval.FIVE_MINUTES));
    }

    public static class Parameter {

        private final int trendPredictPreviousCount;
        private final BigDecimal enterVolumes; // 建仓手数
        private final BigDecimal stopProfit; // 止盈点
        private final BigDecimal stopLoss; // 止损点

        public static StrainerStrategy.Parameter.Builder builder() {
            return new StrainerStrategy.Parameter.Builder();
        }

        private Parameter(int trendPredictPreviousCount, BigDecimal enterVolumes, BigDecimal stopProfit, BigDecimal stopLoss) {
            this.trendPredictPreviousCount = trendPredictPreviousCount;
            this.enterVolumes = enterVolumes;
            this.stopProfit = stopProfit;
            this.stopLoss = stopLoss;
        }

        public int getTrendPredictPreviousCount() {
            return trendPredictPreviousCount;
        }

        public BigDecimal getEnterVolumes() {
            return enterVolumes;
        }

        public BigDecimal getStopProfit() {
            return stopProfit;
        }

        public BigDecimal getStopLoss() {
            return stopLoss;
        }

        public static class Builder {
            private int trendPredictPreviousCount;
            private String enterVolumes;
            private String stopProfit;
            private String stopLoss;

            public Builder setTrendPredictPreviousCount(int trendPredictPreviousCount) {
                this.trendPredictPreviousCount = trendPredictPreviousCount;
                return this;
            }

            public Builder setEnterVolumes(String enterVolumes) {
                this.enterVolumes = enterVolumes;
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

            public StrainerStrategy.Parameter build() {
                return new StrainerStrategy.Parameter(trendPredictPreviousCount, Optional.ofNullable(enterVolumes).map(BigDecimal::new).orElse(BigDecimal.ZERO), Optional.ofNullable(stopProfit).map(BigDecimal::new).orElse(BigDecimal.ZERO), Optional.ofNullable(stopLoss).map(BigDecimal::new).orElse(BigDecimal.ZERO));
            }
        }
    }

}
