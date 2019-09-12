package com.warchaser.glsurfaceviewdev.tensorflow;

import android.content.res.AssetManager;

import java.io.IOException;

public class FloatClassifier extends Classifier{

    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private float[][] mLabelProbArray = null;

    public FloatClassifier(AssetManager assetManager, Device device, int numOfThreads) throws IOException {
        super(assetManager, device, numOfThreads);
        mLabelProbArray = new float[1][getNumLabels()];
    }

    @Override
    public int getImageSizeX() {
        return 224;
    }

    @Override
    public int getImageSizeY() {
        return 224;
    }

    @Override
    protected String getModelPath() {
        return "mobilenet_v1_1.0_224.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "labels.txt";
    }

    @Override
    protected int getNumBytesPerChannel() {
        return 4;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        mImageData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        mImageData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        mImageData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }

    @Override
    protected float getProbability(int labelIndex) {
        return mLabelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        mLabelProbArray[0][labelIndex] = value.floatValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return mLabelProbArray[0][labelIndex];
    }

    @Override
    protected void runInference() {
        mTFLite.run(mImageData, mLabelProbArray);
    }
}
