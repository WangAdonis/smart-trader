package cn.adonis.trader.framework.loader;

import cn.adonis.trader.framework.model.Candle;
import cn.adonis.trader.framework.model.Series;
import cn.adonis.trader.framework.model.TimeInterval;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CSVLoader implements SeriesLoader {

    private final String path;
    private final Map<Candle.Schema, Column<?>> schemaMap;

    private CSVLoader(String path) {
        this.path = path;
        this.schemaMap = Maps.newHashMap();
    }

    public static CSVLoader newCSVLoader(String path) {
        return new CSVLoader(path);
    }

    public int getMaxColumnIndex() {
        return schemaMap.values().stream().map(Column::getIndex).max(Integer::compareTo).orElse(0);
    }

    public CSVLoader addColumnSchema(Candle.Schema schema, Column<?> column) {
        this.schemaMap.put(schema, column);
        return this;
    }


    @Override
    public Series load() throws IOException {
        final int maxColumnIndex = getMaxColumnIndex();
        List<Candle> candleList = Files.lines(Paths.get(path))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(s -> StringUtils.split(s, ","))
                .filter(array -> array.length >= maxColumnIndex)
                .map(this::parseCandle)
                .collect(Collectors.toList());

        TimeInterval timeInterval = null;
        if (candleList.size() >= 2) {
            Candle first = candleList.get(0);
            Candle second = candleList.get(1);
            Duration duration = Duration.between(first.getTime(), second.getTime());
            timeInterval = TimeInterval.millis(duration.toMillis());
        }
        return Series.create(candleList, timeInterval);
    }

    private Candle parseCandle(String[] array) {
        Optional<Column> openColumn = Optional.ofNullable(schemaMap.get(Candle.Schema.OPEN_PRICE));
        Optional<Column> closeColumn = Optional.ofNullable(schemaMap.get(Candle.Schema.CLOSE_PRICE));
        Optional<Column> highColumn = Optional.ofNullable(schemaMap.get(Candle.Schema.HIGH_PRICE));
        Optional<Column> lowColumn = Optional.ofNullable(schemaMap.get(Candle.Schema.LOW_PRICE));
        Optional<Column> timeColumn = Optional.ofNullable(schemaMap.get(Candle.Schema.TIME));

        BigDecimal open = (BigDecimal) openColumn.map(column -> column.getValue(array)).orElse(BigDecimal.ZERO);
        BigDecimal close = (BigDecimal) closeColumn.map(column -> column.getValue(array)).orElse(BigDecimal.ZERO);
        BigDecimal high = (BigDecimal) highColumn.map(column -> column.getValue(array)).orElse(BigDecimal.ZERO);
        BigDecimal low = (BigDecimal) lowColumn.map(column -> column.getValue(array)).orElse(BigDecimal.ZERO);
        LocalDateTime time = (LocalDateTime) timeColumn.map(column -> column.getValue(array)).orElse(null);
        return Candle.create(open, close, high, low, time);
    }

    public static class Column<T> {
        private final int index;
        private final Function<String, T> mapper;

        public static <T> Column<T> of(int index, Function<String, T> mapper) {
            return new Column<>(index, mapper);
        }

        public static Column<BigDecimal> ofBigDecimal(int index) {
            return new Column<>(index, BigDecimal::new);
        }

        public static Column<LocalDateTime> ofTime(int index) {
            return new Column<>(index, s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        public static Column<LocalDateTime> ofTime(int index, String dateTimePattern) {
            return new Column<>(index, s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern(dateTimePattern)));
        }

        public int getIndex() {
            return index;
        }

        private Column(int index, Function<String, T> mapper) {
            this.index = index;
            this.mapper = mapper;
        }

        public T parseValue(String str) {
            return mapper.apply(str);
        }

        public T getValue(String[] line) {
            return mapper.apply(line[index]);
        }
    }
}
