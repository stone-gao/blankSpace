package com.gl.blankspaceview.widget.draw;

import android.content.res.Resources;

/**
 * @author gl
 * @desc 视图提供，用于获取宽高等，确定绘图画板宽高等属性
 */
public interface ViewProvider {
    int getWidth();
    int getHeight();
    void invalidate();
    Resources getResources();
}
