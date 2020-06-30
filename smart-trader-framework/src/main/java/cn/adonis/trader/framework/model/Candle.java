package cn.adonis.trader.framework.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Candle implements Comparable<Candle> {

	private final BigDecimal open;
	private final BigDecimal close;
	private final BigDecimal high;
	private final BigDecimal low;
	private final LocalDateTime time;


	public static Candle create(BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, LocalDateTime time) {
		return new Candle(open, close, high, low, time);
	}

	public static Candle createFindKey(LocalDateTime time) {
		return create(null, null, null, null, time);
	}

	public Candle(BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, LocalDateTime time) {
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.time = time;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public BigDecimal getClose() {
		return close;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public LocalDateTime getTime() {
		return time;
	}

	@Override
	public int compareTo(Candle o) {
		return this.getTime().compareTo(o.getTime());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Candle candle = (Candle) o;

		return time.equals(candle.time);
	}

	@Override
	public int hashCode() {
		return time.hashCode();
	}

	public enum Schema {
		OPEN_PRICE, CLOSE_PRICE, HIGH_PRICE, LOW_PRICE, TIME
	}
}
