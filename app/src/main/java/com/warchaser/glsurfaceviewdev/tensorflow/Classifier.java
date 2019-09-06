package com.warchaser.glsurfaceviewdev.tensorflow;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.List;

public abstract class Classifier {

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
    protected abstract float getNormalizedProbaility(int labelIndex);

    /**
     *
     * */
    protected abstract void runInference();

    protected int getNumLabels(){
        return mLabels.size();
    }
}
