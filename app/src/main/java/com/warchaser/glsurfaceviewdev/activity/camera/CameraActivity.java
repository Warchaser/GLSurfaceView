/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.warchaser.glsurfaceviewdev.activity.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.warchaser.glsurfaceviewdev.R;
import com.warchaser.glsurfaceviewdev.app.BaseActivity;
import com.warchaser.glsurfaceviewdev.tensorflow.TensorFlowLiteClassifier;
import com.warchaser.glsurfaceviewdev.util.DisplayUtil;
import com.warchaser.glsurfaceviewdev.util.NLog;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

public class CameraActivity extends BaseActivity {

    static {
        if(!OpenCVLoader.initDebug()){
//            showToast(R.string.open_cv_init_failed)
//            finish()
        }
    }

    private Camera2BasicFragment mCamera2BasicFragment;
    private TensorFlowLiteClassifier mTensorFlowLiteClassifier;
    private final String MODEL_FILE = "file:///android_asset/digital_gesture500_200_c3.pb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullScreen();

        setContentView(R.layout.activity_camera);

        mCamera2BasicFragment = Camera2BasicFragment.newInstance();
        mCamera2BasicFragment.setCallBack(new Camera2BasicFragment.CallBack() {
            @Override
            public void onTakePicture(byte[] bytes) {

                if(mTensorFlowLiteClassifier == null){
                    mTensorFlowLiteClassifier = new TensorFlowLiteClassifier(getAssets(), MODEL_FILE);
                }

//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                byte[] bytes = new byte[buffer.remaining()];
//                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                Bitmap bitmap4Predict = DisplayUtil.scaleBitmap(bitmap, 64, 64);
                ArrayList<String> result = mTensorFlowLiteClassifier.predict(bitmap4Predict);
                final String prediction = "prediction is " + result.get(0);
                final String precision = "precision is " + result.get(1);
                mCamera2BasicFragment.showToast(prediction + "\n" + precision);
                NLog.e("onTakePicture", prediction);
                NLog.e("onTakePicture", precision);
                bitmap.recycle();
                bitmap4Predict.recycle();
            }
        });

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mCamera2BasicFragment)
                    .commit();
        }
    }

}
