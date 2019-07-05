package com.warchaser.titlebar.util;

import android.content.Context;
import androidx.core.content.ContextCompat;

final public class DrawableUtil {

//    /**
//     * 获取颜色id
//     */
//    public static int getContextColor(int resId) {
//        return ContextCompat.getColor(App.getInstance().getApplicationContext(), resId);
//    }

    public static int getContextColor(int resId, Context context){
        return ContextCompat.getColor(context, resId);
    }

}
