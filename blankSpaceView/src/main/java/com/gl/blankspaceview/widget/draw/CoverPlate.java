package com.gl.blankspaceview.widget.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.gl.blankspaceview.R;


/**
 * @author gl
 * @desc 遮板
 */

public class CoverPlate {

    /**
     * 屏幕宽
     */
    private int screenW;
    /**
     * 屏幕高
     */
    private int screenH;
    /**
     * 距离左边距离
     */
    public int left;
    /**
     * 距离顶部距离
     */
    public int top;
    /**
     * 距离右边距离
     */
    public int right;
    /**
     * 距离底部距离
     */
    public int bottom;
    /**
     * Paint
     */
    private Paint paint;
    /**
     * 遮板宽
     */
    private int width;
    /**
     * 遮板高
     */
    private int height;
    /**
     * Canvas
     */
    private Canvas mCanvas;
    /**
     * 是否清除
     */
    private boolean isClear;
    /**
     * 画板
     */
    private PhotoViewBlank mBlank;
    /**
     * 遮板的drawable
     */
    private Drawable mDrawable;
    /**
     * 遮板改变监听
     */
    private OnCoverChangeLister onCoverChangeLister;
    /**
     * 遮板view
     */
    private View mView;
    /**
     * 是否可以操作
     */
    private boolean isOperation = true;

    public CoverPlate(View view) {
        initPaint();
        mView = view;
        mDrawable = view.getResources().getDrawable(R.drawable.cover_plate_rect);
    }

    public void init(PhotoViewBlank blank, int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.width = screenW;//修改宽度为屏幕宽度
        this.height = screenH;

        this.left = 0;
        this.top = screenH / 2;
        this.right = screenW;
        this.bottom = screenH;
        this.mCanvas = blank.getCanvas();
        this.mBlank = blank;
    }

    private void initPaint() {
        paint = new Paint();
        if (isOperation) {
            paint.setColor(Color.parseColor("#88000000"));
        } else {
            paint.setColor(Color.BLACK);
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    /**
     * 绘制遮板
     */
    public void drawCoverPlate() {
        if (mCanvas != null && !isClear) {
            mBlank.clearDraw();
            revise();//修正坐标，让它不超出屏幕范围
            //  绘制一个矩形
            mCanvas.drawRect(left, top + 6, right, bottom - 6, paint);
            mDrawable.setBounds(left, top, right, bottom);
            mDrawable.draw(mCanvas);
        }
    }

    /**
     * 修正坐标
     */
    private void revise() {
        if (right <= this.width) {//left
            right = this.width;
        }
        if (left >= (screenW - this.width)) {//right
            left = (screenW - this.width);
        }
     /*   if (top >= (screenH - this.height)) {//bottom
            top = (screenH - this.height);
        }*/
        if (bottom <= this.height) {//tops
            bottom = this.height;
        }
    }

    /**
     * 处理点击事件
     *
     * @param event
     * @return 是否处理点击事件
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (!isOperation) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawCoverPlate();
                // 按下
                break;
            case MotionEvent.ACTION_MOVE:
                // 移动
                if (this.width != 0 && this.height != 0) {
                    //  int currentX = (int) event.getX();
                    int currentY = (int) event.getY();
                    // this.right = currentX + this.width;
                    this.left = this.right - this.width;
                    this.top = currentY;
                    this.bottom = this.height;
                    drawCoverPlate();
                }

                break;
            case MotionEvent.ACTION_UP:
                // 抬起
                toggle();
                break;
        }
        // 通知重绘
        mView.invalidate();
        return true;
    }

    /**
     * 转换
     */
    public void toggle() {
        if (null != onCoverChangeLister) {
            float coverH = top;
            int scale = (int) ((coverH / height) * MyPathUtils.PATH_SCALE);
            onCoverChangeLister.onChange(scale);
        }
    }

    /**
     * @param coverHRatio 设置高度
     */
    public void setHeight(int coverHRatio) {
        int coverH = (int) (((float) coverHRatio) * height / MyPathUtils.PATH_SCALE);
        top = coverH;
        mView.invalidate();
    }

    /**
     * 设置TOP
     *
     * @param currentY
     */
    public void setCoverPlate(int currentY) {
        this.top = currentY;
        drawCoverPlate();
    }

    public void setOperation(boolean operation) {
        isOperation = operation;
    }

    //清除View
    public void clearView() {
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
        this.width = 0;
        this.height = 0;
        isClear = true;
        // 通知重绘
        mView.invalidate();
    }

    public boolean isClear() {
        return isClear;
    }

    //重置View
    public void resetView(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;
        // 通知重绘
        mView.invalidate();
    }

    public void initView(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;
        // 通知重绘
        mView.invalidate();
    }

    /**
     * 遮板改变区域监听
     */
    public static interface OnCoverChangeLister {
        void onChange(int height);
    }

    public void setOnCoverChangeLister(OnCoverChangeLister onCoverChangeLister) {
        this.onCoverChangeLister = onCoverChangeLister;
    }
}
