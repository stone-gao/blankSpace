/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.gl.blankspaceview.widget.photoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;

import androidx.appcompat.widget.AppCompatImageView;


public class PhotoView extends AppCompatImageView implements IPhotoView {

    protected PhotoViewAttacker mAttacker;

    private ScaleType mPendingScaleType;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setScaleType(ScaleType.MATRIX);
        init();
    }

    protected void init() {
        if (null == mAttacker || null == mAttacker.getImageView()) {
            mAttacker = new PhotoViewAttacker(this);
        }

        if (null != mPendingScaleType) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }
    }

    @Override
    public void setRotationTo(float rotationDegree) {
        mAttacker.setRotationTo(rotationDegree);
    }

    @Override
    public void setRotationBy(float rotationDegree) {
        mAttacker.setRotationBy(rotationDegree);
    }

    @Override
    public boolean canZoom() {
        return mAttacker.canZoom();
    }

    @Override
    public RectF getDisplayRect() {
        return mAttacker.getDisplayRect();
    }

    @Override
    public void getDisplayMatrix(Matrix matrix) {
        mAttacker.getDisplayMatrix(matrix);
    }

    @Override
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return mAttacker.setDisplayMatrix(finalRectangle);
    }

    @Override
    public float getMinimumScale() {
        return mAttacker.getMinimumScale();
    }

    @Override
    public float getMediumScale() {
        return mAttacker.getMediumScale();
    }

    @Override
    public float getMaximumScale() {
        return mAttacker.getMaximumScale();
    }

    @Override
    public float getScale() {
        return mAttacker.getScale();
    }

    @Override
    public ScaleType getScaleType() {
        return mAttacker.getScaleType();
    }

    @Override
    public Matrix getImageMatrix() {
        return mAttacker.getImageMatrix();
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacker.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mAttacker.setMinimumScale(minimumScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mAttacker.setMediumScale(mediumScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mAttacker.setMaximumScale(maximumScale);
    }

    @Override
    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        mAttacker.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void postDrag(final float dx, final float dy) {
        mAttacker.postDrag(dx, dy);
    }


    public void setFlip(int velocityX, int velocityY) {
        mAttacker.postFlip(velocityX, velocityY);
    }


    public float[] getDragDelta() {
        return mAttacker.getDragDelta();
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (null != mAttacker) {
            mAttacker.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (null != mAttacker) {
            mAttacker.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (null != mAttacker) {
            mAttacker.update();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (null != mAttacker) {
            mAttacker.update();
        }
        return changed;
    }

    @Override
    public void setOnMatrixChangeListener(PhotoViewAttacker.OnMatrixChangedListener listener) {
        mAttacker.setOnMatrixChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mAttacker.setOnLongClickListener(l);
    }

    @Override
    public void setOnPhotoTapListener(PhotoViewAttacker.OnPhotoTapListener listener) {
        mAttacker.setOnPhotoTapListener(listener);
    }

    @Override
    public void setOnViewTapListener(PhotoViewAttacker.OnViewTapListener listener) {
        mAttacker.setOnViewTapListener(listener);
    }

    @Override
    public void setScale(float scale) {
        mAttacker.setScale(scale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mAttacker.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacker.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (null != mAttacker) {
            mAttacker.setScaleType(scaleType);
        } else {
            mPendingScaleType = scaleType;
        }
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mAttacker.setZoomable(zoomable);
    }

    @Override
    public Bitmap getVisibleRectangleBitmap() {
        return mAttacker.getVisibleRectangleBitmap();
    }

    @Override
    public void setZoomTransitionDuration(int milliseconds) {
        mAttacker.setZoomTransitionDuration(milliseconds);
    }

    @Override
    public IPhotoView getIPhotoViewImplementation() {
        return mAttacker;
    }

    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        mAttacker.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    @Override
    public void setOnScaleChangeListener(PhotoViewAttacker.OnScaleChangeListener onScaleChangeListener) {
        mAttacker.setOnScaleChangeListener(onScaleChangeListener);
    }

    public void setOnDragChangeListener(PhotoViewAttacker.OnDragChangeListener onDragChangeListener) {
        mAttacker.setOnDragChangeListener(onDragChangeListener);
    }

    @Override
    public void setOnSingleFlingListener(PhotoViewAttacker.OnSingleFlingListener onSingleFlingListener) {
        mAttacker.setOnSingleFlingListener(onSingleFlingListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacker.cleanup();
        mAttacker = null;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }
}
