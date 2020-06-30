package cn.adonis.trader.framework.predictor;

import java.time.LocalDateTime;

public interface TrendPredictor {

    double predict(LocalDateTime dateTime);

    boolean isGoingUp();

    boolean isGoingDown();
}
