package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.TradingContext;


public interface TradingStrategy {

    /**
     * 预处理，fit前调用
     * @param candle
     * @param tradingContext
     */
    void preFit(Candle candle, TradingContext tradingContext);

    void fit(Candle candle, TradingContext tradingContext);

}
