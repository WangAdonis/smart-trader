package cn.adonis.trader.framework.loader;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.TimeSeries;


public interface SeriesLoader {

    TimeSeries<Candle> load() throws Exception;

}
