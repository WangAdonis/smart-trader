package cn.adonis.trader.framework.indicator;

import cn.adonis.trader.framework.model.*;
import cn.adonis.trader.framework.util.SeriesUtil;
import cn.adonis.trader.framework.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.SortedSet;


public class MovingAverage {

    private final Type type;
    private final TimeSeries<Candle> series;
    private final SortedSet<TimeDataPoint> dataList = Sets.newTreeSet();

    private MovingAverage(Type type, TimeSeries<Candle> series) {
        this.type = type;
        this.series = series;
    }

    public static MovingAverage create(TimeSeries<Candle> originalSeries, Type type) {
        return new MovingAverage(type, SeriesUtil.changeInterval(originalSeries, type.getTimeInterval(), SeriesUtil.MERGE_CANDLE_USE_CLOSED));
    }

    public static TimeDataPoint calculate(TimeSeries<Candle> series, Candle currentCandle, Type type) {
        // 截取当前candle前的 n - 1 个
        TimeSeries<Candle> subSeries = SeriesUtil.changeInterval(series, type.getTimeInterval(), SeriesUtil.MERGE_CANDLE_USE_CLOSED)
                .subSeries(Candle.createFindKey(TimeUtil.alignByInterval(currentCandle.getTime(), series.getTimeInterval())), type.getN() - 1);
        List<Candle> candleList = Lists.newArrayList(subSeries.getSeries().getDataList());

        // 添加当前candle
        candleList.add(currentCandle);
        return TimeDataPoint.of(Average.calculate(candleList, Candle::getClose), currentCandle.getTime());
    }

    public TimeDataPoint add(Candle currentCandle) {
        TimeSeries<Candle> subSeries = series.subSeries(Candle.createFindKey(TimeUtil.alignByInterval(currentCandle.getTime(), series.getTimeInterval())), type.getN() - 1);
        List<Candle> candleList = Lists.newArrayList(subSeries.getSeries().getDataList());
        // 添加当前candle
        candleList.add(currentCandle);
        TimeDataPoint data = TimeDataPoint.of(Average.calculate(candleList, Candle::getClose), currentCandle.getTime());
        dataList.add(data);
        return data;
    }

    public Type getType() {
        return type;
    }

    public TimeSeries<Candle> getSeries() {
        return series;
    }

    public SortedSet<TimeDataPoint> getDataList() {
        return dataList;
    }

    public static class Type {
        private final int n;
        private final TimeInterval timeInterval;

        public static Type of(int n, TimeInterval timeInterval) {
            return new Type(n, timeInterval);
        }

        private Type(int n, TimeInterval timeInterval) {
            this.n = n;
            this.timeInterval = timeInterval;
        }

        public int getN() {
            return n;
        }

        public TimeInterval getTimeInterval() {
            return timeInterval;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Type type = (Type) o;

            if (n != type.n)
                return false;
            return timeInterval.equals(type.timeInterval);
        }

        @Override
        public int hashCode() {
            int result = n;
            result = 31 * result + timeInterval.hashCode();
            return result;
        }
    }

}
