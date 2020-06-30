package cn.adonis.trader.framework.indicator;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.Series;
import cn.adonis.trader.framework.model.TimeInterval;
import cn.adonis.trader.framework.util.SeriesUtil;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;


public class MovingAverage {

    public static BigDecimal calculate(Series series, Candle currentCandle, int n, TimeInterval timeInterval) {

        // 截取当前candle前的 n - 1 个
        Series subSeries = SeriesUtil.changeInterval(series, timeInterval)
                .subSeries(currentCandle, n - 1);
        List<Candle> candleList = Lists.newArrayList(subSeries.getCandleList());

        // 添加当前candle
        candleList.add(currentCandle);
        return Average.calculate(candleList, Candle::getClose);
    }

}
