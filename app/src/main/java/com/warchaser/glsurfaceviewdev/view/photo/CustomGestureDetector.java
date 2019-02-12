package com.warchaser.glsurfaceviewdev.view.photo;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.warchaser.glsurfaceviewdev.util.NLog;

/**
 * 自定义缩放手势监听器
 * */
public class CustomGestureDetector {

    private static final int INVALID_POINTER_ID = -1;

    /**
     * 用跟踪手指滑动速度的方式判断是否触发Fling动作
     * */
    private VelocityTracker mVelocityTracker;

    /**
     * 最小Fling手指滑动瞬时速度
     * */
    private final float mMinimumVelocity;

    /**
     * 触发移动事件的最小距离
     * */
    private final float mTouchSlop;

    /**
     * 手势监听
     * */
    private CustomGestureListener mCustomGestureListener;

    /**
     * 是否在拖动
     * */
    private boolean mIsDragging = false;

    /**
     * 上次触摸屏幕的x坐标
     * */
    private float mLastTouchX;

    /**
     * 上次触摸屏幕的y坐标
     * */
    private float mLastTouchY;

    /**
     * 每根手指从按下、移动到离开屏幕，每个手指都会拥有一个固定PointerId.PointerId的值可以是任意的值。
     * */
    private int mActivePointerId = INVALID_POINTER_ID;

    /**
     * 每根手指从按下、移动到离开屏幕，每根手指在每一个事件的Index可能是不固定的,因为受到其它手指的影响。比如，A跟B两根手指同时按在屏幕上，此时A的PointerIndex为0，B的则为1.当A先离开屏幕时，B的PointerIndex则变为了0.
     * 但是，PointerIndex的值的不是任意的，它必须在[0，PointerCount-1]的范围内。其中PointerCount为参与触控的手指数量。
     * */
    private int mActivePointerIndex = 0;

    /**
     * 缩放手势监听器
     * */
    private final ScaleGestureDetector mScaleGestureDetector;

    public CustomGestureDetector(Context context, CustomGestureListener listener){
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();

        mCustomGestureListener = listener;

        ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                final float scaleFactor = detector.getScaleFactor();
                if(Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)){
                    return false;
                }

                if(scaleFactor >= 0){
                    if(mCustomGestureListener != null){
                        mCustomGestureListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                    }
                }

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        };

        mScaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
    }

    public boolean isDragging(){
        return mIsDragging;
    }

    private void setIsDragging(boolean isDragging){
        this.mIsDragging = isDragging;
    }

    private float getActiveX(MotionEvent ev){
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception | Error e){
            NLog.printStackTrace("CustomGestureDetector.getActiveX", e);
            return ev.getX();
        }
    }

    private float getActiveY(MotionEvent ev){
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception | Error e){
            NLog.printStackTrace("CustomGestureDetector.getActiveY", e);
            return ev.getY();
        }
    }

    /**
     * PhotoView库指出
     * 这里在onDestroy调用时会有bug,
     * 所以封装一层
     * */
    public boolean onTouchEvent(MotionEvent ev){
        try {
            mScaleGestureDetector.onTouchEvent(ev);
            return processTouchEvent(ev);
        } catch (Exception | Error e){
            NLog.printStackTrace("processTouchEvent", e);
            return true;
        }
    }

    /**
     * 处理touch事件
     * */
    private boolean processTouchEvent(MotionEvent ev){

        final int action = ev.getAction();
        //识别多点触控
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:

                mActivePointerId = ev.getPointerId(0);

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);

                mVelocityTracker = VelocityTracker.obtain();
                if(mVelocityTracker != null){
                    mVelocityTracker.addMovement(ev);
                }

                setIsDragging(false);
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                final float deltaX = x - mLastTouchX;
                final float deltaY = y - mLastTouchY;

                if(!isDragging()){
                    setIsDragging(Math.sqrt(deltaX * deltaX + deltaY * deltaY) >= mTouchSlop);
                }

                if(isDragging()){

                    if(mCustomGestureListener != null){
                        mCustomGestureListener.onDrag(deltaX, deltaY);
                    }

                    mLastTouchX = x;
                    mLastTouchY = y;
                }

                if(mVelocityTracker != null){
                    mVelocityTracker.addMovement(ev);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;

                if(isDragging()){
                    if(mVelocityTracker != null){
                        mVelocityTracker.addMovement(ev);
                        //计算上1秒中的最高瞬时速度
                        mVelocityTracker.computeCurrentVelocity(1000);
                        //x方向瞬时速度
                        final float velocityX = mVelocityTracker.getXVelocity();
                        //y方向瞬时速度
                        final float velocityY = mVelocityTracker.getYVelocity();
                        //如果大于等于最小触发Fling动作的瞬时速度，则调用mListener的onFling
                        if(Math.max(Math.abs(velocityX), Math.abs(velocityY)) >= mMinimumVelocity){

                            if(mCustomGestureListener != null){
                                mCustomGestureListener.onFling(mLastTouchX, mLastTouchY, -velocityX, -velocityY);
                            }

                        }
                    }
                }

                //无条件执行VelocityTrack的释放动作
                recycleVelocityTracker();

                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = getPointerIndex(ev.getAction());
                final int pointerId = ev.getPointerId(pointerIndex);
                if(pointerId == mActivePointerId){
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
        }

        int index = mActivePointerId != INVALID_POINTER_ID ? mActivePointerId : 0;
        mActivePointerIndex = ev.findPointerIndex(index);
        return true;
    }

    /**
     * 回收VelocityTracker
     * */
    private void recycleVelocityTracker(){
        if(mVelocityTracker != null){
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private int getPointerIndex(int action){
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

    /**
     * 是否正在缩放
     * */
    public boolean isScaling(){
        return mScaleGestureDetector.isInProgress();
    }

}
