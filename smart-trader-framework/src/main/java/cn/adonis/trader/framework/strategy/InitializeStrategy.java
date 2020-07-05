package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.model.TradingContext;

public interface InitializeStrategy {
    /**
     * 初始化，运行策略前调用
     *
     * @param tradingContext
     */
    void init(TradingContext tradingContext);
}
