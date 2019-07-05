package com.warchaser.glsurfaceviewdev.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.SharedElementCallback;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import com.warchaser.glsurfaceviewdev.R;
import com.warchaser.glsurfaceviewdev.adapter.PhotoViewAdapter;
import com.warchaser.glsurfaceviewdev.app.BaseActivity;
import com.warchaser.glsurfaceviewdev.util.Constants;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageShowingActivity extends BaseActivity {

    private ViewPager mViewPager;

    private PhotoViewAdapter mAdapter;

    private int mStartPosition = 0;

    private boolean mIsFinishing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_showing);

        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mAdapter != null){
            mAdapter.clear();
            mAdapter = null;
        }
    }

    private void initialize(){
        mViewPager = findViewById(R.id.mViewPager);

        Intent intent = getIntent();
        if(intent != null){
            final ArrayList<String> list = intent.getStringArrayListExtra(Constants.IMAGE_PATH_LIST);
            final int position = intent.getIntExtra(Constants.IMAGE_INDEX, 0);
            mStartPosition = position;

            supportPostponeEnterTransition();
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if(mIsFinishing){
                        final int position = mViewPager.getCurrentItem();
                        final View sharedElement = mAdapter.getView(position);

                        if(mStartPosition != position){
                            final String name = mAdapter.getPath(position);
                            names.clear();
                            names.add(name);

                            sharedElements.clear();
                            sharedElements.put(name, sharedElement);
                        }
                    }
                }
            });

            mAdapter = new PhotoViewAdapter(this);
            mViewPager.setAdapter(mAdapter);
            mAdapter.notifyDataSetAllChanged(list);
            mViewPager.setCurrentItem(position);
        }

    }

    @Override
    public void onBackPressed() {
        mIsFinishing = true;
        setResult();
        super.supportFinishAfterTransition();
    }

    @TargetApi(22)
    @Override
    public void supportFinishAfterTransition() {
        setResult();
        super.supportFinishAfterTransition();
    }

    private void setResult(){
        final Intent data = new Intent();
        final int position = mViewPager.getCurrentItem();
        data.putExtra(Constants.IMAGE_INDEX, position);
        data.putExtra(Constants.IMAGE_PATH, mAdapter.getPath(position));
        data.putExtra(Constants.EXTRA_STARTING_ALBUM_POSITION, mStartPosition);
        data.putExtra(Constants.EXTRA_CURRENT_ALBUM_POSITION, position);
        setResult(RESULT_OK, data);
    }
}
