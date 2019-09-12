package com.warchaser.glsurfaceviewdev.tensorflow;

import android.content.res.AssetManager;

import java.io.IOException;

public class QuantizedClassifier extends Classifier{

    private byte[][] mLabelProbArray;

    public QuantizedClassifier(AssetManager assetManager, Device device, int numOfThreads) throws IOException {
        super(assetManager, device, numOfThreads);
        mLabelProbArray = new byte[1][getNumLabels()];
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
        return "mobilenet_v1_1.0_224_quant.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "labels.txt";
    }

    @Override
    protected int getNumBytesPerChannel() {
        return 1;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        mImageData.put((byte)((pixelValue >> 16) & 0xFF));
        mImageData.put((byte) ((pixelValue >> 8) & 0xFF));
        mImageData.put((byte) (pixelValue & 0xFF));
    }

    @Override
    protected float getProbability(int labelIndex) {
        return mLabelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        mLabelProbArray[0][labelIndex] = value.byteValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return (mLabelProbArray[0][labelIndex] & 0xFF) / 255.0f;
    }

    @Override
    protected void runInference() {
        mTFLite.run(mImageData, mLabelProbArray);
    }
}
