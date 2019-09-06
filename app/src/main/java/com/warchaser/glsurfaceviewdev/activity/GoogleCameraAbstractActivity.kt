package com.warchaser.glsurfaceviewdev.activity

import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.media.Image.Plane
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.warchaser.glsurfaceviewdev.app.BaseActivity
import com.warchaser.glsurfaceviewdev.fragment.GoogleCameraFragment
import com.warchaser.glsurfaceviewdev.util.ImageReaderUtils
import com.warchaser.glsurfaceviewdev.util.NLog
import com.warchaser.glsurfaceviewdev.util.SettingsUtil
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied

import permissions.dispatcher.RuntimePermissions
import java.lang.Exception
import java.nio.ByteBuffer

@RuntimePermissions
abstract class GoogleCameraAbstractActivity : BaseActivity(), ImageReader.OnImageAvailableListener {

    private var mIsUseCamera2API: Boolean = false

    private var mIsProcessingFrame: Boolean = false

    @JvmField
    protected var mPreviewWidth: Int = 0
    @JvmField
    protected var mPreviewHeight: Int = 0

    private var mRGBBytes: IntArray? = null
    private val mYUVBytes: Array<ByteArray?> = Array(3) { byteArrayOf() }
    private var mYRowStride: Int? = null

    private var mPostInferenceCallback: Runnable? = null
    private var mImageConverter: Runnable? = null

    private var mHandler : Handler? = null
    private var mHandlerThread : HandlerThread? = null

    private val mGoogleFragmentConnectionCallback = object : GoogleCameraFragment.ConnectionCallback {
        override fun onPreviewSizeChosen(size: Size, cameraRotation: Int) {
            mPreviewHeight = size.height
            mPreviewWidth = size.width
            this@GoogleCameraAbstractActivity.onPreviewSizeChosen(size, cameraRotation)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getActivityLayoutResId())
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setFragmentWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onResume() {
        super.onResume()

        mHandlerThread = HandlerThread("inference")
        mHandlerThread!!.start()
        mHandler = Handler(mHandlerThread!!.looper)
    }

    override fun onPause() {
        super.onPause()

        mHandlerThread!!.quitSafely()
        try {
            mHandlerThread?.join()
            mHandlerThread = null
            mHandler = null
        } catch (e : Exception) {
            e.printStackTrace()
            NLog.printStackTrace(TAG, e)
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun setFragment() {
        val cameraId = chooseCamera()
        var fragment: Fragment? = null
        if (mIsUseCamera2API) {
            val googleCameraFragment = GoogleCameraFragment.newInstance(
                    mGoogleFragmentConnectionCallback,
                    this@GoogleCameraAbstractActivity,
                    getFragmentLayoutResId(),
                    getDesiredPreviewFrameSize(),
                    getTextureViewId())
            googleCameraFragment.setCameraId(cameraId)
            fragment = googleCameraFragment
        }

        supportFragmentManager.beginTransaction().replace(getFragmentContainerId(), fragment!!).commit()
    }

    private fun chooseCamera(): String {
        val cameraManager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristic: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing: Int? = characteristic.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map: StreamConfigurationMap? = characteristic.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                if (map == null) {
                    continue
                }

                mIsUseCamera2API = (
                        facing == CameraCharacteristics.LENS_FACING_EXTERNAL
                                || isHardwareLevelSupported(characteristic, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
                        )

                return cameraId

            }
        } catch (e: Exception) {
            e.printStackTrace()
            NLog.printStackTrace(TAG, e)
        }

        return ""
    }

    private fun isHardwareLevelSupported(characteristics: CameraCharacteristics, requiredLevel: Int): Boolean {
        val deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel
        }

        return requiredLevel <= deviceLevel
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraPermissionFailed() {
        showToast("获取摄像头权限失败")
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraPermissionNeverAsk() {
        SettingsUtil.getInstance().startPermissionsActivity(this)
        showToast("获取摄像头权限失败，请自行打开")
    }

    override fun onImageAvailable(reader: ImageReader?) {
        if (mPreviewHeight == 0 || mPreviewWidth == 0) {
            return
        }

        if (mRGBBytes == null) {
            mRGBBytes = IntArray(mPreviewWidth * mPreviewHeight)
        }

        try {
            val image: Image? = reader?.acquireLatestImage()
            if (image == null) {
                return
            }

            if (mIsProcessingFrame) {
                image.close()
                return
            }

            mIsProcessingFrame = true
            val planes: Array<Plane> = image.planes
            fillBytes(planes, mYUVBytes)
            mYRowStride = planes[0].rowStride
            val uvRowStride: Int = planes[1].rowStride
            val uvPixelStride: Int = planes[1].pixelStride

            mImageConverter = Runnable {
                ImageReaderUtils.convertYUV420ToARGB8888(
                        mYUVBytes[0],
                        mYUVBytes[1],
                        mYUVBytes[2],
                        mPreviewWidth,
                        mPreviewHeight,
                        mYRowStride!!,
                        uvRowStride,
                        uvPixelStride,
                        mRGBBytes
                )
            }

            mPostInferenceCallback = Runnable {
                image.close()
                mIsProcessingFrame = false
            }

            processImage()


        } catch (e: Exception) {
            e.printStackTrace()
            NLog.printStackTrace(TAG, e)
        }
    }

    protected fun fillBytes(planes: Array<Plane>, yuvBytes: Array<ByteArray?>) {
        for (i in 0 until planes.size) {
            val buffer: ByteBuffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i])
        }
    }

    protected fun getScreenOrientation(): Int {
        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }

    protected fun getRGBBytes() : IntArray?{
        mImageConverter?.run()
        return mRGBBytes
    }

    protected fun ready4NextImage(){
        mPostInferenceCallback?.run()
    }

    @Synchronized
    protected fun runInBackground(runnable: Runnable){
        mHandler?.post(runnable)
    }

    protected abstract fun processImage()

    protected abstract fun getFragmentLayoutResId(): Int

    protected abstract fun getTextureViewId(): Int

    protected abstract fun getActivityLayoutResId(): Int

    protected abstract fun getFragmentContainerId(): Int

    protected abstract fun onPreviewSizeChosen(size: Size, rotation: Int)

    protected abstract fun getDesiredPreviewFrameSize(): Size


}
