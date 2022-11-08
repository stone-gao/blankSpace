package com.gl.blankspaceview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.gl.blankspaceview.widget.draw.Coordinate;
import com.gl.blankspaceview.widget.draw.MarkPath;
import com.gl.blankspaceview.widget.draw.PathContainer;
import com.gl.blankspaceview.widget.draw.PhotoViewBlank;
import com.gl.blankspaceview.widget.draw.ViewProvider;
import com.gl.blankspaceview.widget.photoview.PhotoView;
import com.gl.blankspaceview.widget.photoview.PhotoViewAttacker;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author gl
 *
 */
public class BlankPhotoView extends PhotoView implements ViewProvider, View.OnTouchListener,
        PhotoViewAttacker.OnScaleChangeListener, PhotoViewAttacker.OnMatrixChangedListener {

    /**
     * 绘图模式枚举
     */
    public enum DrawMode implements Serializable {
        path,
        normal,
        eraser;

        public boolean isNormal() {
            return this == normal;
        }

        public boolean isPath() {
            return this == path;
        }

        public boolean isEraser() {
            return this == eraser;
        }
    }

    /**
     * 画线工具类
     */
    private MarkPath markPath;

    /**
     * 画板
     */
    private PhotoViewBlank blank;

    /**
     * 绘图模式模式
     */
    private DrawMode mMode = DrawMode.normal;

    /**
     * 绘图矩阵
     */
    private Matrix mMatrix = new Matrix();

    /**
     * 旋转角度
     */
    private float mDegree = 0;

    /**
     * 初始化监听
     */
    private Runnable initListener;

    /**
     * 是否可以操作
     */
    private boolean isOperation = true;

    /**
     * 绘图操作完成回调
     */
    private onDrawCompleteListener mDrawCompleteListener;

    /**
     * 原始宽高
     */
    private Rect originRect = new Rect();

    /**
     * 对比图片宽高和父空间宽高做等比缩放
     */
    private RectF minRect = new RectF();

    /**
     * drawable
     */
    private Drawable source;

    /**
     * 旋转监听
     */
    private OnRotateListener onRotateListener;

    /**
     * 旋转监听
     */
    private PhotoViewAttacker.OnScaleChangeListener onScaleChangeListener;

    private RectF displayRectF = new RectF();


    public BlankPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
        blank = new PhotoViewBlank();
        markPath = new MarkPath(this);
        setAllowParentInterceptOnEdge(true);
        setOnTouchListener(this);
        mAttacker.setOnScaleChangeListener(this);
        mAttacker.setOnMatrixChangeListener(this);
        markPath.setOnDrawCompleteListener(mDrawCompleteListener);
    }

    /**
     * @param listener 设置绘图监听
     */
    public void setOnDrawCompleteListener(onDrawCompleteListener listener) {
        this.mDrawCompleteListener = listener;
        markPath.setOnDrawCompleteListener(mDrawCompleteListener);
    }

    /**
     * @param drawable 设置图片drawable
     */
    @Override
    public void setImageDrawable(final Drawable drawable) {
        super.setImageDrawable(drawable);
        source = drawable;
        if (drawable == null)
            return;
        post(() -> {
            if (drawable != null) {
                if (blank.isDirty()) {
                    blank.clearDraw();
                    invalidate();
                    blank.destroy(getWidth(), getHeight());
                }
                if (getWidth() == 0 || getHeight() == 0)
                    return;
                System.out.println("#set src view:" + getWidth() + " " + getHeight());
                markPath.init(blank.build(getWidth(), getHeight()));
                if (initListener != null) {
                    initListener.run();
                }
            }
        });
    }

    public void rotateTo(final float degree) {
        post(() -> {
            if (Float.compare(degree, Float.NaN) == 0 || Float.compare(degree, 0) == 0)
                return;
            if (source == null) {
                source = getDrawable();
            }
            if (originRect.isEmpty()) {
                originRect.set(0, 0, getWidth(), getHeight());
            }
            minRect = getMinRect();
            //非法宽高直接退出
            if (minRect == null)
                return;
            ViewGroup.LayoutParams lp = getLayoutParams();
            if ((degree / 90) % 2 == 0) {
                lp.width = originRect.width();
                lp.height = originRect.height();
            } else {//反转宽高
                float scaleW = minRect.width() * 1.0f / originRect.width();
                float scaleH = minRect.height() * 1.0f / originRect.height();
                lp.width = (int) (originRect.width() * scaleW);
                lp.height = (int) (originRect.height() * scaleH);
            }
            setLayoutParams(lp);
            setRotation(degree);
            post(() -> {
                if (onRotateListener != null) {
                    onRotateListener.onRotate(getDegree(), BlankPhotoView.this);
                }
            });
            if (degree == -360) {
                mDegree = 0;
            }
            mDegree = degree;
        });
    }

    public void rotate() {
        mDegree += -90;
        rotateTo(mDegree);
    }

    private RectF getMinRect() {
        RectF rect = new RectF();
        View parent = (View) getParent();
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();
        int srcWidth = source.getIntrinsicWidth();
        int srcHeight = source.getIntrinsicHeight();
        rect.set(0, 0, Math.min(parentWidth, srcHeight), Math.min(parentHeight, srcWidth));
        int nowW = originRect.width();
        int nowH = originRect.height();
        //float scaleWidth = ((float) newWidth) / width;
        //float scaleHeight = ((float) newHeight) / height;
        if (nowW <= 0 || nowH <= 0)
            return null;
        //现有的等比缩放
        float scaleW = rect.width() * 1.0f / nowH;
        float scaleH = rect.height() * 1.0f / nowW;
        float scale = Math.min(scaleH, scaleW);
        System.out.println("#scale:" + scale);
        //反转宽高
        rect.set(0, 0, nowW * scale, nowH * scale);
        return rect;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        System.out.println("#onSizeChanged:" + w + " " + h + " old:" + oldw + " " + oldh);
    }

    /**
     * 核心绘制
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //绘制背景图
        super.onDraw(canvas);
        //绘制画笔图片
        blank.onDraw(canvas);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    /**
     * 设置绘制模式
     *
     * @param mode
     */
    public void setDrawMode(DrawMode mode) {
        if (mode != mMode) {
            mMode = mode;
            //模式切换清除路径数据
            //mEraser.reset();
            //markPath.reset();
            //切换普通模式清空
            //mAttacher.update();
            if (mode.isNormal()) {
                //blank.clearDraw();
                //mAttacher.update();
            }
        } else {
            mMode = DrawMode.normal;
            if (mAttacker != null) {
                mAttacker.update();
            }
        }
        markPath.setMode(mode);
    }

    /**
     * 清空路径数据
     */
    public void clearPath() {
        markPath.clear();
        blank.clearDraw();
        invalidate();
    }

    /**
     * 绘制path路径
     *
     * @param path 直接用于绘制的对象
     */
    public void drawPath(Path path) {
        markPath.drawMarkPath(path);
    }

    /**
     * 绘制path路径
     *
     * @param path 一步路径数据
     */
    public void drawPath(PathContainer path) {
        markPath.drawMarkPath(path);
    }

    /**
     * 绘制橡皮擦路径
     *
     * @param path
     */
    public void drawEraserPath(Path path) {
        markPath.drawEraserPath(path);
    }

    /**
     * 绘制橡皮擦路径
     *
     * @param path
     */
    public void drawEraserPath(PathContainer path) {
        markPath.drawEraserPath(path);
    }

    /**
     * 撤销
     */
    public void revert() {
        markPath.revert(getContext());
    }

    /**
     * 设置路径颜色
     *
     * @param color 画线颜色
     */
    public void setPathColor(int color) {
        markPath.setPathColor(color);
    }

    /**
     * 设置是否可以操作
     *
     * @param operation
     */
    public void setOperation(boolean operation) {
        isOperation = operation;
    }

    /**
     * 设置路径粗细
     *
     * @param pathSize 粗细，单位px
     */
    public void setPathSize(int pathSize) {
        markPath.setPathSize(pathSize);
    }

    /**
     * 处理点击事件
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isOperation) {
            return mAttacker.onTouch(v, event);
        }
        if (mMode.isEraser() || mMode.isPath()) {
            ViewProvider provider = new ViewProvider() {
                @Override
                public int getWidth() {
                    return (int) displayRectF.width();
                }

                @Override
                public int getHeight() {
                    return (int) displayRectF.height();
                }

                @Override
                public void invalidate() {

                }

                @Override
                public Resources getResources() {
                    return getResources();
                }
            };
            //放大时：画线的相对坐标偏移
            float l = -displayRectF.left;
            float t = -displayRectF.top;
            float x = l / displayRectF.width() * getWidth();
            float y = t / displayRectF.height() * getHeight();
            return markPath.onTouchEvent(event, provider, x, y);
        }
        return mAttacker.onTouch(v, event);
    }

    /**
     * 缩放回调
     *
     * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
     * @param focusX focal point X position
     * @param focusY focal point Y position
     */
    @Override
    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
        //mAttacher.getSuppMatrix(mMatrix);
        //同步矩阵处理画板上的数据用于和本VIEW图片同步变换
        //markPath.setScale(mMatrix);
        //LogX.d("#onScaleChange：" + mAttacher.getScale() + " scaleFactor:" + scaleFactor);
        if (onScaleChangeListener != null) {
            onScaleChangeListener.onScaleChange(getScale(), focusX, focusY);
        }
    }

    public Matrix getMatrix() {
        return mMatrix;
    }


    /**
     * 矩阵变换的回调，比如本VIEW变大变小
     *
     * @param rect - Rectangle displaying the Drawable's new bounds.
     */
    @Override
    public void onMatrixChanged(RectF rect) {
        displayRectF.set(rect);
        //LogX.d("#onMatrixChanged：" + rect);
        markPath.init(blank.build(getWidth(), getHeight()));
        mAttacker.getSuppMatrix(mMatrix);
        //同步矩阵处理画板上的数据用于和本VIEW图片同步变换
        markPath.setScale(mMatrix);
        markPath.refresh();
    }

    /**
     * @return 获得所有画线操作数据，不包括撤销
     */
    public ArrayList<PathContainer> getAllPath() {
        return markPath.getAllPath();
    }

    public MarkPath getMarkPath() {
        return markPath;
    }

    public float getDegree() {
        return mDegree;
    }


    public void setOnRotateListener(OnRotateListener onRotateListener) {
        this.onRotateListener = onRotateListener;
    }

    public void setOnZoomChangeListener(PhotoViewAttacker.OnScaleChangeListener onScaleChangeListener) {
        this.onScaleChangeListener = onScaleChangeListener;
    }

    /**
     * @param paths 设置画线操作数据
     */

    public void setAllPath(final ArrayList<PathContainer> paths) {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        initListener = new Runnable() {
            @Override
            public void run() {
                markPath.setAllPath(paths);
            }
        };
        if (markPath.isInit()) {
            initListener.run();
        }
    }

    /**
     * 画线监听
     */
    public static interface onDrawCompleteListener {

        /**
         * 一条完整的笔迹画完后触发
         *
         * @param points 笔迹的坐标比例数组（x，y相间），坐标比例即坐标相对图像的十万分比位置
         * @author Felix
         */
        public void onDrawComplete(String action, ArrayList<Coordinate> points);

    }

    public static interface OnRotateListener {
        public void onRotate(float degree, BlankPhotoView view);
    }
}
