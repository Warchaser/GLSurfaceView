package com.warchaser.glsurfaceviewdev;

import android.view.MotionEvent;
import android.widget.ImageView;

public class GestureUtil {

    private GestureUtil(){

    }

    /**
     * 获得触摸点索引类型
     * */
    public static int getPointerIndex(int action){
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

    public static boolean hasDrawable(ImageView imageView){
        return imageView != null && imageView.getDrawable() != null;
    }

}
