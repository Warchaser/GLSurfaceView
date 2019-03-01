package com.warchaser.glsurfaceviewdev.util;

import com.warchaser.glsurfaceviewdev.base.App;

public class DisplayUtil {

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        final float scale = App.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
