package com.warchaser.glsurfaceviewdev.util;

import android.content.Context;

import com.warchaser.glsurfaceviewdev.base.App;

public class StringUtils {

    public static String getFormatString(int resId, Object ... objects){
        return String.format(getContext().getString(resId), objects);
    }

    public static String getFormatString(String format, Object ... objects){
        return String.format(format, objects);
    }

    private static Context getContext(){
        return App.getInstance().getApplicationContext();
    }

}
