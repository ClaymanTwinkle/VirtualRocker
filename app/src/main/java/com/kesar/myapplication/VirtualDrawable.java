package com.kesar.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;

public class VirtualDrawable {
    private static final String TAG = "TargetDrawable";

    private static final boolean DEBUG = false;

    public static final int[] STATE_ACTIVE = {android.R.attr.state_enabled, android.R.attr.state_active};
    public static final int[] STATE_INACTIVE = {android.R.attr.state_enabled, -android.R.attr.state_active};
    public static final int[] STATE_FOCUSED = {android.R.attr.state_enabled, android.R.attr.state_focused};

    private float mTranslationX = 0.0f;
    private float mTranslationY = 0.0f;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private float mAlpha = 1.0f;
    private Drawable mDrawable;

    private Resources mResources;

    private int[] mDrawableIds;

    /**
     * Drawable的显示图片名称（不包括主题后缀名）
     */
    private int mDrawableId;

    private int mDrawableNormalId;
    private int mDrawableActiveId;

    /**
     * Context
     */
    private Context mContext;

    public VirtualDrawable(Context ctx, int resId) {
        this.mResources = ctx.getResources();
        this.mContext = ctx;

        if (resId > 0) {
            this.mDrawable = this.mResources.getDrawable(resId);
        }
        resizeDrawables();
        setState(STATE_INACTIVE);
    }

    public VirtualDrawable(Context ctx, int resNormalId, int resActiveId) {
        this.mResources = ctx.getResources();
        this.mContext = ctx;

        if (resNormalId > 0 && resActiveId > 0) {
            this.mDrawable = new StateListDrawable();
            ((StateListDrawable) mDrawable).addState(STATE_ACTIVE, this.mResources.getDrawable(resActiveId));
            ((StateListDrawable) mDrawable).addState(STATE_INACTIVE, this.mResources.getDrawable(resNormalId));

            this.mDrawableIds = new int[]{resNormalId, resActiveId};
        }

        setState(STATE_INACTIVE);

        resizeDrawables();
    }

    public void setState(int[] state) {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            if (d.getState() != state) {
                d.setState(state);
            }
        }
    }

    /**
     * init 资源
     */
    public void initRes() {
        if (mDrawable instanceof StateListDrawable) {
            initDrawableList();
        }
        else {
            initResDrawable();
        }
    }

    private void initResDrawable() {
        int resId = mDrawableId;

        if (resId > 0 && null != mContext) {
            Drawable drawable = mContext.getResources().getDrawable(resId);
            if (null != drawable) {
                mDrawable = drawable;
                resizeDrawables();
            }
        }
    }

    public void initDrawableList() {
        if (mDrawableNormalId > 0 && mDrawableActiveId > 0) {
            mDrawable = new StateListDrawable();
            ((StateListDrawable) mDrawable).addState(STATE_ACTIVE,
                    mResources.getDrawable(mDrawableActiveId));
            ((StateListDrawable) mDrawable).addState(STATE_INACTIVE,
                    mResources.getDrawable(mDrawableNormalId));

            mDrawableIds = new int[]{mDrawableNormalId, mDrawableActiveId};
        }

        resizeDrawables();
    }

    /**
     * Returns true if the drawable is a StateListDrawable and is in the focused
     * state.
     *
     * @return
     */
    public boolean isActive() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int[] states = d.getState();
            for (int i = 0; i < states.length; i++) {
                if (states[i] == android.R.attr.state_focused) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this target is enabled. Typically an enabled target
     * contains a valid drawable in a valid state. Currently all targets with
     * valid drawables are valid.
     *
     * @return
     */
    public boolean isValid() {
        return mDrawable != null;
    }

    /**
     * Makes drawables in a StateListDrawable all the same dimensions. If not a
     * StateListDrawable, then justs sets the bounds to the intrinsic size of
     * the drawable.
     */
    private void resizeDrawables() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int maxWidth = 0;
            int maxHeight = 0;
            for (int i = 0; i < mDrawableIds.length; i++) {
                Drawable childDrawable = mResources.getDrawable(mDrawableIds[i]);
                maxWidth = Math.max(maxWidth, childDrawable.getIntrinsicWidth());
                maxHeight = Math.max(maxHeight, childDrawable.getIntrinsicHeight());
            }
            if (DEBUG)
                Log.v(TAG, "union of childDrawable rects " + d + " to: " + maxWidth + "x" + maxHeight);
            d.setBounds(0, 0, maxWidth, maxHeight);
            for (int i = 0; i < mDrawableIds.length; i++) {
                Drawable childDrawable = mResources.getDrawable(mDrawableIds[i]);
                if (DEBUG)
                    Log.v(TAG, "sizing drawable " + childDrawable + " to: " + maxWidth + "x" + maxHeight);
                childDrawable.setBounds(0, 0, maxWidth, maxHeight);
            }
        }
        else if (mDrawable != null) {
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    public void setX(float x) {
        mTranslationX = x;
    }

    public void setY(float y) {
        mTranslationY = y;
    }

    public void setScaleX(float x) {
        mScaleX = x;
    }

    public void setScaleY(float y) {
        mScaleY = y;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getX() {
        return mTranslationX;
    }

    public float getY() {
        return mTranslationY;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public int getWidth() {
        return mDrawable != null ? mDrawable.getIntrinsicWidth() : 0;
    }

    public int getHeight() {
        return mDrawable != null ? mDrawable.getIntrinsicHeight() : 0;
    }

    public void draw(Canvas canvas) {
        if (mDrawable == null) {
            return;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(mTranslationX, mTranslationY);
        canvas.scale(mScaleX, mScaleY);
        canvas.translate(-0.5f * getWidth(), -0.5f * getHeight());
        mDrawable.setAlpha(Math.round(mAlpha * 255f));
        mDrawable.draw(canvas);
        canvas.restore();
    }
}
