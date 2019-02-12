package com.warchaser.glsurfaceviewdev;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class PhotoView extends android.support.v7.widget.AppCompatImageView {

    private PhotoViewAttacher mAttacher;

    public PhotoView(Context context) {
        super(context);
        init();
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mAttacher = new PhotoViewAttacher(this);
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if(isAttacherNotNull()){
            mAttacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if(isAttacherNotNull()){
            mAttacher.update();
        }
    }

    private boolean isAttacherNotNull(){
        return mAttacher != null;
    }
}
