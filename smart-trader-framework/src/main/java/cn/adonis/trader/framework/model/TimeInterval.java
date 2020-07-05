package cn.adonis.trader.framework.model;

import java.util.concurrent.TimeUnit;

public class TimeInterval {

    public static final TimeInterval ONE_DAY = TimeInterval.days(1);
    public static final TimeInterval FIVE_MINUTES = TimeInterval.minutes(5);

    private final long duration;
    private final TimeUnit unit;

    private TimeInterval(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    public static TimeInterval of(long duration, TimeUnit timeUnit) {
        return new TimeInterval(duration, timeUnit);
    }

    public static TimeInterval millis(long duration) {
        return new TimeInterval(duration, TimeUnit.MILLISECONDS);
    }

    public static TimeInterval seconds(long duration) {
        return new TimeInterval(duration, TimeUnit.SECONDS);
    }

    public static TimeInterval minutes(long duration) {
        return new TimeInterval(duration, TimeUnit.MINUTES);
    }

    public static TimeInterval hours(long duration) {
        return new TimeInterval(duration, TimeUnit.HOURS);
    }

    public static TimeInterval days(long duration) {
        return new TimeInterval(duration, TimeUnit.DAYS);
    }

    public static TimeInterval weeks(long duration) {
        return new TimeInterval(duration * 7, TimeUnit.DAYS);
    }

    public long toMilliseconds() {
        return unit.toMillis(duration);
    }

    public long toSeconds() {
        return unit.toSeconds(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TimeInterval that = (TimeInterval) o;

        if (duration != that.duration)
            return false;
        return unit == that.unit;
    }

    @Override
    public int hashCode() {
        int result = (int) (duration ^ (duration >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
