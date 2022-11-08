package com.gl.blankspaceview.widget.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.gl.blankspaceview.widget.BlankPhotoView;

import java.util.ArrayList;

/**
 * @author gl
 * @desc 橡皮擦操作类
 */

public class MarkEraseDrawer {
    /**
     * Paint
     */
    private Paint mEraserPaint;

    /**
     * ViewProvider,确定绘图画板宽高等属性
     */
    private ViewProvider mView;

    /**
     * 路径对象
     */
    private Path mPath = new Path();

    /**
     * 当前手指X坐标
     */
    private float mX;

    /**
     * 当前手指Y坐标
     */
    private float mY;

    /**
     * Canvas
     */
    private Canvas mCanvas;


    /**
     * 画板
     */
    private PhotoViewBlank mBlank;

    /**
     * 记录最近一次坐标
     */
    private ArrayList<Coordinate> latestPath = new ArrayList<>();

    /**
     * 路径列表
     */
    private ArrayList<Path> allPath = new ArrayList<>();

    public MarkEraseDrawer(ViewProvider view) {
        mView = view;
        initPaint();
    }

    private void initPaint() {
        //橡皮擦
        mEraserPaint = new Paint();
        mEraserPaint.setAlpha(0);
        //这个属性是设置paint为橡皮擦重中之重
        //这是重点
        //下面这句代码是橡皮擦设置的重点
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //上面这句代码是橡皮擦设置的重点（重要的事是不是一定要说三遍）
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setStrokeWidth(10);
    }

    /**
     * 初始化画板
     *
     * @param blank
     */
    public void init(PhotoViewBlank blank) {
        mCanvas = blank.getCanvas();
        mBlank = blank;
    }

    /**
     * 手指按下
     *
     * @param x
     * @param y
     */
    public void touchDown(float x, float y) {
        latestPath.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        latestPath.add(new Coordinate(MyPathUtils.getXRatio(x, mView), MyPathUtils.getYRatio(y, mView)));
    }

    /**
     * 画橡皮擦路径
     *
     * @param path 路径对象
     */
    public void drawEraserPath(Path path) {
        mPath.reset();
        mPath.addPath(path);
        mCanvas.save();
        mCanvas.drawPath(mPath, mEraserPaint);
        mCanvas.restore();
        allPath.add(path);
        mView.invalidate();
    }

    /**
     * @return 是否为空数据
     */
    public boolean isEmpty() {
        return allPath.isEmpty();
    }

    /**
     * 撤销
     * @deprecated
     */
    public void revert() {
        int size = allPath.size();
        if (size > 0) {
            allPath.remove(size - 1);
            mPath.reset();
            for (Path path : allPath) {
                mCanvas.drawPath(path, mEraserPaint);
            }
        }
    }


    /**
     * 手指屏幕移动
     *
     * @param x
     * @param y
     */
    public void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= MarkPath.TOUCH_TOLERANCE || dy >= MarkPath.TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            latestPath.add(new Coordinate(MyPathUtils.getXRatio(x, mView), MyPathUtils.getYRatio(y, mView)));
            if (latestPath.size() > 2) {
                mCanvas.drawPath(mPath, mEraserPaint);
            }
        }
    }


    /**
     * @return 手指离开屏幕
     */
    public PathContainer touchUp() {
        if (latestPath.size() > 2) {
            Path path = MyPathUtils.createPath(latestPath, mView);
            allPath.add(path);
            PathContainer pathContainer = new PathContainer(path, BlankPhotoView.DrawMode.eraser);
            pathContainer.latestPath = new ArrayList<>(latestPath);
            return pathContainer;
        }
        latestPath.clear();
        return null;
    }

    public Canvas getCanvas() {
        return mCanvas;
    }


    /**
     * 重置路径数据
     */
    public void reset() {
        latestPath.clear();
        mPath.reset();
    }
}
