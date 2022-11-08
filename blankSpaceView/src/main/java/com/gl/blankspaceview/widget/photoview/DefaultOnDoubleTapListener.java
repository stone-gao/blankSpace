package com.gl.blankspaceview.widget.photoview;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Provided default implementation of GestureDetector.OnDoubleTapListener, to be overridden with custom behavior, if needed
 * <p>&nbsp;</p>
 * To be used via {@link PhotoViewAttacker#setOnDoubleTapListener(GestureDetector.OnDoubleTapListener)}
 */
public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

    private PhotoViewAttacker mPhotoViewAttacker;

    /**
     * Default constructor
     *
     * @param photoViewAttacker PhotoViewAttacher to bind to
     */
    public DefaultOnDoubleTapListener(PhotoViewAttacker photoViewAttacker) {
        setPhotoViewAttacher(photoViewAttacker);
    }

    /**
     * Allows to change PhotoViewAttacher within range of single instance
     *
     * @param newPhotoViewAttacker PhotoViewAttacher to bind to
     */
    public void setPhotoViewAttacher(PhotoViewAttacker newPhotoViewAttacker) {
        this.mPhotoViewAttacker = newPhotoViewAttacker;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (this.mPhotoViewAttacker == null)
            return false;

        ImageView imageView = mPhotoViewAttacker.getImageView();

        if (null != mPhotoViewAttacker.getOnPhotoTapListener()) {
            final RectF displayRect = mPhotoViewAttacker.getDisplayRect();

            if (null != displayRect) {
                final float x = e.getX(), y = e.getY();

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    float xResult = (x - displayRect.left)
                            / displayRect.width();
                    float yResult = (y - displayRect.top)
                            / displayRect.height();

                    mPhotoViewAttacker.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
                    return true;
                }else{
                    mPhotoViewAttacker.getOnPhotoTapListener().onOutsidePhotoTap();
                }
            }
        }
        if (null != mPhotoViewAttacker.getOnViewTapListener()) {
            mPhotoViewAttacker.getOnViewTapListener().onViewTap(imageView, e.getX(), e.getY());
        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent ev) {
        if (mPhotoViewAttacker == null)
            return false;

        try {
            float scale = mPhotoViewAttacker.getScale();
            float x = ev.getX();
            float y = ev.getY();

            if (scale < mPhotoViewAttacker.getMediumScale()) {
                mPhotoViewAttacker.setScale(mPhotoViewAttacker.getMediumScale(), x, y, true);
            } else if (scale >= mPhotoViewAttacker.getMediumScale() && scale < mPhotoViewAttacker.getMaximumScale()) {
                mPhotoViewAttacker.setScale(mPhotoViewAttacker.getMaximumScale(), x, y, true);
            } else {
                mPhotoViewAttacker.setScale(mPhotoViewAttacker.getMinimumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }

}
