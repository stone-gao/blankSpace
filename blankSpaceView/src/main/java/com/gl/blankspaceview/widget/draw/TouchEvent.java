package com.gl.blankspaceview.widget.draw;

/**
 * @author gl
 * @desc
 */

public class TouchEvent {
    private float x;
    private float y;

    public TouchEvent(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
