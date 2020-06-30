package cn.adonis.trader.framework.loader;

import cn.adonis.trader.framework.model.Series;


public interface SeriesLoader {

    Series load() throws Exception;

}
