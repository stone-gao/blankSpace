package com.gl.blankspaceview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.gl.blankspaceview.widget.draw.CoverPlate;
import com.gl.blankspaceview.widget.draw.PhotoViewBlank;


/**
 * @author gl
 * @desc 可调整大小的遮盖层View
 */

public class SunshadeView extends View {

    private PhotoViewBlank blank;

    //遮板
    private CoverPlate coverPlate;

    private CoverPlate.OnCoverChangeLister onCoverChangeLister;

    public SunshadeView(Context context) {
        this(context, null);
    }

    public SunshadeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunshadeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        blank = new PhotoViewBlank();
        coverPlate = new CoverPlate(this);
        post(() -> {
            blank = blank.build(getWidth(), getHeight());
            coverPlate.init(blank, getWidth(), getHeight());
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!coverPlate.isClear()) {
            return coverPlate.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (View.VISIBLE == visibility) {
            coverPlate.drawCoverPlate();
        }
        coverPlate.toggle();
    }

    public void setHeight(int coverHRatio) {
        coverPlate.setHeight(coverHRatio);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //clearDraw(canvas);
        super.onDraw(canvas);
        blank.onDraw(canvas);
    }

    public void setOnCoverChangeLister(CoverPlate.OnCoverChangeLister onCoverChangeLister) {
        this.onCoverChangeLister = onCoverChangeLister;
        coverPlate.setOnCoverChangeLister(onCoverChangeLister);
    }
}
