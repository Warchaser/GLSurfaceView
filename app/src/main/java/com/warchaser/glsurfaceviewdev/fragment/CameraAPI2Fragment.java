package com.warchaser.glsurfaceviewdev.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.warchaser.glsurfaceviewdev.util.NLog;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraAPI2Fragment extends AbstractCameraFragment {

    /**
     * 防止app在关闭相机之前就退出
     * 是一个同步锁，同一时间只能有一个线程访问
     * */
    private final Semaphore CAMERA_OPEN_CLOSE_LOCK = new Semaphore(1);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener;

    /**
     * TensorFlow定义的像素尺寸(一个方形的宽和高)
     * */
    private final Size INPUT_SIZE;

    /**
     * 当前相机Id
     * */
    private String mCameraId;


    /**
     *
     * */
    private CameraCaptureSession mCameraCaptureSession;

    /**
     * 相机
     * */
    private CameraDevice mCameraDevice;

    /**
     * 相机传感器显示时的倾斜角度
     * */
    private Integer mSensorOrientation;

    /**
     * 相机预览的Size
     * */
    private Size mPreviewSize;

    private Handler mBackgroundHandler;

    /**
     *
     * */
    private ImageReader mPreviewReader;

    /**
     *
     * */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CaptureRequest mPreviewRequest;

    private final ConnectionCallback mConnectionCallback;

    private CameraAPI2Fragment(
            final ImageReader.OnImageAvailableListener imageAvailableListener,
            final Size inputSize,
            final int layoutResId,
            final ConnectionCallback connectionCallback,
            final int textureViewResId){
        super(layoutResId, textureViewResId);
        mOnImageAvailableListener = imageAvailableListener;
        INPUT_SIZE = inputSize;
        mConnectionCallback = connectionCallback;
    }

    /**
     * TextureView状态回调
     * */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * 相机状态回调
     * */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            CAMERA_OPEN_CLOSE_LOCK.release();
            mCameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            CAMERA_OPEN_CLOSE_LOCK.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            CAMERA_OPEN_CLOSE_LOCK.release();
            camera.close();
            mCameraDevice = null;
            final Activity activity = getActivity();
            if(activity != null){
                activity.finish();
            }
        }
    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    public static CameraAPI2Fragment newInstance(
            final ConnectionCallback callback,
            final ImageReader.OnImageAvailableListener listener,
            final int layoutResId,
            final Size inputSize,
            final int textureResId){

        return new CameraAPI2Fragment(
                listener,
                inputSize,
                layoutResId,
                callback,
                textureResId
        );

    }

    public void setCameraId(String cameraId){
        this.mCameraId = cameraId;
    }

    private void openCamera(final int width, final int height){
        setUpCameraOutputs();
        configureTransform(width, height);
        final Activity activity = getActivity();
        final CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if(!CAMERA_OPEN_CLOSE_LOCK.tryAcquire(2500, TimeUnit.MILLISECONDS)){
                throw new RuntimeException("等待锁超时");
            }
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            NLog.printStackTrace(TAG, e);
        } catch (InterruptedException e){
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera(){
        try {
            CAMERA_OPEN_CLOSE_LOCK.acquire();
            if(mCameraCaptureSession != null){
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }

            if(mCameraDevice != null){
                mCameraDevice.close();
                mCameraDevice = null;
            }

            if(mPreviewReader != null){
                mPreviewReader.close();
                mPreviewReader = null;
            }

        } catch (Exception e){
            e.printStackTrace();
            NLog.printStackTrace(TAG, e);
        } finally {
            CAMERA_OPEN_CLOSE_LOCK.release();
        }
    }

    private void setUpCameraOutputs(){
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            final StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            mPreviewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture.class),
                    INPUT_SIZE.getWidth(),
                    INPUT_SIZE.getHeight()
            );

            final int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
            NLog.printStackTrace(TAG, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("This device doesn\\'t support Camera2 API.");
        }

        mConnectionCallback.onPreviewSizeChosen(mPreviewSize, mSensorOrientation);
    }

    private void configureTransform(final int viewWidth, final int viewHeight){
        final Activity activity = getActivity();
        if(mTextureView == null || mPreviewSize == null || activity == null){
            return;
        }

        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0 , viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if(Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation){
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float)viewWidth / mPreviewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if(Surface.ROTATION_180 == rotation){
            matrix.postRotate(180, centerX, centerX);
        }

        mTextureView.setTransform(matrix);
    }

    private void createCameraPreviewSession(){
        try {
            final SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            final Surface surface = new Surface(texture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mPreviewReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
            mPreviewReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            mPreviewRequestBuilder.addTarget(mPreviewReader.getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mPreviewReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if(mCameraDevice == null){
                        return;
                    }

                    mCameraCaptureSession = session;
                    try {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                        NLog.printStackTrace(TAG, e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    NLog.eWithFile("TAG", "onConfigureFailed");
                }
            }, null);

//            surface.release();
        } catch (Exception e) {
            NLog.printStackTrace(TAG, e);
        }
    }

    @Override
    protected void startBackgroundThread(){
        super.startBackgroundThread();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void stopBackgroundThread(){
        super.stopBackgroundThread();
        mBackgroundHandler = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(LAYOUT_RES_ID, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTextureView = view.findViewById(TEXTURE_VIEW_RES_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if(mTextureView == null){
            return;
        }
        if(mTextureView.isAvailable()){
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    public interface ConnectionCallback{
        void onPreviewSizeChosen(Size size, int cameraRotation);
    }

}
