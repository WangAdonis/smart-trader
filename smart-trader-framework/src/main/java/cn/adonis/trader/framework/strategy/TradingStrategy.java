package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.TradingContext;


public interface TradingStrategy {

    void fit(Candle candle, TradingContext tradingContext);

}
