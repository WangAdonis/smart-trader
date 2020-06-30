package cn.adonis.trader.framework.model;

import cn.adonis.trader.framework.BackTestException;
import cn.adonis.trader.framework.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class Series {

    private final List<Candle> candleList;

    private final TimeInterval timeInterval;

    public static Series create(List<Candle> candleList, TimeInterval timeInterval) {
        return new Series(candleList, timeInterval, true);
    }

    public static Series createUncheck(List<Candle> candleList, TimeInterval timeInterval) {
        return new Series(candleList, timeInterval, false);
    }

    private Series(List<Candle> candleList, TimeInterval timeInterval, boolean init) {
        if (candleList == null) {
            candleList = Collections.emptyList();
        }
        candleList = new ArrayList<>(candleList);
        if (init) {
            Collections.sort(candleList);
            check(candleList);
        }
        this.candleList = Collections.unmodifiableList(candleList);
        this.timeInterval = timeInterval;
    }

    /**
     * 截取Series
     * @param startTime >=
     * @param endTime <=
     * @return
     */
    public Series find(LocalDateTime startTime, LocalDateTime endTime) {
        int start = 0;
        if (startTime != null) {
            start = Collections.binarySearch(candleList, Candle.createFindKey(startTime));
            start = start >= 0 ? start : Math.abs(start + 1);
        }

        int end = candleList.size();
        if (endTime != null) {
            end = Collections.binarySearch(candleList, Candle.createFindKey(endTime));
            end = end >= 0 ? end + 1 : Math.abs(end + 1);
        }

        return createUncheck(candleList.subList(start, end), timeInterval);
    }

    public int findCandle(Candle candle) {
        return Collections.binarySearch(candleList, Candle.createFindKey(TimeUtil.alignByInterval(candle.getTime(), timeInterval)));
    }

    /**
     * 截取Series
     * @param currentCandle 当前candle
     * @param beforeCount 向前追溯n个candle(不包括当前candle在内)
     * @return
     */
    public Series subSeries(Candle currentCandle, int beforeCount) {
        int index = findCandle(currentCandle);
        if (index < 0) {
            throw new BackTestException("can not find candle index");
        }
        int startIndex = index - beforeCount;
        if (startIndex < 0) {
            throw new BackTestException("can not subSeries");
        }
        return createUncheck(candleList.subList(startIndex, index), timeInterval);
    }

    public Optional<Candle> findHighestClosedPrice(LocalDateTime startTime, LocalDateTime endTime) {
        Series series = find(startTime, endTime);
        return series.stream().max(Comparator.comparing(Candle::getClose));
    }

    public Optional<Candle> findLowestClosedPrice(LocalDateTime startTime, LocalDateTime endTime) {
        Series series = find(startTime, endTime);
        return series.stream().min(Comparator.comparing(Candle::getClose));
    }

    public int size() {
        return candleList.size();
    }

    public Stream<Candle> stream() {
        return candleList.stream();
    }

    private void check(List<Candle> candleList) {
        Set<Candle> candles = new HashSet<>(candleList);
        if (candles.size() != candleList.size()) {
            throw new BackTestException("candleList has duplicate datetime candle");
        }
    }

    public List<Candle> getCandleList() {
        return candleList;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }
}
