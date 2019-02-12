package com.warchaser.glsurfaceviewdev.view.photo;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.warchaser.glsurfaceviewdev.util.NLog;

/**
 * MSCALE_X, MSKEW_X, MTRANS_X
 *
 * MSKEW_Y, MSCALE_Y, MTRANS_Y
 *
 * MPERSP_0, MPERSP_1, MPERSP_2
 * */

public class PhotoViewAttacher implements View.OnTouchListener, View.OnLayoutChangeListener {

    private static float DEFAULT_MAX_SCALE = 3.0f;
    private static float DEFAULT_MID_SCALE = 1.75f;
    private static float DEFAULT_MIN_SCALE = 1.0f;

    private static final int DEFAULT_ZOOM_DURATION = 200;

    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;

    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;

    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMinScale = DEFAULT_MIN_SCALE;

    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

    private GestureDetector mGestureDetector;
    private CustomGestureDetector mCustomeGestureDetector;

    private ImageView mImageView;

    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSupportMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();

    private final float[] mMatrixValues = new float[9];

    private float mBaseRotation = 0.0f;

    private int mZoomDuration = DEFAULT_ZOOM_DURATION;

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public PhotoViewAttacher(ImageView imageView){
        mImageView = imageView;
        imageView.setOnTouchListener(this);
        imageView.addOnLayoutChangeListener(this);

        if(imageView.isInEditMode()){
            return;
        }

        initialize();
    }

    private void initialize(){

        mCustomeGestureDetector = new CustomGestureDetector(mImageView.getContext(), mCustomGestureListener);

        mGestureDetector = new GestureDetector(mImageView.getContext(), new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDown(MotionEvent e) {
                //没有onDown并返回true
                //GestureDetector.onTouchEvent始终返回false 导致DoubleTap不响应
                return true;
            }
        });

        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                try {
                    final float scale = getScale();
                    final float x = e.getX();
                    final float y = e.getY();
                    if(scale < getMidScale()){
                        setScale(getMidScale(), x, y, true);
                    } else if(scale >= getMidScale() && scale < getMaxScale()){
                        setScale(getMaxScale(), x, y, true);
                    } else {
                        setScale(getMinScale(), x, y, true);
                    }
                } catch (Exception | Error er){
                    NLog.printStackTrace("onDoubleTap", er);
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if(left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom){
            updateBaseMatrix(mImageView.getDrawable());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean handled = false;

        if(GestureUtil.hasDrawable((ImageView)v)){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ViewParent parent = v.getParent();
                    if(parent != null){
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if(getScale() < getMinScale()){
                        RectF rectF = getDisplayRectF();
                        if(rectF != null){
                            handled = true;
                        }
                    } else if(getScale() > getMaxScale()){
                        RectF rectF = getDisplayRectF();
                        if(rectF != null){
                            handled = true;
                        }
                    }
                    break;
                default:
                    break;
            }

            if(mCustomeGestureDetector != null){
                boolean isScaling = mCustomeGestureDetector.isScaling();
                boolean isDragging = mCustomeGestureDetector.isDragging();
                handled = mCustomeGestureDetector.onTouchEvent(event);
                boolean canScale = !isScaling && !mCustomeGestureDetector.isScaling();
                boolean canDrag = !isDragging && !mCustomeGestureDetector.isDragging();
                mBlockParentIntercept = canDrag && canScale;
            }

            //此处判断用户是否是双击行为
            if(mGestureDetector != null && mGestureDetector.onTouchEvent(event)){
                handled = true;
            }
        }

        return handled;
    }

    /**
     * 求Matrix.MSCALE_X(X轴缩放比)与Matrix.MSKEW_Y(Y轴旋转角度)对应的勾股值
     * 即缩放比
     * */
    public float getScale(){
        final float op1 = (float) Math.pow(getValue(mSupportMatrix, Matrix.MSCALE_X), 2);
        final float op2 = (float) Math.pow(getValue(mSupportMatrix, Matrix.MSKEW_Y), 2);
        return (float) Math.sqrt(op1 + op2);
     }

    /**
     * 把Matrix的9个点阵数组拷贝到mMatrixValues数组中
     * 并将whichValue对应的元素返回
     * */
    private float getValue(Matrix matrix, int whichValue){
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    public void setScale(float scale, float focalX, float focalY, boolean isAnimated){
        if(scale < mMinScale || scale > mMaxScale){

        }

        if(isAnimated){
            mImageView.post(new AnimatedZoomRunnable(getScale(), scale, focalX, focalY));
        } else{
            mSupportMatrix.setScale(scale, scale, focalX, focalY);
            checkAndDisplayMatrix();
        }

    }

    /**
     * 简单检查一下Matrix的边界
     * 并执行ImageView的Matrix变换
     * */
    private void checkAndDisplayMatrix(){
        if(checkMatrixBounds()){
            setImageViewMatrix(getDrawMatrix());
        }
    }

    private boolean checkMatrixBounds(){
        //精度更高的矩形构造(Float)
        final RectF rectF = getDisplayRect(getDrawMatrix());
        if(rectF == null){
            return false;
        }
        final float height = rectF.height();
        final float width = rectF.width();
        float deltaX = 0;
        float deltaY = 0;
        final int imageViewHeight = getImageViewHeight();
        final int imageViewWidth = getImageViewWidth();
        if(height <= imageViewHeight){
            switch (mScaleType){
                case FIT_START:
                    deltaY = -rectF.top;
                    break;
                case FIT_END:
                    deltaY = imageViewHeight - height - rectF.top;
                    break;
                default:
                    deltaY = (imageViewHeight - height) / 2 - rectF.top;
                    break;
            }
            setVerticalScrollEdge(VERTICAL_EDGE_BOTH);
        } else if(rectF.top > 0){
            setVerticalScrollEdge(VERTICAL_EDGE_TOP);
            deltaY = -rectF.top;
        } else if(rectF.bottom < imageViewHeight){
            setVerticalScrollEdge(VERTICAL_EDGE_BOTTOM);
            deltaY = imageViewHeight - rectF.bottom;
        } else {
            setVerticalScrollEdge(VERTICAL_EDGE_NONE);
        }

        if(width <= imageViewWidth){
            switch (mScaleType){
                case FIT_START:
                    deltaX = -rectF.left;
                    break;
                case FIT_END:
                    deltaX = imageViewWidth - width - rectF.left;
                    break;
                default:
                    deltaX = (imageViewWidth - width) / 2 - rectF.left;
                    break;
            }
            setHorizontalScrollEdge(HORIZONTAL_EDGE_BOTH);
        } else if(rectF.left > 0){
            setHorizontalScrollEdge(HORIZONTAL_EDGE_LEFT);
            deltaX = -rectF.left;
        } else if(rectF.right < imageViewWidth){
            setHorizontalScrollEdge(HORIZONTAL_EDGE_RIGHT);
            deltaX = imageViewWidth - rectF.right;
        } else {
            setHorizontalScrollEdge(HORIZONTAL_EDGE_NONE);
        }

        //最后执行Matrix(延后移位)
        mSupportMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    public RectF getDisplayRectF(){
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    /**
     * 获取ImageView的Drawable矩形对象
     * */
    private RectF getDisplayRect(Matrix matrix){
        Drawable drawable = mImageView.getDrawable();
        if(drawable != null){
            mDisplayRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);

            return mDisplayRect;
        }

        return null;
    }

    private Matrix getDrawMatrix(){
        mDrawMatrix.set(mBaseMatrix);
        //左乘矩阵
        mDrawMatrix.postConcat(mSupportMatrix);
        return mDrawMatrix;
    }

    private void setImageViewMatrix(Matrix matrix){
        mImageView.setImageMatrix(matrix);
    }

    private int getImageViewWidth(){
        return mImageView.getWidth() - mImageView.getPaddingLeft() - mImageView.getPaddingRight();
    }

    private int getImageViewHeight(){
        return mImageView.getHeight() - mImageView.getPaddingTop() - mImageView.getPaddingBottom();
    }

    private float getMaxScale(){
        return mMaxScale;
    }

    private float getMidScale(){
        return mMidScale;
    }

    private float getMinScale(){
        return mMinScale;
    }

    public void update(){
        updateBaseMatrix(mImageView.getDrawable());
    }

    private void updateBaseMatrix(Drawable drawable){
        if(drawable == null){
            return;
        }

        final float imageViewWidth = getImageViewWidth();
        final float imageViewHeight = getImageViewHeight();
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        mBaseMatrix.reset();
        final float widthScale = imageViewWidth / drawableWidth;
        final float heightScale = imageViewHeight / drawableHeight;
        switch (mScaleType){
            case CENTER:
                mBaseMatrix.postTranslate((imageViewWidth - drawableWidth) / 2f, (imageViewHeight - drawableHeight) / 2f);
                break;
            case CENTER_CROP:{
                final float scale = Math.max(widthScale, heightScale);
                mBaseMatrix.postScale(scale, scale);
                mBaseMatrix.postTranslate((imageViewWidth - drawableWidth * scale) / 2f, (imageViewHeight - drawableHeight * scale) / 2f);
            }
                break;
            case CENTER_INSIDE:{
                final float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
                mBaseMatrix.postScale(scale, scale);
                mBaseMatrix.postTranslate((imageViewWidth - drawableWidth * scale) / 2f, (imageViewHeight - drawableHeight * scale) / 2f);
            }
                break;
            default:{

                RectF tempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
                RectF tempDst = new RectF(0, 0, imageViewWidth, imageViewHeight);

                if((int) mBaseRotation % 180 != 0){
                    tempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
                }

                switch (mScaleType){
                    case FIT_CENTER:
                        mBaseMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER);
                        break;
                    case FIT_START:
                        mBaseMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.START);
                        break;
                    case FIT_END:
                        mBaseMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.END);
                        break;
                    case FIT_XY:
                        mBaseMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.FILL);
                        break;
                    default:
                        break;
                }
            }
                break;
        }

        resetMatrix();
    }

    public void setRotationBy(float degree){
        mSupportMatrix.postRotate(degree % 360);
        checkAndDisplayMatrix();
    }

    private void resetMatrix(){
        mSupportMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private CustomGestureListener mCustomGestureListener = new CustomGestureListener() {
        @Override
        public void onDrag(float deltaX, float deltaY) {
            if(mCustomeGestureDetector != null && mCustomeGestureDetector.isScaling()){
                return;
            }

            mSupportMatrix.postTranslate(deltaX, deltaY);
            checkAndDisplayMatrix();

            disallowParentTouchEvent(deltaX, deltaY);
        }

        @Override
        public void onFling(float startC, float startY, float velocityX, float velocityY) {

        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            if(getScale() < getMaxScale() || scaleFactor < 1f){
                mSupportMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                checkAndDisplayMatrix();
            }
        }
    };

    /**
     * 确定是否需要屏蔽ImageView父控件的Touch事件
     *
     * */
    private void disallowParentTouchEvent(float deltaX, float deltaY){
        ViewParent parent = mImageView.getParent();

        if(parent == null){
            return;
        }

        if(mAllowParentInterceptOnEdge && mCustomeGestureDetector != null && !mCustomeGestureDetector.isScaling() && !mBlockParentIntercept){
            if(mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                || (mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT && deltaX >= 1f)
                || (mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && deltaX <=-1f)
                || (mVerticalScrollEdge == VERTICAL_EDGE_TOP && deltaY >= 1f)
                || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && deltaY <= -1f)){
                parent.requestDisallowInterceptTouchEvent(false);
            }
        } else {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void setVerticalScrollEdge(int type){
        this.mVerticalScrollEdge = type;
    }

    private void setHorizontalScrollEdge(int type){
        this.mHorizontalScrollEdge = type;
    }

    private class AnimatedZoomRunnable implements Runnable{

        private final float mFocalX;
        private final float mFocalY;
        private final long mStartTime;
        private final float mZoomStart;
        private final float mZoomEnd;

        AnimatedZoomRunnable(final float currentZoom,
                             final float targetZoom,
                             final float focalX,
                             final float focalY){
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            final float t = interpolate();
            final float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            final float deltaScale = scale / getScale();
            mCustomGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            if(t < 1f){
                AnimationCompat.postOnAnimation(mImageView, this);
            }
        }

        private float interpolate(){
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

}
