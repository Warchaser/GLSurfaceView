package com.warchaser.glsurfaceviewdev.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.warchaser.glsurfaceviewdev.R;

import java.util.ArrayList;

public class ImageShowingAdapter extends RecyclerView.Adapter<ImageShowingAdapter.ViewHolder> {

    private Context mContext;

    private ArrayList<String> mList = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    private OnItemClickDelegate mOnItemClickDelegate;

    private RequestOptions mOptions;

    public ImageShowingAdapter(Context context){
        mContext = context;
        mOnItemClickListener = new OnItemClickListener();

        mOptions = new RequestOptions()
                .optionalCenterCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH);
    }

    public void notifyDataSetAllChanged(ArrayList<String> list){
        if(mList != null){
            mList.clear();
            mList.addAll(list);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_img, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        ImageView imageView = viewHolder.mImageView;
        String path = mList.get(i);

        Glide.with(mContext)
                .load(path)
                .apply(mOptions)
                .into(imageView);

        imageView.setTransitionName(path);
        imageView.setTag(i);
        imageView.setOnClickListener(mOnItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickDelegate(OnItemClickDelegate delegate){
        this.mOnItemClickDelegate = delegate;
    }

    private class OnItemClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(mOnItemClickDelegate == null){
                return;
            }

            final int position = (int)v.getTag();

            switch (v.getId()){
                case R.id.mImageView:
                    mOnItemClickDelegate.onImageClick(position, mList.get(position), v);
                    break;
                default:
                    break;
            }

        }
    }

    public interface OnItemClickDelegate{
        void onImageClick(int position, String path, View view);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.mImageView);
        }
    }
}
