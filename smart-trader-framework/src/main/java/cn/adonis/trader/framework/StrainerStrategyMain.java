package cn.adonis.trader.framework;

import cn.adonis.trader.framework.loader.CSVLoader;
import cn.adonis.trader.framework.loader.SeriesLoader;
import cn.adonis.trader.framework.model.*;
import cn.adonis.trader.framework.strategy.StrainerStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


public class StrainerStrategyMain {

    private static final String PATH = "/Users/wangchun3/Desktop/t2009-0629.csv";

    public static void main(String[] args) throws Exception {

        // 1. 构建数据加载方式
        SeriesLoader seriesLoader = CSVLoader.newCSVLoader(PATH)
                .addColumnSchema(Candle.Schema.OPEN_PRICE, CSVLoader.Column.ofBigDecimal(3))
                .addColumnSchema(Candle.Schema.CLOSE_PRICE, CSVLoader.Column.ofBigDecimal(6))
                .addColumnSchema(Candle.Schema.HIGH_PRICE, CSVLoader.Column.ofBigDecimal(4))
                .addColumnSchema(Candle.Schema.LOW_PRICE, CSVLoader.Column.ofBigDecimal(5))
                .addColumnSchema(Candle.Schema.TIME, CSVLoader.Column.ofTime(2, "yyyy-MM-dd HH:mm"));

        //2. 构建回测参数
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        BackTestParameter backTestParameter = BackTestParameter.builder()
                .setStartTime(LocalDateTime.of(LocalDate.parse("2020-03-01", dateTimeFormatter), LocalTime.MIDNIGHT))
                .setEndTime(LocalDateTime.of(LocalDate.parse("2020-06-15", dateTimeFormatter), LocalTime.MIDNIGHT))
                .setInitialFunds("5000000")
                .setTradingFee(FuturesTradingFee.of("10000", "0.02"))
                .build();

        // 3. 构建回测策略
        StrainerStrategy.Parameter parameter = StrainerStrategy.Parameter.builder()
                .setTrendPredictPreviousCount(5)
                .setEnterVolumes("10")
                .setStopLoss("0.5")
                .setStopProfit("0.5")
                .build();
        StrainerStrategy strainerStrategy = StrainerStrategy.newStrainerStrategy(parameter);

        // 4. 构建回测系统
        BackTest backTest = BackTest.builder()
                .setSeriesLoader(seriesLoader)
                .setParameter(backTestParameter)
                .setTradingStrategy(strainerStrategy)
                .build();

        // 5. 执行回测
        BackTestResult result = backTest.run();
        TimeSeries<TimeDataPoint> dailyMa20 = TimeSeries.create(Series.create(strainerStrategy.getDailyMa20().getDataList(), "日线ma20"), TimeInterval.minutes(5));
        TimeSeries<TimeDataPoint> fiveMinutesMa60 = TimeSeries.create(Series.create(strainerStrategy.getFiveMinutesMa60().getDataList(), "5分钟线ma60"), TimeInterval.minutes(5));
        result.setExtTimeSeries(Arrays.asList(dailyMa20, fiveMinutesMa60));

        // 6. 输出回测结果
        String json = result.toEchartsJson();

        System.out.println("option = " + json + ";");
    }
}
