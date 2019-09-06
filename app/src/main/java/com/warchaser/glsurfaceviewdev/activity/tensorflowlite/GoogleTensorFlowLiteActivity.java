package com.warchaser.glsurfaceviewdev.activity.tensorflowlite;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Size;

import com.warchaser.glsurfaceviewdev.R;
import com.warchaser.glsurfaceviewdev.util.ImageReaderUtils;

import org.jetbrains.annotations.NotNull;

public class GoogleTensorFlowLiteActivity extends GoogleCameraAbstractActivity {

    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private Bitmap mRGBFrameBitmap = null;
    private Bitmap mCroppedBitmap = null;

    private Matrix mFrameToCropTransform;
    private Matrix mCropToFrameTransform;

    private Integer mSensorOrientation;

    @Override
    protected void processImage() {
        mRGBFrameBitmap.setPixels(
                getRGBBytes(),
                0,
                mPreviewWidth,
                0,
                0,
                mPreviewWidth,
                mPreviewHeight
        );

        final Canvas canvas = new Canvas(mCroppedBitmap);
        canvas.drawBitmap(mRGBFrameBitmap, mFrameToCropTransform, null);


    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_camera2_basic;
    }

    @Override
    protected int getTextureViewId() {
        return R.id.texture;
    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.activity_camera;
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    protected void onPreviewSizeChosen(@NotNull Size size, int rotation) {
        mPreviewWidth = size.getWidth();
        mPreviewHeight = size.getHeight();

        mSensorOrientation = rotation - getScreenOrientation();

        mRGBFrameBitmap = Bitmap.createBitmap(mPreviewWidth, mPreviewHeight, Config.ARGB_8888);
        mCroppedBitmap = Bitmap.createBitmap(224, 224, Config.ARGB_8888);

        mFrameToCropTransform = ImageReaderUtils.getTransformationMatrix(
                mPreviewWidth,
                mPreviewHeight,
                224,
                224,
                mSensorOrientation,
                MAINTAIN_ASPECT
        );

        mCropToFrameTransform = new Matrix();
        mFrameToCropTransform.invert(mCropToFrameTransform);

    }

    @NotNull
    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }
}
