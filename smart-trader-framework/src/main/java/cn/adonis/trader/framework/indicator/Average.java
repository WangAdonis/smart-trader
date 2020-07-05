package cn.adonis.trader.framework.indicator;

import cn.adonis.trader.framework.model.Candle;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Average {

    public static BigDecimal calculate(List<Candle> candleList, Function<Candle, BigDecimal> input) {
        BigDecimal sum = candleList.stream()
                .filter(Objects::nonNull)
                .map(input)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        return sum.divide(BigDecimal.valueOf(candleList.size()), 2, BigDecimal.ROUND_HALF_UP);
    }


}
