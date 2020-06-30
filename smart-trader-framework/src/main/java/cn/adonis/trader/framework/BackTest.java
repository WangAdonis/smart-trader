package cn.adonis.trader.framework;

import cn.adonis.trader.framework.loader.SeriesLoader;
import cn.adonis.trader.framework.model.*;
import cn.adonis.trader.framework.strategy.TradingStrategy;
import cn.adonis.trader.framework.util.BigDecimalUtil;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;


public class BackTest {

    private final TradingStrategy tradingStrategy;

    private final SeriesLoader seriesLoader;

    private final BackTestParameter parameter;

    public static Builder builder() {
        return new Builder();
    }

    private BackTest(TradingStrategy tradingStrategy, SeriesLoader seriesLoader, BackTestParameter parameter) {
        this.tradingStrategy = tradingStrategy;
        this.seriesLoader = seriesLoader;
        this.parameter = parameter;
    }

    public BackTestResult run() throws Exception {

        // 1. 加载数据
        Series originalData = seriesLoader.load();

        // 2. 创建tradingContext对象
        TradingContext tradingContext = TradingContext.builder()
                .setOriginalData(originalData)
                .setParameter(parameter)
                .build();

        // 3. 运行策略
        originalData.find(parameter.getStartTime(), parameter.getEndTime()).stream()
                .forEach(candle -> tradingStrategy.fit(candle, tradingContext));

        // 4. 结算
        List<Settlement> settlements = settle(tradingContext);

        // 5. 计算总收益
        BigDecimal profit = calculateProfit(settlements);

        // 6. 封装结果
        BackTestResult backTestResult = new BackTestResult();
        backTestResult.setProfit(profit);
        backTestResult.setOriginalData(originalData);
        backTestResult.setTransactions(tradingContext.getTransactions());
        backTestResult.setSettlements(settlements);

        return backTestResult;
    }

    private BigDecimal calculateProfit(List<Settlement> settlements) {
        // 卖出 - 买入
        // 直接累和计数的是(买入 - 卖出)，因此需要取反
        return settlements.stream()
                .map(Settlement::getProfit)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private List<Settlement> settle(TradingContext tradingContext) {
        BigDecimal holdVolume = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;

        BigDecimal lever = Optional.ofNullable(tradingContext.getParameter())
                .map(BackTestParameter::getTradingFee)
                .map(TradingFee::getLever)
                .orElse(BigDecimal.ONE);

        List<Settlement> settlements = Lists.newArrayList();
        for (Transaction transaction : tradingContext.getTransactions()) {
            holdVolume = holdVolume.add(transaction.getVolume());
            profit = profit.add(transaction.getPrice().multiply(transaction.getVolume()));
            // 平仓时计算收益
            if (BigDecimalUtil.equalsZero(holdVolume)) {
                settlements.add(Settlement.of(profit.negate().multiply(lever), transaction.getTime()));
                holdVolume = BigDecimal.ZERO;
                profit = BigDecimal.ZERO;
            }
        }
        return settlements;
    }

    public static class Builder {
        private TradingStrategy tradingStrategy;
        private SeriesLoader seriesLoader;
        private BackTestParameter parameter;

        public BackTest build() {
            return new BackTest(tradingStrategy, seriesLoader, parameter);
        }

        public Builder setTradingStrategy(TradingStrategy tradingStrategy) {
            this.tradingStrategy = tradingStrategy;
            return this;
        }

        public Builder setSeriesLoader(SeriesLoader seriesLoader) {
            this.seriesLoader = seriesLoader;
            return this;
        }

        public Builder setParameter(BackTestParameter parameter) {
            this.parameter = parameter;
            return this;
        }
    }
}
