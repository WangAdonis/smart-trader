package cn.adonis.trader.framework.util;

import cn.adonis.trader.framework.model.TimeInterval;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtil {

    public static final ZoneOffset TIME_ZONE = ZoneOffset.of("+8");

    public static long toSeconds(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(TIME_ZONE);
    }

    public static LocalDateTime toLocalDateTime(long seconds) {
        return LocalDateTime.ofEpochSecond(seconds, 0, TIME_ZONE);
    }

    public static LocalDateTime alignByInterval(LocalDateTime localDateTime, TimeInterval timeInterval) {
        long seconds = toSeconds(localDateTime);
        long period = seconds / timeInterval.toSeconds();
        return toLocalDateTime(period * timeInterval.toSeconds());
    }
}
