package com.gl.blankspaceview.widget.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;


/**
 * @author gl
 * @desc 画板
 */

public class PhotoViewBlank {
    /**
     * Paint
     */
    private Paint clipPaint = new Paint();
    /**
     * 变换矩阵
     */
    private Matrix matrix;
    /**
     * 画板对应的Bitmap
     */
    private Bitmap mBitmap;
    /**
     * Canvas
     */
    private Canvas mCanvas;
    /**
     * 画板的宽
     */
    private int mWidth;
    /**
     * 画板的高
     */
    private int mHeight;
    /**
     * 画板背景，一般用不上
     */
    private Bitmap bg;

    public PhotoViewBlank() {
        clipPaint.setAntiAlias(true);
        clipPaint.setDither(true);
        clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    public PhotoViewBlank build(int width, int height) {
        build(null, width, height);
        return this;
    }

    public PhotoViewBlank build(Bitmap bitmap, int width, int height) {
        if (width == 0 || height == 0)
            return null;
        if (destroy(width, height)) {
            mWidth = width;
            mHeight = height;
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawColor(Color.TRANSPARENT);
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            bg = bitmap;
        }
        return this;
    }

    /**
     * @return 画板是否绘制过数据
     */
    public boolean isDirty() {
        return mBitmap != null;
    }


    /**
     * 在对应的canvas上绘制画板数据
     *
     * @param canvas 一般为view的canvas
     */
    public void onDraw(Canvas canvas) {
        canvas.save();
        if (mBitmap != null) {
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            if (matrix != null) {
                canvas.drawBitmap(mBitmap, matrix, clipPaint);
            } else {
                canvas.drawBitmap(mBitmap, 0, 0, clipPaint);
            }
        }
        canvas.restore();
    }

    public void drawMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    /**
     * 清空画板
     */
    public void clearDraw() {
        mCanvas.drawColor(Color.TRANSPARENT);
        Paint clipPaint = new Paint();
        clipPaint.setAntiAlias(true);
        clipPaint.setDither(true);
        clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(clipPaint);
    }

    /**
     * 绘制背景
     */
    public void drawBgBitmap() {
        if (bg != null) {
            mCanvas.drawBitmap(bg, 0, 0, clipPaint);
        }
    }

    /**
     * 销毁画板数据
     *
     * @param width 最新画板宽
     * @param height 最新画板高
     * @return 是否销毁
     */
    public boolean destroy(int width, int height) {
        //不销毁
        if (mWidth == width && mHeight == height)
            return false;
        //销毁
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mCanvas = null;
        return true;
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}
