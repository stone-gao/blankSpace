package com.gl.blankspaceview.widget.draw;

import android.graphics.Path;

import com.gl.blankspaceview.widget.BlankPhotoView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author gl
 * @desc 保存路径数据，路径颜色粗细等数据
 */

public class PathContainer implements Serializable {

    /**
     * 不可作为Serializable序列化,只能用作当前绘制
     */
    public transient Path path;

    /**
     * 最近一次的路径数据
     */
    public ArrayList<Coordinate> latestPath;

    /**
     * 绘制模式
     */
    public BlankPhotoView.DrawMode drawMode;

    /**
     * 路径颜色
     */
    public int color;

    /**
     * 路径粗细
     */
    public int size;

    /**
     * 记录本次路径对应的背景视图的宽
     */
    private int width;
    /**
     * 记录本次路径对应的背景视图的高
     */
    private int height;
    public PathContainer() {
        this(null);
    }

    public PathContainer(BlankPhotoView.DrawMode drawMode) {
        this(null, drawMode);
    }

    public PathContainer(Path path, BlankPhotoView.DrawMode drawMode) {
        this.path = path;
        this.drawMode = drawMode;
    }

    public void setDrawMode(BlankPhotoView.DrawMode drawMode) {
        this.drawMode = drawMode;
    }

    /**
     * 根据视图提供器获得路径
     *
     * @param view 视图提供器
     * @return 返回可直接用于绘制的路径对象
     */
    public Path getPath(ViewProvider view) {
        //比对当前视图的宽高和历史宽高，如果发生变化需要重新生成可用于绘制的路径
        if (path == null || path.isEmpty() || view.getHeight() != height || view.getWidth() != width) {
            path = MyPathUtils.createPath(latestPath, view);
            width = view.getWidth();
            height = view.getHeight();
        }
        return path;
    }
}
