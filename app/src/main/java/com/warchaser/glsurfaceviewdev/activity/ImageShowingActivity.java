package com.warchaser.glsurfaceviewdev.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
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
    private int mCurrentPosition = 0;

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
                        View sharedElement = mAdapter.getView(mCurrentPosition);

                        if(mStartPosition != mCurrentPosition){
                            final String name = mAdapter.getPath(mCurrentPosition);
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

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mCurrentPosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
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
        data.putExtra(Constants.EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
    }
}