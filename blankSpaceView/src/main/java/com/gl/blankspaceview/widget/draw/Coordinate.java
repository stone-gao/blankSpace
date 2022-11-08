package com.gl.blankspaceview.widget.draw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gl
 * @desc 画线路径坐标
 */
public class Coordinate implements Serializable{
    public double x;
    public double y;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static List<String> getXs(List<Coordinate> points) {
        List<String> xs = new ArrayList<>();
        for (Coordinate point : points) {
            xs.add(point.x + "");
        }
        return xs;
    }

    public static List<String> getYs(List<Coordinate> points) {
        List<String> ys = new ArrayList<>();
        for (Coordinate point : points) {
            ys.add(point.y + "");
        }
        return ys;
    }
}
