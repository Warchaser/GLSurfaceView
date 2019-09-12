package com.warchaser.glsurfaceviewdev.activity.tensorflowlite;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Size;
import android.widget.TextView;

import com.warchaser.glsurfaceviewdev.R;
import com.warchaser.glsurfaceviewdev.tensorflow.Classifier;
import com.warchaser.glsurfaceviewdev.tensorflow.Classifier.Model;
import com.warchaser.glsurfaceviewdev.tensorflow.Classifier.Device;
import com.warchaser.glsurfaceviewdev.tensorflow.Classifier.Recognition;
import com.warchaser.glsurfaceviewdev.tensorflow.FloatClassifier;
import com.warchaser.glsurfaceviewdev.tensorflow.QuantizedClassifier;
import com.warchaser.glsurfaceviewdev.util.HandlerUtils;
import com.warchaser.glsurfaceviewdev.util.ImageReaderUtils;
import com.warchaser.glsurfaceviewdev.util.NLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class GoogleTensorFlowLiteActivity extends GoogleCameraAbstractActivity {

    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private Bitmap mRGBFrameBitmap = null;
    private Bitmap mCroppedBitmap = null;

    private Matrix mFrameToCropTransform;
    private Matrix mCropToFrameTransform;

    private Integer mSensorOrientation;

    private Model mModel = Model.QUANTIZED;
    private Device mDevice = Device.CPU;
    private int mNumOfThreads = 1;

    private Classifier mClassifier;

    private static final int MESSAGE_REFRESH_UI = 0x1001;

    private MessageHandler mMessageHandler;

    private TextView mTvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();
    }

    private void initialize(){
        mMessageHandler = new MessageHandler(this);

        mTvTitle = findViewById(R.id.mTvTitle);

        recreateClassifier(mModel, mDevice, mNumOfThreads);
    }

    private void refreshUi(Recognition recognition){

        if(recognition == null){
            return;
        }

        if(mTvTitle != null){
            mTvTitle.setText(recognition.getTitle());
        }
    }

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

        runInBackground(new Runnable() {
            @Override
            public void run() {

                if(mClassifier != null){
                    final List<Recognition> results = mClassifier.recognizeImage(mCroppedBitmap);
                    Recognition recognition1 = results.get(0);
                    Recognition recognition2 = results.get(1);
                    Recognition recognition3 = results.get(2);

                    HandlerUtils.sendMessage(mMessageHandler, MESSAGE_REFRESH_UI, recognition1, -1, -1);
                }

            }
        });

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

    private Model getModel(){
        return mModel;
    }

    private void recreateClassifier(Model model, Device device, int numOfThreads){
        if(mClassifier != null){
            mClassifier.close();
            mClassifier = null;
        }

        try {
            if (model == Model.QUANTIZED) {
                mClassifier = new QuantizedClassifier(getAssets(), device, numOfThreads);
            } else {
                mClassifier = new FloatClassifier(getAssets(), device, numOfThreads);
            }
        } catch (Exception e) {
            NLog.printStackTrace(TAG, e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMessageHandler != null){
            mMessageHandler.removeCallbacksAndMessages(null);
            mMessageHandler = null;
        }
    }

    private static class MessageHandler extends Handler{

        private WeakReference<GoogleTensorFlowLiteActivity> mWeakReference;

        MessageHandler(GoogleTensorFlowLiteActivity activity){
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final GoogleTensorFlowLiteActivity activity = mWeakReference.get();
            switch (msg.what){
                case MESSAGE_REFRESH_UI:
                    final Recognition recognition = (Recognition) msg.obj;
                    activity.refreshUi(recognition);
                    break;
                default:
                    break;
            }
        }
    }

}
