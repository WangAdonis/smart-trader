package cn.adonis.trader.framework.model;

import java.math.BigDecimal;

public interface TradingFee {

    BigDecimal getFee();

    BigDecimal getLever();
}
