package com.warchaser.glsurfaceviewdev.tensorflow;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.warchaser.glsurfaceviewdev.util.Constants;
import com.warchaser.glsurfaceviewdev.util.NLog;
import com.warchaser.glsurfaceviewdev.util.StringUtils;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public abstract class Classifier {

    private final String TAG = Constants.getSimpleClassName(this);

    /**
     * 分类的模型类型
     * */
    public enum Model{
        FLOAT,
        QUANTIZED
    }

    /**
     * 执行分类的运行时设备类型
     * */
    public enum Device{
        CPU,
        NNPAI,
        GPU
    }

    /**
     * UI显示的最大结果数量
     * */
    private static final int MAX_RESULTS_IN_SHOWING = 3;

    /**
     * 输入的维度数量
     * */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    /**
     * 为存储Image数据而预申请的缓冲区
     * */
    private final int[] INT_VALUES = new int[getImageSizeX() * getImageSizeY()];

    private final Interpreter.Options TF_LITE_OPTIONS = new Interpreter.Options();

    private MappedByteBuffer mTFLiteModel;

    private List<String> mLabels;

    private GpuDelegate mGpuDelegate = null;

    protected Interpreter mTFLite;

    protected ByteBuffer mImageData = null;

    /**
     * X坐标的图像长度
     * */
    public abstract int getImageSizeX();

    /**
     * Y坐标的图像长度
     * */
    public abstract int getImageSizeY();

    /**
     * Assets中的模型文件路径
     * */
    protected abstract String getModelPath();

    /**
     * Assets中label文件的路径
     * */
    protected abstract String getLabelPath();

    /**
     * 获取用来存储单通道颜色的字节
     * */
    protected abstract int getNumBytesPerChannel();

    /**
     * add 像素值 to 字节缓冲区
     * */
    protected abstract void addPixelValue(int pixelValue);

    /**
     *
     * */
    protected abstract float getProbability(int labelIndex);

    /**
     *
     * */
    protected abstract void setProbability(int labelIndex, Number value);

    /**
     *
     * */
    protected abstract float getNormalizedProbability(int labelIndex);

    /**
     *
     * */
    protected abstract void runInference();

    /**
     * 图片被识别后的一个被Classifier描述的不可变的结果Bean
     * */
    public static class Recognition{
        /**
         * 图片被识别后的唯一标识。
         * 这个唯一标识是对于本类而言的，而不是对象的实例
         * */
        private final String id;

        /**
         * 识别后的展示名字
         * */
        private final String title;

        /**
         * 描述这个识别结果对于其他结果来说有多准确的一个可排列的分数。
         * 越高越好。
         * */
        private final Float confidence;

        /**
         *
         * */
        private RectF location;

        public Recognition(
                final String id,
                final String title,
                final Float confidence,
                final RectF location){
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId(){
            return id;
        }

        public String getTitle(){
            return title;
        }

        public Float getConfidence(){
            return confidence;
        }

        public RectF getLocation(){
            return new RectF(location);
        }

        public void setLocation(RectF location){
            this.location = location;
        }

        @NonNull
        @Override
        public String toString() {
            String resultString = "";
            if(id != null){
                resultString += "[" + id + "] ";
            }

            if(title != null){
                resultString += title + " ";
            }

            if(confidence != null){
                resultString += StringUtils.getFormatString("(%.1f%%) ", confidence * 100.f);
            }

            if(location != null){
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }

    protected Classifier(
            AssetManager assetManager,
            Device device,
            int numOfThreads) throws IOException {
        mTFLiteModel = loadModelFile(assetManager);
        switch (device){
            case NNPAI:
                TF_LITE_OPTIONS.setUseNNAPI(true);
                break;
            case CPU:
                mGpuDelegate = new GpuDelegate();
                TF_LITE_OPTIONS.addDelegate(mGpuDelegate);
                break;
            case GPU:
                break;
        }
        TF_LITE_OPTIONS.setNumThreads(numOfThreads);
        mTFLite = new Interpreter(mTFLiteModel, TF_LITE_OPTIONS);
        mLabels = loadLabelList(assetManager);
        mImageData = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE
                        * getImageSizeX()
                        * getImageSizeY()
                        * DIM_PIXEL_SIZE
                        * getNumBytesPerChannel()
        );
        mImageData.order(ByteOrder.nativeOrder());
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException{
        final AssetFileDescriptor fileDescriptor = assetManager.openFd(getModelPath());
        final FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        final FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager) throws IOException{
        final List<String> labels = new ArrayList<>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null){
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    private void convertBitmapByteBuffer(Bitmap bitmap){
        if(mImageData == null){
            return;
        }

        mImageData.rewind();
        bitmap.getPixels(
                INT_VALUES,
                0,
                bitmap.getWidth(),
                0,
                0 ,
                bitmap.getWidth(),
                bitmap.getHeight()
        );
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for(int i = 0; i < getImageSizeX(); i++){
            for(int j = 0; j < getImageSizeY(); j++){
                final int val = INT_VALUES[pixel++];
                addPixelValue(val);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        NLog.e(TAG, "Time-cost to put values into ByteBuffer: " + (endTime - startTime));
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap){
        convertBitmapByteBuffer(bitmap);

        runInference();

        PriorityQueue<Recognition> priorityQueue = new PriorityQueue<>(
                3,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition lhs, Recognition rhs) {
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                    }
                });

        for(int i = 0; i < mLabels.size(); i++){
            priorityQueue.add(new Recognition(
                    "" + i,
                    mLabels.size() > i ? mLabels.get(i) : "unknown",
                    getNormalizedProbability(i),
                    null
                    )
            );
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int size = Math.min(priorityQueue.size(), MAX_RESULTS_IN_SHOWING);
        for(int i = 0; i < size; i++){
            recognitions.add(priorityQueue.poll());
        }

        return recognitions;

    }

    public void close(){
        if(mTFLite != null){
            mTFLite.close();
            mTFLite = null;
        }

        if(mGpuDelegate != null){
            mGpuDelegate.close();
            mGpuDelegate = null;
        }

        mTFLiteModel = null;
    }

    protected int getNumLabels(){
        return mLabels.size();
    }
}
