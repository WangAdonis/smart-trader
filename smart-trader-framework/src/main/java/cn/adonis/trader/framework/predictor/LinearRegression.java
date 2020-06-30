package cn.adonis.trader.framework.predictor;

import cn.adonis.trader.framework.model.Series;
import cn.adonis.trader.framework.util.TimeUtil;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LinearRegression implements TrendPredictor {

    private final SimpleRegression simpleRegression;

    private LinearRegression(SimpleRegression simpleRegression) {
        this.simpleRegression = simpleRegression;
    }

    public static LinearRegression fit(Series series) {
        SimpleRegression simpleRegression = new SimpleRegression();
        series.stream().forEach(candle -> simpleRegression.addData(TimeUtil.toSeconds(candle.getTime()), candle.getClose().doubleValue()));
        return new LinearRegression(simpleRegression);
    }

    @Override
    public double predict(LocalDateTime dateTime) {
        return simpleRegression.predict(TimeUtil.toSeconds(dateTime));
    }

    @Override
    public boolean isGoingUp() {
        return simpleRegression.getSlope() > 0.0001;
    }

    @Override
    public boolean isGoingDown() {
        return simpleRegression.getSlope() < -0.0001;
    }


}
