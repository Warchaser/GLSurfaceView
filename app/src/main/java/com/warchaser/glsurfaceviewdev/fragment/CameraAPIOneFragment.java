package com.warchaser.glsurfaceviewdev.fragment;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.warchaser.glsurfaceviewdev.util.ImageReaderUtils;
import com.warchaser.glsurfaceviewdev.util.NLog;

import java.util.List;

public class CameraAPIOneFragment extends AbstractCameraFragment {

    private Camera mCamera;
    private Camera.PreviewCallback mPreviewCallback;

    public CameraAPIOneFragment(
            final Camera.PreviewCallback previewCallback,
            final int layoutResId,
            final Size desiredSize,
            final int textureViewResId){
        super(layoutResId, textureViewResId, desiredSize);
        mPreviewCallback = previewCallback;
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            final int index = getCameraId();
            mCamera = Camera.open(index);
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                final List<String> focusMode = parameters.getSupportedFocusModes();
                if(focusMode != null && focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                final List<Camera.Size> cameraSizes = parameters.getSupportedPictureSizes();
                final Size[] sizes = new Size[cameraSizes.size()];
                for(int i = 0; i < cameraSizes.size(); i++){
                    final Camera.Size size = cameraSizes.get(i);
                    sizes[i++] = new Size(size.width, size.height);
                }
                final Size previewSize = chooseOptimalSize(sizes, INPUT_SIZE.getWidth(), INPUT_SIZE.getHeight());
                parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());

                mCamera.setDisplayOrientation(90);
                mCamera.setParameters(parameters);
                mCamera.setPreviewTexture(surface);

            } catch (Exception e) {
                mCamera.release();
                e.printStackTrace();
                NLog.printStackTrace(TAG, e);
            }

            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
            final Camera.Size size = mCamera.getParameters().getPictureSize();
            mCamera.addCallbackBuffer(new byte[ImageReaderUtils.getYUVByteSize(size.height, size.width)]);
            mTextureView.setAspectRatio(size.height, size.width);
            mCamera.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

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

        if(mTextureView.isAvailable()){
            mCamera.startPreview();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        stopCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void stopCamera(){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private int getCameraId(){
        Camera.CameraInfo info = new Camera.CameraInfo();
        for(int i = 0; i < Camera.getNumberOfCameras(); i++){
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                return i;
            }
        }

        return -1;
    }
}
