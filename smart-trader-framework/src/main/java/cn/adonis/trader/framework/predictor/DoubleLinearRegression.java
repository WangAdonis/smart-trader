package cn.adonis.trader.framework.predictor;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.Series;
import cn.adonis.trader.framework.util.TimeUtil;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DoubleLinearRegression implements TrendPredictor {

    private final SimpleRegression simpleRegressionShort;

    private final SimpleRegression simpleRegressionLong;

    private DoubleLinearRegression(SimpleRegression simpleRegressionShort, SimpleRegression simpleRegressionLong) {
        this.simpleRegressionShort = simpleRegressionShort;
        this.simpleRegressionLong = simpleRegressionLong;
    }

    public static DoubleLinearRegression fit(Series<Candle> seriesShort, Series<Candle> seriesLong) {
        return new DoubleLinearRegression(getSimpleRegression(seriesShort), getSimpleRegression(seriesLong));
    }

    private static SimpleRegression getSimpleRegression(Series<Candle> series) {
        SimpleRegression simpleRegression = new SimpleRegression();
        series.stream().forEach(candle -> simpleRegression.addData(TimeUtil.toSeconds(candle.getTime()), candle.getClose().doubleValue()));
        return simpleRegression;
    }

    @Override
    public double predict(LocalDateTime dateTime) {
        return 0D;
    }

    @Override
    public boolean isGoingUp() {
        double shortSlope = simpleRegressionShort.getSlope();
        double longSlope = simpleRegressionLong.getSlope();
        return shortSlope > 0 && shortSlope > longSlope;
    }

    @Override
    public boolean isGoingDown() {
        double shortSlope = simpleRegressionShort.getSlope();
        double longSlope = simpleRegressionLong.getSlope();
        return shortSlope < 0 && shortSlope < longSlope;
    }

}
