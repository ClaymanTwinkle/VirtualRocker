package com.kesar.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 *
 * Created by chenqitian on 2016/12/27.
 */
public class RockerView extends View {

    private double mWaveCenterX;
    private double mWaveCenterY;
    private double mTapRadius;
    private double mOuterRadius = 0.0f;

    private VirtualDrawable mHandleDrawable;
    private VirtualDrawable mOuterDrawable;

    public RockerView(Context context) {
        super(context);
        init();
    }

    public RockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RockerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RockerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mHandleDrawable = new VirtualDrawable(getContext(), R.drawable.gamepad_hand_rocker_ball);
        mOuterDrawable = new VirtualDrawable(getContext(), R.drawable.gamepad_hand_rocker_bg);

        mOuterRadius = getContext().getResources().getDimensionPixelSize(R.dimen.gamepad_joystick_outerRadius);
        mTapRadius = mHandleDrawable.getWidth() / 2;

    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return mOuterDrawable.getWidth();
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return mOuterDrawable.getHeight();
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int width = right - left;
        final int height = bottom - top;
        double newWaveCenterX = Math.max(width, mOuterDrawable.getWidth()) / 2;
        double newWaveCenterY = Math.max(height, mOuterDrawable.getHeight()) / 2;
        if (newWaveCenterX != mWaveCenterX || newWaveCenterY != mWaveCenterY) {
            if (mWaveCenterX == 0 && mWaveCenterY == 0) {
                if (mOuterRadius == 0.0f) {
                    mOuterRadius = 0.5f * (Math.hypot(newWaveCenterX, newWaveCenterY));
                }
                moveHandleTo(newWaveCenterX, newWaveCenterY);
            }
            mWaveCenterX = newWaveCenterX;
            mWaveCenterY = newWaveCenterY;

            mOuterDrawable.setX((float) mWaveCenterX);
            mOuterDrawable.setY((float) mWaveCenterY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mOuterDrawable.draw(canvas);
        mHandleDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandleDrawable.setState(VirtualDrawable.STATE_ACTIVE);
            case MotionEvent.ACTION_MOVE:
                final int historySize = event.getHistorySize();
                for (int k = 0; k < historySize + 1; k++) {
                    double x = k < historySize ? event.getHistoricalX(k) : event.getX();
                    double y = k < historySize ? event.getHistoricalY(k) : event.getY();
                    double tx = x - mWaveCenterX;
                    double ty = y - mWaveCenterY;
                    double touchRadius = Math.hypot(tx, ty);
                    final double scale = touchRadius > mOuterRadius ? mOuterRadius / touchRadius : 1.0f;
                    double limitX = mWaveCenterX + tx * scale;
                    double limitY = mWaveCenterY + ty * scale;
                    x = limitX;
                    y = limitY;
                    moveHandleTo(x, y);
                }
                double x = mHandleDrawable.getX();
                double y = mHandleDrawable.getY();
                double degree = calcDegrees(mWaveCenterX, mWaveCenterY, x, y);
                if (degree <= 45 && degree > -45) {
                    System.err.println("右");
                }
                else if (degree > 45 && degree <= 135) {
                    System.err.println("下");
                }
                else if (degree <= -45 && degree > -135) {
                    System.err.println("上");
                }
                else {
                    System.err.println("左");
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                System.err.println("ACTION_UP");
                moveHandleTo(mWaveCenterX, mWaveCenterY);
                mHandleDrawable.setState(VirtualDrawable.STATE_INACTIVE);
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    private void moveHandleTo(double x, double y) {
        mHandleDrawable.setX((float) x);
        mHandleDrawable.setY((float) y);
    }

    /**
     * 计算两点的角度
     */
    public double calcDegrees(double originX, double originY, double targetX, double targetY) {
        //得到两点X的距离
        double x = targetX - originX;
        //得到两点Y的距离
        double y = originY - targetY;
        //算出斜边长
        double xie = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        //得到这个角度的余弦值（通过三角函数中的定理 ：邻边/斜边=角度余弦值）
        double cosAngle = x / xie;
        //通过反余弦定理获取到其角度的弧度
        double rad = Math.acos(cosAngle);
        //注意：当触屏的位置Y坐标<摇杆的Y坐标我们要取反值-0~-180
        if (targetY < originY) {
            rad = -rad;
        }
        return Math.toDegrees(rad);
    }
}
