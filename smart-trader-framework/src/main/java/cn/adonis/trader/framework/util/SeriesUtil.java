package cn.adonis.trader.framework.util;

import cn.adonis.trader.framework.BackTestException;
import cn.adonis.trader.framework.model.*;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SeriesUtil {

    public static <T> T getLast(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public static BiFunction<LocalDateTime, List<Candle>, Candle> MERGE_CANDLE_USE_CLOSED = (time, list) -> {
        Candle candle = SeriesUtil.getLast(list);
        if (candle != null) {
            return candle.modifyTime(time);
        }
        return null;
    };

    public static <T extends TimeDataPoint> TimeSeries<T> changeInterval(TimeSeries<T> timeSeries, TimeInterval timeInterval, BiFunction<LocalDateTime, List<T>, T> mergeFunction) {
        if (timeInterval.toSeconds() < timeSeries.getTimeInterval().toSeconds()) {
            throw new BackTestException("can not change timeSeries interval");
        } else if (timeInterval.toSeconds() == timeSeries.getTimeInterval().toSeconds()) {
            return timeSeries;
        }

        Map<Long, List<T>> candleMap = timeSeries.getSeries().stream()
                .collect(Collectors.groupingBy(p -> TimeUtil.toSeconds(p.getTime()) / timeInterval.toSeconds()));

        List<T> candleList = candleMap.entrySet().stream()
                .map(entry -> {
                    long period = entry.getKey();
                    return mergeFunction.apply(TimeUtil.toLocalDateTime(period * timeInterval.toSeconds()), entry.getValue());
                }).collect(Collectors.toList());

        return TimeSeries.create(Series.create(candleList, timeSeries.getSeries().getName()), timeInterval);
    }

    public static Optional<Candle> findHighestClosedPrice(TimeSeries<Candle> timeSeries, LocalDateTime startTime, LocalDateTime endTime) {
        Series<Candle> series = timeSeries.getSeries().find(Candle.createFindKey(startTime), Candle.createFindKey(endTime));
        return series.stream().max(Comparator.comparing(Candle::getClose));
    }

    public static Optional<Candle> findLowestClosedPrice(TimeSeries<Candle> timeSeries, LocalDateTime startTime, LocalDateTime endTime) {
        Series<Candle> series = timeSeries.getSeries().find(Candle.createFindKey(startTime), Candle.createFindKey(endTime));
        return series.stream().min(Comparator.comparing(Candle::getClose));
    }
}
