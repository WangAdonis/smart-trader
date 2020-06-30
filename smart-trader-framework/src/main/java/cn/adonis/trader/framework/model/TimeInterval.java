package cn.adonis.trader.framework.model;

import java.util.concurrent.TimeUnit;

public class TimeInterval {

    public final long duration;
    public final TimeUnit unit;

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

}
