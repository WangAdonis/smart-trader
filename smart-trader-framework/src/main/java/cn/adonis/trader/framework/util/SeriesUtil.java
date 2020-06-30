package cn.adonis.trader.framework.util;

import cn.adonis.trader.framework.BackTestException;
import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.Series;
import cn.adonis.trader.framework.model.TimeInterval;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SeriesUtil {

    public static Series changeInterval(Series series, TimeInterval timeInterval) {
        return changeInterval(series, timeInterval, candleList -> {
            List<Candle> tempList = Lists.newArrayList(candleList);
            Collections.sort(tempList);
            return tempList.get(tempList.size() - 1);
        });
    }

    public static Series changeInterval(Series series, TimeInterval timeInterval, Function<List<Candle>, Candle> mergeFunction) {
        if (timeInterval.toSeconds() < series.getTimeInterval().toSeconds()) {
            throw new BackTestException("can not change series interval");
        }

        Map<Long, List<Candle>> candleMap = series.stream()
                .collect(Collectors.groupingBy(candle -> TimeUtil.toSeconds(candle.getTime()) / timeInterval.toSeconds()));

        List<Candle> candleList = candleMap.entrySet().stream()
                .map(entry -> {
                    long period = entry.getKey();
                    Candle merged = mergeFunction.apply(entry.getValue());
                    return Candle.create(merged.getOpen(), merged.getClose(), merged.getHigh(), merged.getLow(), TimeUtil.toLocalDateTime(period * timeInterval.toSeconds()));
                }).collect(Collectors.toList());

        return Series.create(candleList, timeInterval);
    }
}
