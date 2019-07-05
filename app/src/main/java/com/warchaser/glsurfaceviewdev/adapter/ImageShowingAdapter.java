package com.warchaser.glsurfaceviewdev.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.warchaser.glsurfaceviewdev.R;
import com.warchaser.glsurfaceviewdev.view.SquareLayout;

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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        final ImageView imageView = viewHolder.mImageView;

        viewHolder.mRootLayout.setTag(position);

        final String path = mList.get(position);

        Glide.with(mContext)
                .load(path)
                .apply(mOptions)
                .into(imageView);

        imageView.setTransitionName(path);
        imageView.setTag(R.id.glide_tag, position);
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

            final int position = (int)v.getTag(R.id.glide_tag);

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

        /**
         * 图片
         * */
        ImageView mImageView;

        /**
         * 根布局
         * */
        SquareLayout mRootLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.mImageView);
            mRootLayout = itemView.findViewById(R.id.mRootLayout);
        }
    }
}
