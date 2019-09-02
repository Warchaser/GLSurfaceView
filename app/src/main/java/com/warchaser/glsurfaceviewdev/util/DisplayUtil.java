package com.warchaser.glsurfaceviewdev.util;

import android.graphics.Bitmap;

import com.warchaser.glsurfaceviewdev.base.App;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class DisplayUtil {

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        final float scale = App.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height){
        final Mat src = new Mat();
        final Mat dst = new Mat();
        Utils.bitmapToMat(bitmap, src);

        Imgproc.resize(src, dst, new Size(width, height), 0, 0, Imgproc.INTER_AREA);
        final Bitmap bitmap1 = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(dst, bitmap1);
        return bitmap1;
    }

}
