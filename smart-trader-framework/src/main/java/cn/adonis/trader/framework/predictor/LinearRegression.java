package cn.adonis.trader.framework.predictor;

import cn.adonis.trader.framework.model.CoordinatePoint;
import cn.adonis.trader.framework.model.Series;
import cn.adonis.trader.framework.util.TimeUtil;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.time.LocalDateTime;
import java.util.List;

public class LinearRegression implements TrendPredictor {

    private final SimpleRegression simpleRegression;

    private LinearRegression(SimpleRegression simpleRegression) {
        this.simpleRegression = simpleRegression;
    }

    public static LinearRegression fit(Series<?> series) {
        SimpleRegression simpleRegression = new SimpleRegression();
        series.getDataList().forEach(p -> simpleRegression.addData(p.getX(), p.getY()));
        return new LinearRegression(simpleRegression);
    }

    public static LinearRegression fit(List<? extends CoordinatePoint> points) {
        SimpleRegression simpleRegression = new SimpleRegression();
        points.forEach(point -> simpleRegression.addData(point.getX(), point.getY()));
        return new LinearRegression(simpleRegression);
    }

    @Override
    public double predict(LocalDateTime dateTime) {
        return simpleRegression.predict(TimeUtil.toSeconds(dateTime));
    }

    @Override
    public boolean isGoingUp() {
        return simpleRegression.getSlope() > 0;
    }

    @Override
    public boolean isGoingDown() {
        return simpleRegression.getSlope() < 0;
    }


}
