package cn.adonis.trader.framework.model;


public interface CoordinatePoint extends Comparable<CoordinatePoint> {
    double getX();
    double getY();

    @Override
    default int compareTo(CoordinatePoint o) {
        return Double.compare(getX(), o.getX());
    }
}
