package com.gl.blankspaceview.widget.draw;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.gl.blankspaceview.R;
import com.gl.blankspaceview.widget.BlankPhotoView;

import java.util.ArrayList;

/**
 *  @author gl
 * @desc 画线操作类
 */

public class MarkPathDrawer {

    /**
     * 画线Paint
     */
    private final Paint mGesturePaint = new Paint();

    /**
     * Resources
     */
    private Resources res;

    /**
     * ViewProvider,确定绘图画板宽高等属性
     */
    private ViewProvider mView;

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
     * 路径对象
     */
    private Path mPath = new Path();

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
    /**
     * 画笔颜色
     */
    private int pathColor = Color.RED;
    /**
     * 画笔粗细
     */
    private int pathSize = 4;


    public MarkPathDrawer(ViewProvider view) {
        mView = view;
        res = view.getResources();
        initPaint();
    }

    /**
     * 画笔初始化
     */
    private void initPaint() {
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setDither(true);
        mGesturePaint.setStyle(Paint.Style.STROKE);
        //预先约定的集中画笔颜色和粗细
        String[] colors = res.getStringArray(R.array.sync_color);
        String[] sizes = res.getStringArray(R.array.sync_size);
        if (colors != null && colors.length > 0) {
            pathColor = Color.parseColor(colors[0]);
        }
        if (sizes != null && sizes.length > 0) {
            pathSize = Integer.valueOf(sizes[sizes.length - 1]);
        }
        mGesturePaint.setColor(pathColor);
        mGesturePaint.setStrokeWidth(pathSize);
        mGesturePaint.setStrokeJoin(Paint.Join.ROUND);
        mGesturePaint.setStrokeCap(Paint.Cap.ROUND);
        mGesturePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
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
     * 手指点下屏幕时调用
     *
     * @param event
     */
    public void touchDown(TouchEvent event) {
        latestPath.clear();
        mPath.reset();
        float x = event.getX();
        float y = event.getY();
        latestPath.add(new Coordinate(MyPathUtils.getXRatio(x, mView), MyPathUtils.getYRatio(y, mView)));
        mX = x;
        mY = y;
        //mPath绘制的绘制起点
        mPath.moveTo(x, y);
        Log.i("起点", "(" + x + "," + y + ")");
    }

    //手指在屏幕上滑动时调用
    public void touchMove(TouchEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        Log.i("移动", "(" + x + "," + y + ")");

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        //两点之间的距离大于等于2时，生成贝塞尔绘制曲线
        if (dx >= MarkPath.TOUCH_TOLERANCE || dy >= MarkPath.TOUCH_TOLERANCE) {
            //设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            //二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);

            //第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;

            latestPath.add(new Coordinate(MyPathUtils.getXRatio(x, mView), MyPathUtils.getYRatio(y, mView)));
        }
        if (latestPath.size() >= 2) {
            mCanvas.drawPath(mPath, mGesturePaint);
        }
    }

    /**
     * 手指离开屏幕调用
     *
     * @return 返回画笔数据
     */
    public PathContainer touchUp() {
        if (latestPath.size() >= 2) {
            Path path = MyPathUtils.createPath(latestPath, mView);
            allPath.add(path);
            PathContainer pathContainer = new PathContainer(path, BlankPhotoView.DrawMode.path);
            pathContainer.latestPath = new ArrayList<>(latestPath);
            pathContainer.color = pathColor;
            pathContainer.size = pathSize;
            return pathContainer;
        }
        return null;
    }


    /**
     * 画线
     *
     * @param path
     */
    public void drawMarkPath(Path path) {
        mPath.reset();
        mPath.addPath(path);
        mCanvas.save();
        mCanvas.drawPath(mPath, mGesturePaint);
        mCanvas.restore();
        allPath.add(path);
        mView.invalidate();
    }

    /**
     * @return 是否为空
     */
    public boolean isEmpty() {
        return allPath.isEmpty();
    }

    /**
     * 撤销
     */
    public void revert() {
        int size = allPath.size();
        if (size > 0) {
            allPath.remove(size - 1);
            mPath.reset();
            for (Path path : allPath) {
                mCanvas.drawPath(path, mGesturePaint);
            }
        }
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    /**
     * 设置矩阵
     *
     * @param matrix 矩阵对象
     */
    public void setScale(Matrix matrix) {
        if (mBlank != null) {
            mBlank.drawMatrix(matrix);
        }
        mView.invalidate();
    }

    /**
     * 重置路径数据
     */
    public void reset() {
        latestPath.clear();
        mPath.reset();
    }

    /**
     * 设置画笔颜色
     *
     * @param pathColor 画笔颜色
     */
    public void setPathColor(int pathColor) {
        if (pathColor == 0)
            return;
        this.pathColor = pathColor;
        this.mGesturePaint.setColor(pathColor);
    }

    /**
     * 设置画笔粗细
     *
     * @param pathSize 画笔粗细
     */
    public void setPathSize(int pathSize) {
        if (pathSize == 0)
            return;
        this.pathSize = pathSize;
        this.mGesturePaint.setStrokeWidth(pathSize);
    }

    public int getPathColor() {
        return pathColor;
    }

    public int getPathSize() {
        return pathSize;
    }
}
