package cn.adonis.trader.framework.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BackTestResult {

    private static final String ECHARTS_TEMPLATE = "{\"tooltip\":{\"trigger\":\"axis\",\"axisPointer\":{\"animation\":false}},\"axisPointer\":{\"link\":{\"xAxisIndex\":\"all\"}},\"toolbox\":{\"feature\":{\"dataView\":{\"show\":true,\"readOnly\":false},\"magicType\":{\"show\":true,\"type\":[\"line\",\"bar\"]},\"restore\":{\"show\":true},\"saveAsImage\":{\"show\":true}}},\"grid\":[{\"left\":50,\"right\":50,\"height\":\"35%\"},{\"left\":50,\"right\":50,\"top\":\"55%\",\"height\":\"35%\"}],\"xAxis\":[{\"type\":\"category\",\"boundaryGap\":false,\"data\":[],\"axisLine\":{\"onZero\":true}},{\"gridIndex\":1,\"boundaryGap\":false,\"data\":[],\"axisLine\":{\"onZero\":true}}],\"yAxis\":[{\"type\":\"value\",\"name\":\"价格\",\"min\":90},{\"type\":\"value\",\"name\":\"持仓\"},{\"gridIndex\":1,\"type\":\"value\",\"name\":\"收益\"}],\"dataZoom\":[{\"show\":true,\"realtime\":true,\"start\":30,\"end\":70,\"xAxisIndex\":[0,1]},{\"type\":\"inside\",\"realtime\":true,\"start\":30,\"end\":70,\"xAxisIndex\":[0,1]}],\"series\":[{\"name\":\"价格\",\"type\":\"line\",\"data\":[]},{\"name\":\"持仓\",\"type\":\"line\",\"yAxisIndex\":1,\"data\":[]},{\"name\":\"收益\",\"type\":\"line\",\"xAxisIndex\":1,\"yAxisIndex\":2,\"data\":[]}]}";

    private BigDecimal profit;
    private List<Transaction> transactions;
    private Series originalData;
    private List<Settlement> settlements;

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Series getOriginalData() {
        return originalData;
    }

    public void setOriginalData(Series originalData) {
        this.originalData = originalData;
    }

    public List<Settlement> getSettlements() {
        return settlements;
    }

    public void setSettlements(List<Settlement> settlements) {
        this.settlements = settlements;
    }

    public String toEchartsJson() {
        List<String> timeStrList = originalData.stream().map(c -> c.getTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))).collect(Collectors.toList());
        List<String> priceStrList = originalData.stream().map(Candle::getClose).map(b -> b.setScale(2, RoundingMode.HALF_UP)).map(b -> b.stripTrailingZeros().toPlainString()).collect(Collectors.toList());
        Map<LocalDateTime, Transaction> timeTransactionMap = transactions.stream().collect(Collectors.toMap(Transaction::getTime, t -> t));

        // 持仓对齐
        BigDecimal holdVolume = BigDecimal.ZERO;
        List<String> holdVolumeStrList = Lists.newArrayListWithCapacity(originalData.size());
        for (Candle candle : originalData.getCandleList()) {
            Transaction transaction = timeTransactionMap.get(candle.getTime());
            if (transaction != null) {
                holdVolume = holdVolume.add(transaction.getVolume());
            }
            holdVolumeStrList.add(holdVolume.stripTrailingZeros().toPlainString());
        }

        // 收益对齐
        BigDecimal profit = BigDecimal.ZERO;
        List<String> settlementStrList = Lists.newArrayListWithCapacity(settlements.size());
        Map<LocalDateTime, Settlement> timeSettlementMap = settlements.stream().collect(Collectors.toMap(Settlement::getTime, s -> s));
        for (Candle candle : originalData.getCandleList()) {
            Settlement settlement = timeSettlementMap.get(candle.getTime());
            if (settlement != null) {
                profit = profit.add(settlement.getProfit());
            }
            settlementStrList.add(profit.stripTrailingZeros().toPlainString());
        }


        JSONObject results = JSONObject.parseObject(ECHARTS_TEMPLATE);

        // 拼装x轴时间参数
        JSONArray xAxis = results.getJSONArray("xAxis");
        ((JSONObject) xAxis.get(0)).put("data", timeStrList);
        ((JSONObject) xAxis.get(1)).put("data", timeStrList);

        // 拼装曲线参数
        JSONArray series = results.getJSONArray("series");
        ((JSONObject) series.get(0)).put("data", priceStrList);
        ((JSONObject) series.get(1)).put("data", holdVolumeStrList);
        ((JSONObject) series.get(2)).put("data", settlementStrList);
//        results.put("times", timesArray);
//        results.put("prices", priceArray);
//        results.put("settlements", settlementArray);
//        results.put("volumes", holdVolumeArray);
//        results.put("profit", profit.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());

        return results.toJSONString();
    }
}
