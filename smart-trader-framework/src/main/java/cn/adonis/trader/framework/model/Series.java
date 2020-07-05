package cn.adonis.trader.framework.model;

import cn.adonis.trader.framework.BackTestException;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Stream;

public class Series<T extends CoordinatePoint> {

    private final List<T> dataList;

    private final String name;

    public static <T extends CoordinatePoint> Series<T> create(Collection<T> dataList, String name) {
        return new Series<>(Lists.newArrayList(dataList), name, true);
    }

    public static <T extends CoordinatePoint> Series<T> createUncheck(List<T> dataList, String name) {
        return new Series<>(dataList, name, false);
    }

    protected Series(List<T> dataList, String name, boolean needSort) {
        if (dataList == null) {
            dataList = Collections.emptyList();
        }
        dataList = new ArrayList<>(dataList);
        if (needSort) {
            Collections.sort(dataList);
            check(dataList);
        }
        this.dataList = Collections.unmodifiableList(dataList);
        this.name = name;
    }

    /**
     * 截取Series
     * @param startX >=
     * @param endX <=
     * @return
     */
    public Series<T> find(T startX, T endX) {
        int start = 0;
        if (startX != null) {
            start = Collections.binarySearch(dataList, startX);
            start = start >= 0 ? start : Math.abs(start + 1);
        }

        int end = dataList.size();
        if (endX != null) {
            end = Collections.binarySearch(dataList, endX);
            end = end >= 0 ? end + 1 : Math.abs(end + 1);
        }

        return createUncheck(dataList.subList(start, end), name);
    }

    /**
     * 截取Series
     * @param point 当前点
     * @param beforeCount 向前追溯n个点(不包括当前点在内)
     * @return
     */
    public Series<T> subSeries(T point, int beforeCount) {
        int index = Collections.binarySearch(dataList, point);
        if (index < 0) {
            throw new BackTestException("can not find point index");
        }
        int startIndex = index - beforeCount;
        if (startIndex < 0) {
            return createUncheck(Collections.emptyList(), name);
        }
        return createUncheck(dataList.subList(startIndex, index), name);
    }

    public int size() {
        return dataList.size();
    }

    public Stream<T> stream() {
        return dataList.stream();
    }

    private void check(List<T> dataList) {
        Set<T> dataSet = new HashSet<>(dataList);
        if (dataSet.size() != dataList.size()) {
            throw new BackTestException("dataList has duplicate x point");
        }
    }

    public List<T> getDataList() {
        return dataList;
    }

    public String getName() {
        return name;
    }
}
