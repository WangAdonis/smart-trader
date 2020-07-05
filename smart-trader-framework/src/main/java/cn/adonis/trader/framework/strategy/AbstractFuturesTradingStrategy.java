package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.BackTestException;
import cn.adonis.trader.framework.model.*;
import cn.adonis.trader.framework.util.BigDecimalUtil;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

/**
 * 期货交易
 */
public abstract class AbstractFuturesTradingStrategy implements TradingStrategy {

    @Override
    public void fit(Candle candle, TradingContext tradingContext) {

        // 预处理
        preFit(candle, tradingContext);

        // 决策
        Decision decision = makeDecision(candle, tradingContext);

        if (decision.doNoting()) {
            return;
        }

        // 更新TradingContext
        boolean result = updateTradingContext(candle, decision, tradingContext);

        if (!result) {
            if (tradingContext.getParameter().isThrowExceptionIfSurplusFundsUnqualified()) {
                throw new BackTestException("surplus funds is not enough!");
            }
            return;
        }

        // 生成交易信息
        Transaction transaction = Transaction.builder()
                .setPrice(candle.getClose())
                .setTime(candle.getTime())
                .setVolume(decision.getVolume())
                .setDecision(decision)
                .build();

        // 更新建仓点
        if (decision.getReason() == Decision.Reason.ENTRY) {
            tradingContext.setEntryPoint(candle);
        }

        // 添加当前交易信息
        tradingContext.addTransaction(transaction);
    }

    /**
     * 计算持仓数
     * @param decision
     * @param oldHoldVolumes
     * @return
     */
    private BigDecimal calculateHoldVolumes(Decision decision, BigDecimal oldHoldVolumes) {
        return Optional.ofNullable(oldHoldVolumes).orElse(BigDecimal.ZERO).add(decision.getVolume());
    }

    /**
     * 计算持仓数、金额并更新context
     * @param candle
     * @param decision
     * @param tradingContext
     * @return
     */
    private boolean updateTradingContext(Candle candle, Decision decision, TradingContext tradingContext) {

        // 计算新的持仓数
        final BigDecimal oldHoldVolumes = tradingContext.getHoldVolumes();
        final BigDecimal newHoldVolumes = calculateHoldVolumes(decision, oldHoldVolumes);

        final VolumeType newVolumeType = getVolumeType(newHoldVolumes);
        final VolumeType oldVolumeType = getVolumeType(tradingContext.getHoldVolumes());

        // 加仓、平仓数
        BigDecimal addVolume = BigDecimal.ZERO;
        BigDecimal reduceVolume = BigDecimal.ZERO;

        // 计算加仓、平仓数
        if (newVolumeType == oldVolumeType) {
            // 持仓类型相同时
            BigDecimalUtil.CompareResult compareResult = BigDecimalUtil.compare(newHoldVolumes.abs(), oldHoldVolumes.abs());
            if (compareResult == BigDecimalUtil.CompareResult.GREATER) {
                // 加仓
                addVolume = newHoldVolumes.abs().subtract(oldHoldVolumes.abs());
            } else if (compareResult == BigDecimalUtil.CompareResult.LESS) {
                // 减仓
                reduceVolume = oldHoldVolumes.abs().subtract(newHoldVolumes.abs());
            }
        } else {
            addVolume = newHoldVolumes.abs();
            reduceVolume = oldHoldVolumes.abs();
        }

        // 加仓数大于0，计算保证金
        BigDecimal deposit = BigDecimal.ZERO;
        if (BigDecimalUtil.greaterThanZero(addVolume)) {
            deposit = addVolume.multiply(candle.getClose()).multiply(tradingContext.getParameter().getTradingFee().getFee());
        }

        // 减仓数大于0，计算
        BigDecimal income = BigDecimal.ZERO;
        if (BigDecimalUtil.greaterThanZero(reduceVolume)) {
            income = reduceVolume.multiply(candle.getClose()).multiply(tradingContext.getParameter().getTradingFee().getFee());
        }

        // 计算剩余金额
        BigDecimal surplusFunds = tradingContext.getSurplusFunds().subtract(deposit).add(income);

        if (BigDecimalUtil.lessThanZero(surplusFunds)) {
            BigDecimal overspendRate = Optional.ofNullable(tradingContext.getParameter().getOverspendRate()).orElse(BigDecimal.ZERO);
            BigDecimal initFunds = tradingContext.getParameter().getInitialFunds();
            // 超支过高
            if (BigDecimalUtil.greaterThan(surplusFunds.abs(), initFunds.multiply(overspendRate))) {
                return false;
            }
        }

        tradingContext.setHoldVolumes(newHoldVolumes);
        tradingContext.setSurplusFunds(surplusFunds);
        return true;
    }

    protected VolumeType getVolumeType(BigDecimal holdVolume) {
        BigDecimalUtil.CompareResult compare = BigDecimalUtil.compare(holdVolume, BigDecimal.ZERO);
        if (compare == BigDecimalUtil.CompareResult.EQUALS) {
            return VolumeType.EMPTY;
        }
        return compare == BigDecimalUtil.CompareResult.GREATER ? VolumeType.LONG : VolumeType.SHORT;
    }

    protected Optional<Transaction> findLastSpecificTransactionByType(TradingContext tradingContext, Transaction.Type type) {
        return Lists.reverse(tradingContext.getTransactions()).stream().filter(t -> t.getType() == type).findFirst();
    }

    protected Optional<Transaction> findTransactionByTime(TradingContext tradingContext, LocalDateTime time) {
        int index = Collections.binarySearch(tradingContext.getTransactions(), Transaction.buildTimeKey(time), Comparator.comparing(Transaction::getTime));
        if (index >= 0) {
            return Optional.ofNullable(tradingContext.getTransactions().get(index));
        }
        return Optional.empty();
    }



    // 持仓类型
    protected enum VolumeType {
        // buy long 多头买进
        // sell short 空头卖出
        LONG, // 做多
        SHORT, // 做空
        EMPTY;
    }

    protected Decision makeDecision(Candle candle, TradingContext tradingContext) {
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

    public abstract void preFit(Candle candle, TradingContext tradingContext);

    protected abstract Decision entryDecision(Candle candle, TradingContext tradingContext);

    protected abstract Decision longPositionDecision(Candle candle, TradingContext tradingContext);

    protected abstract Decision shortPositionDecision(Candle candle, TradingContext tradingContext);

}
