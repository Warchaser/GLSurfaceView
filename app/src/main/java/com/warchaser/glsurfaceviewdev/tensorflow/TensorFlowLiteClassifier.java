package com.warchaser.glsurfaceviewdev.tensorflow;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TensorFlowLiteClassifier {

    /**
     * 模型中输入变量的名称
     * */
    private final String INPUT_NAME = "input_x";

    /**
     * 模型中输出变量的名称
     * */
    private final String OUTPUT_NAME = "predict";

    /**
     * 概率变量的名称
     * */
    private final String PROBABILITY_NAME = "probability";

    /**
     * cnn输出层的数据
     * */
    private final String OUT_LAYER_NAME = "outlayer";

    private TensorFlowInferenceInterface mInterface;

    /**
     * 图片维度
     * */
    private final int IMAGE_WIDTH = 64;

    public TensorFlowLiteClassifier(AssetManager manager, String modelPath){
        mInterface = new TensorFlowInferenceInterface(manager, modelPath);
    }

    private float[] getPixels(Bitmap bitmap){
        final float[] floatValues = new float[IMAGE_WIDTH * IMAGE_WIDTH * 3];
        if(bitmap == null){
            return floatValues;
        }
        final int[] intValues = new int[IMAGE_WIDTH * IMAGE_WIDTH];

        if(bitmap.getWidth() != IMAGE_WIDTH || bitmap.getHeight() != IMAGE_WIDTH){
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, IMAGE_WIDTH, IMAGE_WIDTH);
        }

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for(int i = 0; i < intValues.length; i++){
            final int val = intValues[i];
            floatValues[i * 3] = Color.red(val) / 255.0f;
            floatValues[i * 3 + 1] = Color.green(val) / 255.0f;
            floatValues[i * 3 + 2] = Color.blue(val) / 255.0f;
        }

        return floatValues;
    }

    public ArrayList<String> predict(Bitmap bitmap){
        final ArrayList<String> list = new ArrayList<>();
        if(bitmap == null){
            return list;
        }
        final float[] inputData = getPixels(bitmap);
        mInterface.feed(INPUT_NAME, inputData, 1, IMAGE_WIDTH, IMAGE_WIDTH, 3);
        //运行模型,run的参数必须是String[]类型
        final String[] outPutNames = new String[]{OUTPUT_NAME, PROBABILITY_NAME, OUT_LAYER_NAME};
        mInterface.run(outPutNames);

        //获取结果
        final int[] labels = new int[1];
        mInterface.fetch(OUTPUT_NAME, labels);
        final int label = labels[0];

        final float[] probs = new float[11];
        mInterface.fetch(PROBABILITY_NAME, probs);

        final DecimalFormat df = new DecimalFormat("0.000000");
        final float labelProb = probs[label];

        list.add(Integer.toString(label));
        list.add(df.format(labelProb));

        return list;
    }

}
