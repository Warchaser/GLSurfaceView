package com.warchaser.glsurfaceviewdev.activity;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.warchaser.glsurfaceviewdev.R;
import com.warchaser.glsurfaceviewdev.adapter.ImageShowingAdapter;
import com.warchaser.glsurfaceviewdev.app.BaseActivity;
import com.warchaser.glsurfaceviewdev.util.Constants;
import com.warchaser.glsurfaceviewdev.util.DisplayUtil;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlbumActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private ImageShowingAdapter mAdapter;

    /**
     * 每张图片的路径集合
     */
    private ArrayList<String> mImgPaths = new ArrayList<>();

    private static final int MESSAGE_SCAN_RESULT = 0x001;

    private MessageHandler mMessageHandler;

    private Bundle mBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_scan);

        initialize();
        getImages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMessageHandler != null) {
            mMessageHandler.removeCallbacksAndMessages(null);
            mMessageHandler = null;
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        mBundle = new Bundle(data.getExtras());

        final int startPosition = mBundle.getInt(Constants.EXTRA_STARTING_ALBUM_POSITION);
        final int currentPosition = mBundle.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION);
        if(startPosition != currentPosition){
            mRecyclerView.scrollToPosition(currentPosition);
        }

        supportPostponeEnterTransition();

        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    private void initialize(){
        mMessageHandler = new MessageHandler(this);

        mRecyclerView = findViewById(R.id.mRecyclerView);

        final int margin = DisplayUtil.dip2px(7f);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int pos = parent.getChildAdapterPosition(view);
                int column = pos % 3 + 1;
                outRect.top = margin;
                outRect.bottom = margin / 2;
                outRect.left = (column - 1) * margin / 3;
                outRect.right = (3 - column) * margin / 3;
            }
        });

        mAdapter = new ImageShowingAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickDelegate(new ImageShowingAdapter.OnItemClickDelegate() {
            @Override
            public void onImageClick(int position, String path, View view) {
                transition(view, path, position);
            }
        });

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if(mBundle != null){
                    final int position = mBundle.getInt(Constants.IMAGE_INDEX);
                    final String name = mBundle.getString(Constants.IMAGE_PATH);
                    sharedElements.clear();
                    names.clear();
                    names.add(name);

                    ImageView imageView = mRecyclerView.findViewWithTag(position);

//                    View itemView = layoutManager.findViewByPosition(position);
//                    ImageView imageView = itemView.findViewById(R.id.mImageView);
                    sharedElements.put(name, imageView);
                }

                mBundle = null;
            }
        });
    }

    /**
     * 获取所有图路径
     */
    private void getImages() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //查询指定文件夹下的所有图片
                String imgPath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/sensetime";
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = AlbumActivity.this
                        .getContentResolver();
                Cursor mCursor = null;
                try {
                    mCursor = mContentResolver.query(mImageUri, null,
                            MediaStore.Images.ImageColumns.DATA + " like '%" + imgPath + "%'",
                            null,
                            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC");
                } catch (Exception e) {
                    Toast.makeText(AlbumActivity.this, "未找到指定文件夹", Toast.LENGTH_SHORT).show();
                }
                if (mCursor == null) {
                    return;
                }
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));

                    Log.e("TAG", path);

                    mImgPaths.add(path);
                }
                mCursor.close();
                // 通知Handler扫描图片完成

                sendMessage(MESSAGE_SCAN_RESULT, null, -1, -1);
            }
        }).start();

    }

    private void scanResult() {
        if (mImgPaths.isEmpty()) {
            Toast.makeText(this, "未扫描到图片", Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter != null) {
                mAdapter.notifyDataSetAllChanged(mImgPaths);
            }
        }
    }

    private void transition(View view, String path, int position) {
        Intent intent = new Intent(this, ImageShowingActivity.class);
        intent.putExtra(Constants.IMAGE_PATH, path);
        intent.putStringArrayListExtra(Constants.IMAGE_PATH_LIST, mImgPaths);
        intent.putExtra(Constants.IMAGE_INDEX, position);
        if (Build.VERSION.SDK_INT < 21) {
            startActivity(intent);
        } else {
            Pair p = Pair.create(view, view.getTransitionName());
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, p).toBundle());
        }
    }

    /**
     * 向MessageHandler发送消息
     */
    private void sendMessage(int what, Object object, int arg1, int arg2) {
        if (mMessageHandler == null) {
            return;
        }

        if (object == null) {

            if (arg1 == -1 && arg2 == -1) {
                mMessageHandler.obtainMessage(what).sendToTarget();
            } else {
                mMessageHandler.obtainMessage(what, arg1, arg2).sendToTarget();
            }

        } else {
            mMessageHandler.obtainMessage(what, object).sendToTarget();
        }
    }

    private static class MessageHandler extends Handler {

        private WeakReference<AlbumActivity> mWeakReference;

        MessageHandler(AlbumActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final AlbumActivity activity = mWeakReference.get();
            switch (msg.what) {
                case MESSAGE_SCAN_RESULT:
                    activity.scanResult();
                    break;
                default:
                    break;
            }
        }
    }
}
