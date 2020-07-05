package cn.adonis.trader.framework.model;


import cn.adonis.trader.framework.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TimeSeries<T extends TimeDataPoint> {

    private final Series<T> series;
    private final TimeInterval timeInterval;

    private TimeSeries(Series<T> series, TimeInterval timeInterval) {
        this.series = series;
        this.timeInterval = timeInterval;
    }

    public TimeSeries<T> subSeries(T point, int beforeCount) {
        return TimeSeries.create(series.subSeries(point, beforeCount), timeInterval);
    }

    public static <T extends TimeDataPoint> TimeSeries<T> create(Series<T> series, TimeInterval timeInterval) {
        return new TimeSeries<>(series, timeInterval);
    }

    public Series<T> getSeries() {
        return series;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    private TimeDataPoint createFindKey(LocalDateTime time) {
        return TimeDataPoint.of(BigDecimal.ZERO, time);
    }
}
