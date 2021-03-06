package com.warchaser.glsurfaceviewdev.activity

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.database.Cursor
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import androidx.core.app.SharedElementCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Pair
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView

import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.adapter.ImageShowingAdapter
import com.warchaser.glsurfaceviewdev.app.BaseActivity
import com.warchaser.glsurfaceviewdev.util.Constants
import com.warchaser.glsurfaceviewdev.util.DisplayUtil
import com.warchaser.glsurfaceviewdev.util.HandlerUtils
import com.warchaser.glsurfaceviewdev.util.NLog
import com.warchaser.glsurfaceviewdev.view.SquareLayout
import kotlinx.android.synthetic.main.activity_media_scan.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied

import java.lang.ref.WeakReference
import java.util.ArrayList

import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class AlbumActivity : BaseActivity() {

    private var mAdapter: ImageShowingAdapter? = null

    /**
     * 每张图片的路径集合
     */
    private val mImgPaths = ArrayList<String>()

    private var mMessageHandler: MessageHandler? = null

    private var mBundle: Bundle? = null

    private val PATH : String = "/DCIM/我丹"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_scan)

        initialize()

        getImagesWithPermissionCheck()
    }

    override fun onDestroy() {
        super.onDestroy()

        mMessageHandler?.removeCallbacksAndMessages(null)
        mMessageHandler = null
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)

        mBundle = Bundle(data.extras)

        val startPosition = mBundle!!.getInt(Constants.EXTRA_STARTING_ALBUM_POSITION)
        val currentPosition = mBundle!!.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION)
        if (startPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition)
        }

        supportPostponeEnterTransition()

        mRecyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mRecyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                startPostponedEnterTransition()
                return true
            }
        })
    }

    private fun initialize() {
        mMessageHandler = MessageHandler(this)

        val margin = DisplayUtil.dip2px(3f)
        val columnCount = 3
        val layoutManager = GridLayoutManager(this, columnCount)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                val pos = parent.getChildAdapterPosition(view)
                val column = pos % columnCount + 1
                val line = pos / columnCount + 1

                if(line == 1){
                    outRect.top = 0
                } else {
                    outRect.top = margin
                }

                outRect.bottom = 0
                outRect.left = (column - 1) * margin / columnCount
                outRect.right = (columnCount - column) * margin / columnCount
            }
        })

        mAdapter = ImageShowingAdapter(this)
        mRecyclerView.adapter = mAdapter

        mAdapter?.setOnItemClickDelegate { position, path, view -> transition(view, path, position) }

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                if (mBundle != null) {
                    val position = mBundle?.getInt(Constants.IMAGE_INDEX)
                    val name = mBundle?.getString(Constants.IMAGE_PATH)

                    val rootLayout = mRecyclerView.findViewWithTag<SquareLayout>(position)

                    val imageView = rootLayout.getChildAt(0) as ImageView

                    name?.apply {
                        names?.run {
                            clear()
                            add(this@apply)
                        }

                        sharedElements?.run {
                            clear()
                            this[this@apply] = imageView
                        }
                    }
                }

                mBundle = null
            }
        })
    }

    /**
     * 获取所有图路径
     */
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getImages() {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            HandlerUtils.sendMessage(mMessageHandler, MESSAGE_ERROR, getString(R.string.no_external_storage_mounted), -1, -1)
            return
        }

        Thread(Runnable {
            //查询指定文件夹下的所有图片
            val imgPath = Environment.getExternalStorageDirectory().toString() + PATH
            val mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val mContentResolver = this@AlbumActivity
                    .contentResolver
            var mCursor: Cursor? = null
            try {
                mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.ImageColumns.DATA + " like '%" + imgPath + "%'", null,
                        MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC")
            } catch (e: Exception) {
                NLog.printStackTrace("AlbumActivity", e)
                HandlerUtils.sendMessage(mMessageHandler, MESSAGE_ERROR, getString(R.string.no_folder_found), -1, -1)
            }

            if (mCursor == null) {
                return@Runnable
            }
            while (mCursor.moveToNext()) {
                // 获取图片的路径
                val path = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.Images.Media.DATA))

                NLog.e("AlbumActivity", path)

                mImgPaths.add(path)
            }
            mCursor.close()
            // 通知Handler扫描图片完成

            HandlerUtils.sendMessage(mMessageHandler, MESSAGE_SCAN_RESULT, null, -1, -1)
        }).start()

    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onReadExternalStorageFailed(){
        showToast(R.string.read_external_storage_permission_granted_failed)
        finish()
    }

    private fun scanResult() {
        if (mImgPaths.isEmpty()) {
            HandlerUtils.sendMessage(mMessageHandler, MESSAGE_ERROR, getString(R.string.no_pics_scanned), -1, -1)
        } else {
            mAdapter?.notifyDataSetAllChanged(mImgPaths)
        }
    }

    private fun transition(view: View, path: String, position: Int) {
        val intent = Intent(this, ImageShowingActivity::class.java)
        intent.putExtra(Constants.IMAGE_PATH, path)
        intent.putStringArrayListExtra(Constants.IMAGE_PATH_LIST, mImgPaths)
        intent.putExtra(Constants.IMAGE_INDEX, position)
        if (Build.VERSION.SDK_INT < 21) {
            startActivity(intent)
        } else {
            val p = Pair.create(view, view.transitionName)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, p).toBundle())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private class MessageHandler internal constructor(activity: AlbumActivity) : Handler() {

        private val mWeakReference: WeakReference<AlbumActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = mWeakReference.get()
            when (msg.what) {
                MESSAGE_SCAN_RESULT -> {
                    activity?.scanResult()
                }

                MESSAGE_ERROR -> {
                    val message = msg.obj as String
                    activity?.showToast(message)
                }

                else -> {
                }
            }
        }
    }

    companion object {

        private const val MESSAGE_SCAN_RESULT = 0x001
        private const val MESSAGE_ERROR = 0x002
    }
}
