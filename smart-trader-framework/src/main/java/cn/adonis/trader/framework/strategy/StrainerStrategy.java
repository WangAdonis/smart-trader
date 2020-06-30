package cn.adonis.trader.framework.strategy;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.Decision;
import cn.adonis.trader.framework.model.TradingContext;

public class StrainerStrategy extends AbstractFuturesTradingStrategy {
    @Override
    protected Decision makeDecision(Candle candle, TradingContext tradingContext) {
        return null;
    }
}
