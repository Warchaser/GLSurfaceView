package com.warchaser.glsurfaceviewdev.fragment;

import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import androidx.fragment.app.Fragment;

import com.warchaser.glsurfaceviewdev.util.Constants;
import com.warchaser.glsurfaceviewdev.util.NLog;
import com.warchaser.glsurfaceviewdev.view.AutoFitTextureView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AbstractCameraFragment extends Fragment {

    protected final String TAG = Constants.getSimpleClassName(this);

    /**
     * 相机预览的最小像素尺寸
     * */
    private static final int MINIMUM_PREVIEW_SIZE = 320;

    /**
     * 布局文件ID
     * */
    protected final int LAYOUT_RES_ID;

    /**
     * TextureView的资源Id
     * */
    protected final int TEXTURE_VIEW_RES_ID;

    /**
     * 相机预览View
     * */
    protected AutoFitTextureView mTextureView;

    /**
     * TensorFlow定义的像素尺寸(一个方形的宽和高)
     * */
    protected final Size INPUT_SIZE;

    public AbstractCameraFragment(final int layoutResId,final int textureViewResId, final Size inputSize){
        LAYOUT_RES_ID = layoutResId;
        TEXTURE_VIEW_RES_ID = textureViewResId;
        INPUT_SIZE = inputSize;
    }

    /**
     *
     * */
    protected static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    /**
     * 额外的用于跑任务的线程，以便不阻塞UI
     * */
    protected HandlerThread mBackgroundThread;

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
            NLog.printStackTrace(TAG, e);
        }
    }

    /**
     *
     * */
    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height){
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<>();
        final List<Size> tooSmall = new ArrayList<>();
        for(final Size option : choices){

            if(option == null){
                continue;
            }

            if(option.equals(desiredSize)){
                exactSizeFound = true;
            }

            if(option.getHeight() >= minSize && option.getHeight() >= minSize){
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        if(exactSizeFound){
            return desiredSize;
        }

        if(bigEnough.size() > 0){
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            return chosenSize;
        } else {
            return choices[0];
        }
    }

    public static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(final Size lhs, final Size rhs) {
            return Long.signum((long)lhs.getHeight() * lhs.getWidth() - (long)rhs.getWidth() * rhs.getHeight());
        }
    }
}
