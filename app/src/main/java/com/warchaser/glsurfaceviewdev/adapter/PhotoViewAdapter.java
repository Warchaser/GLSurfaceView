package com.warchaser.glsurfaceviewdev.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.warchaser.glsurfaceviewdev.R;

import java.util.ArrayList;

/**
 * Author: Leon
 * Date: on 2019/7/3
 * Email：fangjianwei@ihmair.cn
 * 作用：
 */
public class PhotoViewAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> mImgPaths = new ArrayList<>();
    private PhotoViewAttacher mAttacher;

    private RequestOptions mOptions;

    private SparseArray<View> mViews = new SparseArray<>();

    public PhotoViewAdapter(Context context) {
        this.mContext = context;

        mOptions = new RequestOptions()
                .optionalCenterCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH);
    }

    public void notifyDataSetAllChanged(ArrayList<String> list){
        if(mImgPaths != null){
            mImgPaths.clear();
            mImgPaths.addAll(list);
        }

        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = container.inflate(mContext,
                R.layout.item_photo_view, null);
        final PhotoView photoView = view.findViewById(R.id.mPhoto);

        mViews.put(position, photoView);

        String path = mImgPaths.get(position);

        photoView.setTransitionName(path);

        Glide.with(mContext)
                .load(path)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        photoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                photoView.getViewTreeObserver().removeOnPreDrawListener(this);
                                ActivityCompat.startPostponedEnterTransition((Activity) mContext);
                                return true;
                            }
                        });
                        return false;
                    }
                })
                .apply(mOptions)
                .into(photoView);

        //给图片增加点击事件
        // mAttacher = new PhotoViewAttacher(mPhotoView);
        // mAttacher.setOnViewTapListener(PhotoviewActivity.this);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mAttacher = null;
        if(position < mViews.size()){
            mViews.removeAt(position);
        }
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public String getPath(int position){
        return mImgPaths.get(position);
    }

    public View getView(int position){
        return mViews.get(position);
    }

}
