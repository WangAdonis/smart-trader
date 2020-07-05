package cn.adonis.trader.framework.model;
import cn.adonis.trader.framework.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TimeDataPoint implements CoordinatePoint {
    protected final BigDecimal value;
    protected final LocalDateTime time;

    public static TimeDataPoint of(BigDecimal value, LocalDateTime time) {
        return new TimeDataPoint(value, time);
    }

    protected TimeDataPoint(BigDecimal value, LocalDateTime time) {
        this.value = value;
        this.time = time;
    }

    public BigDecimal getValue() {
        return value;
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TimeDataPoint data = (TimeDataPoint) o;

        return time.equals(data.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    @Override
    public double getX() {
        return TimeUtil.toSeconds(time);
    }

    @Override
    public double getY() {
        return value.doubleValue();
    }

    public TimeDataPoint modifyTime(LocalDateTime time) {
        return TimeDataPoint.of(this.value, time);
    }

}
