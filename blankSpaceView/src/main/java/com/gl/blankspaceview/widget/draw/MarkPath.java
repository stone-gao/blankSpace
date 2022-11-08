package com.gl.blankspaceview.widget.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.view.MotionEvent;

import com.gl.blankspaceview.widget.BlankPhotoView;

import java.util.ArrayList;

/**
 * @author gl
 * @desc 标注操作类
 */

public class MarkPath {
    public static float TOUCH_TOLERANCE = 1.0f;
    public static final String ACTION_ADD = "pathAdd";
    public static final String ACTION_ERASER = "pathEraser";
    public static final String ACTION_SCALE = "actionScale";
    public static final String ACTION_REVERT = "actionRevert";
    public static final String ACTION_CLEAR = "actionClear";
    public static final String ACTION_ROTATE = "actionRotate";
    public static final String ACTION_MOVE = "actionMove";
    public static final String ACTION_FLY = "actionFly";
    public static final String ACTION_SHADE = "actionShade";
    /**
     * 视图提供
     */
    private ViewProvider mView;

    /**
     * Canvas
     */
    private Canvas mCanvas;

    /**
     * 画板
     */
    private PhotoViewBlank mBlank;

    /**
     * 绘制监听
     */
    private BlankPhotoView.onDrawCompleteListener mDrawCompleteListener;

    /**
     * 所有路径数据
     */
    private ArrayList<PathContainer> allPath = new ArrayList<>();

    /**
     * 当前模式
     */
    private BlankPhotoView.DrawMode currentMode;

    /**
     * 橡皮擦工具类
     */
    private MarkEraseDrawer eraseDrawer;

    /**
     * 画线工具类
     */
    private MarkPathDrawer pathDrawer;


    public MarkPath(ViewProvider view) {
        mView = view;
        eraseDrawer = new MarkEraseDrawer(view);
        pathDrawer = new MarkPathDrawer(view);
    }

    public void init(PhotoViewBlank blank) {
        if (blank == null)
            return;
        mCanvas = blank.getCanvas();
        mBlank = blank;
        pathDrawer.init(mBlank);
        eraseDrawer.init(mBlank);
    }

    public void copy(MarkPath markPath) {
        this.setPathColor(markPath.getPathColor());
        this.setPathSize(markPath.getPathSize());
    }

    /**
     * 设置当前模式
     *
     * @param mode
     */
    public void setMode(BlankPhotoView.DrawMode mode) {
        this.currentMode = mode;
    }

    public void setOnDrawCompleteListener(BlankPhotoView.onDrawCompleteListener drawCompleteListener) {
        mDrawCompleteListener = drawCompleteListener;
    }

    public void reset() {
        eraseDrawer.reset();
        pathDrawer.reset();
    }

    public void clear() {
        reset();
        allPath.clear();
    }


    /**
     * 手指点下屏幕时调用
     *
     * @param event
     */
    private void touchDown(TouchEvent event) {
        if (currentMode.isEraser()) {
            eraseDrawer.touchDown(event.getX(), event.getY());
        } else if (currentMode.isPath()) {
            pathDrawer.touchDown(event);
        }
    }


    /**
     * 手指在屏幕上滑动时调用
     *
     * @param event
     */
    private void touchMove(TouchEvent event) {
        if (currentMode.isEraser()) {
            eraseDrawer.touchMove(event.getX(), event.getY());
        } else if (currentMode.isPath()) {
            pathDrawer.touchMove(event);
        }
    }

    /**
     * 手指离开屏幕
     */
    private void touchUp() {
        if (currentMode.isEraser()) {
            PathContainer newPath = eraseDrawer.touchUp();
            if (newPath != null) {
                allPath.add(newPath);
                if (mDrawCompleteListener != null) {
                    mDrawCompleteListener.onDrawComplete(ACTION_ERASER, newPath.latestPath);
                }
            }
        } else if (currentMode.isPath()) {
            PathContainer newPath = pathDrawer.touchUp();
            if (newPath != null) {
                allPath.add(newPath);
                if (mDrawCompleteListener != null) {
                    mDrawCompleteListener.onDrawComplete(ACTION_ADD, newPath.latestPath);
                }
            }
        }
    }

    /**
     * @return 是否为空
     */
    public boolean isEmpty() {
        return allPath.isEmpty();
    }

    /**
     * @return 是否初始化
     */
    public boolean isInit() {
        return mBlank != null && mCanvas != null;
    }

    /**
     * 撤销
     *
     * @param context
     */
    public void revert(Context context) {
        int size = allPath.size();
        if (size > 0) {
            allPath.remove(size - 1);
            reset();
            mBlank.clearDraw();
            for (PathContainer path : allPath) {
                if (path.drawMode.isPath()) {
                    pathDrawer.setPathColor(path.color);
                    pathDrawer.setPathSize(path.size);
                    pathDrawer.drawMarkPath(path.getPath(mView));
                } else if (path.drawMode.isEraser()) {
                    eraseDrawer.drawEraserPath(path.getPath(mView));
                }
            }
            mView.invalidate();
        } else {
            //  ToastUtils.showMsg(context, "已全部撤销");
        }
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    /**
     * 设置矩阵
     *
     * @param matrix
     */
    public void setScale(Matrix matrix) {
        if (mBlank != null) {
            //mBlank.drawMatrix(matrix);
            if (mCanvas != null) {
                mCanvas.setMatrix(matrix);
            }
        }
        mView.invalidate();
    }

    /**
     * 处理触摸屏幕事件
     *
     * @param event
     * @param matrixProvider 矩阵坐标转换
     * @param deltaX         放大时画线的坐标偏移
     * @param deltaY         放大时画线的坐标偏移
     * @return
     */
    public boolean onTouchEvent(MotionEvent event, ViewProvider matrixProvider, float deltaX, float deltaY) {
        //坐标转换
        final float xRatio = MyPathUtils.getXRatio(event.getX(), matrixProvider);
        final float yRatio = MyPathUtils.getYRatio(event.getY(), matrixProvider);
        float x = MyPathUtils.parseRatioToX(xRatio, mView);
        float y = MyPathUtils.parseRatioToY(yRatio, mView);
        System.out.println("#before:" + event.getX() + "  " + x);
        System.out.println("#before1:" + event.getY() + "  " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(new TouchEvent(x + deltaX, y + deltaY));
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(new TouchEvent(x + deltaX, y + deltaY));
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        mView.invalidate();
        return true;
    }

    /**
     * @param path 设置画线数据
     */
    public void drawMarkPath(Path path) {
        pathDrawer.drawMarkPath(path);
    }

    /**
     * @param path 设置画线路径数据
     */
    public void drawMarkPath(PathContainer path) {
        pathDrawer.setPathColor(path.color);
        pathDrawer.setPathSize(path.size);
        pathDrawer.drawMarkPath(path.getPath(mView));
        allPath.add(path);
    }

    /**
     * @param path 设置橡皮擦画线数据
     */
    public void drawEraserPath(Path path) {
        eraseDrawer.drawEraserPath(path);
    }

    /**
     * @param path 设置橡皮擦画线路径数据
     */
    public void drawEraserPath(PathContainer path) {
        eraseDrawer.drawEraserPath(path.getPath(mView));
        allPath.add(path);
    }

    /**
     * @return 获得所有路径操作数据，不包括撤销
     */
    public ArrayList<PathContainer> getAllPath() {
        return allPath;
    }

    /**
     * 刷新
     */
    public void refresh() {
        if (!isInit()) {
            return;
        }
        mBlank.clearDraw();
        mView.invalidate();
        mBlank.drawBgBitmap();
        for (PathContainer path : allPath) {
            if (path.drawMode.isPath()) {
                pathDrawer.setPathColor(path.color);
                pathDrawer.setPathSize(path.size);
                pathDrawer.drawMarkPath(path.getPath(mView));
            } else if (path.drawMode.isEraser()) {
                eraseDrawer.drawEraserPath(path.getPath(mView));
            }
        }
    }

    /**
     * 设置路径数据
     *
     * @param paths
     */
    public void setAllPath(ArrayList<PathContainer> paths) {
        if (mBlank == null || paths == null)
            return;
        allPath.clear();
        allPath.addAll(paths);
        reset();
        refresh();
    }

    /**
     * 设置画笔颜色
     *
     * @param pathColor
     */
    public void setPathColor(int pathColor) {
        if (pathColor == 0)
            return;
        pathDrawer.setPathColor(pathColor);
    }

    /**
     * 设置画笔粗细
     *
     * @param pathSize
     */
    public void setPathSize(int pathSize) {
        if (pathSize == 0)
            return;
        pathDrawer.setPathSize(pathSize);
    }

    public int getPathColor() {
        return pathDrawer.getPathColor();
    }

    public int getPathSize() {
        return pathDrawer.getPathSize();
    }
}

