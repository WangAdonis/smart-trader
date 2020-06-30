package cn.adonis.trader.framework.model;


import cn.adonis.trader.framework.util.BigDecimalUtil;

import java.math.BigDecimal;

public class Decision {

    private final BigDecimal volume;
    private final Reason reason;

    public static Decision DO_NOTHING = of(BigDecimal.ZERO, null);

    public static Decision buy(BigDecimal volume, Reason reason) {
        return of(volume.abs(), reason);
    }

    public static Decision sell(BigDecimal volume, Reason reason) {
        return of(volume.abs().negate(), reason);
    }

    public static Decision of(BigDecimal volume, Reason reason) {
        return new Decision(volume, reason);
    }

    private Decision(BigDecimal volume, Reason reason) {
        this.volume = volume;
        this.reason = reason;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public boolean isBuy() {
        return BigDecimalUtil.greaterThanZero(volume);
    }

    public boolean isSell() {
        return BigDecimalUtil.lessThanZero(volume);
    }

    public boolean doNoting() {
        return BigDecimalUtil.equalsZero(volume);
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        ENTRY, ADD, STOP_PROFIT, STOP_LOSS;
    }
}
