package com.warchaser.glsurfaceviewdev.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.core.app.ActivityCompat
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.warchaser.glsurfaceviewdev.util.NLog
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.app.BaseActivity
import com.warchaser.glsurfaceviewdev.tensorflow.TensorFlowLiteClassifier
import com.warchaser.glsurfaceviewdev.util.DisplayUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_open_gl_camera.*
import org.opencv.android.OpenCVLoader
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.lang.Exception
import java.util.*

@RuntimePermissions
class OpenGLCameraActivity : BaseActivity() {

    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null

    private var mMainHandler : Handler? = null

    private val TAG: String = "MainActivity"

    private var mPreviewSize: Size? = null
    private var mCameraId: String? = null
    private var mCameraDevice: CameraDevice? = null

    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureRequest: CaptureRequest? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null

    private var mImageReader: ImageReader? = null
    private val ORIENTATIONS = SparseIntArray()

    /**
     * 模型存放路径
     * */
    private val MODEL_FILE = "file:///android_asset/digital_gesture.pb"
    private var mTensorFlowLiteClassifier : TensorFlowLiteClassifier ? = null

    private var mSurface : Surface ? = null
    private var mSurfaceTexture : SurfaceTexture ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fullScreen()

        setContentView(R.layout.activity_open_gl_camera)

        initialize()

        initializeTextureView()
    }

    private fun initialize() {
        mCameraThread = HandlerThread("CameraThread")
        mCameraThread!!.start()
        mCameraHandler = Handler(mCameraThread!!.looper)

        mMainHandler = Handler(mainLooper)

        mBtnTakePicture.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                takePicture()
            }
        })

        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)

        mTensorFlowLiteClassifier = TensorFlowLiteClassifier(assets, MODEL_FILE)
    }

    private fun initializeTextureView() {
        mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return false
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                mSurfaceTexture = surface
                initializeCamera(width, height)
//                openCamera()
                openCameraWithPermissionCheck()
            }

        }
    }

    fun initializeCamera(width : Int, height : Int){
        val cameraManager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG,1)
        mImageReader!!.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener{
            override fun onImageAvailable(reader: ImageReader?) {
                NLog.e("OnImageAvailableListener", "onImageAvailable")
                reader?.run {
                    val image = acquireNextImage()
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    close()
                    val bitmap4Predict = DisplayUtil.scaleBitmap(bitmap, 64, 64)

                    val result = mTensorFlowLiteClassifier!!.predict(bitmap4Predict)

                    showToast(result[0])
                    bitmap.recycle()
                    bitmap4Predict.recycle()
                }

            }

        }, mMainHandler)

        try {
            for(cameraId : String in cameraManager.cameraIdList){
                val characteristics : CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing : Int? = characteristics.get(CameraCharacteristics.LENS_FACING)
                //打开后置摄像头
                if(facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT){
                    continue
                }
                //获取StreamConfigurationMap(管理摄像头支持的所有输出格式和尺寸)
                val map : StreamConfigurationMap? = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map?.getOutputSizes(SurfaceTexture::class.java)!!, width, height)
                mCameraId = cameraId
            }
        } catch (e : Exception){
            NLog.printStackTrace(TAG, e)
        } catch (e : Error){
            NLog.printStackTrace(TAG, e)
        }
    }

    private fun startPreview(){
        try {
            mSurfaceTexture?.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
            mSurface = Surface(mSurfaceTexture)
            mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder?.addTarget(mSurface)
            mCameraDevice?.createCaptureSession(
                    listOf(mSurface, mImageReader?.surface),
                    object : CameraCaptureSession.StateCallback(){
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            NLog.eWithFile("onConfigureFailed", "onConfigureFailed")
                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            mCameraCaptureSession = session
                            try {
//                                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
//                                // 打开闪光灯
//                                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.FLASH_MODE_OFF)
//                                val previewRequest = mPreviewRequestBuilder?.build()
                                mCaptureRequest = mPreviewRequestBuilder?.build()
                                mCameraCaptureSession?.setRepeatingRequest(mCaptureRequest, null, mCameraHandler)
                            } catch (e : Exception) {
                                NLog.printStackTrace("onConfigured", e)
                            }
                        }

                    }, mCameraHandler)
        } catch (e : Exception){
            NLog.printStackTrace(TAG, e)
        } catch (e : Error){
            NLog.printStackTrace(TAG, e)
        } finally {
//            previewSurface?.release()
        }
    }

    private fun takePicture(){
        mCameraDevice?.run {
            val captureRequestBuilder : CaptureRequest.Builder
            try {
                captureRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureRequestBuilder.addTarget(mImageReader!!.surface)
//                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
//                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                val rotation = windowManager.defaultDisplay.rotation
                captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
                val captureRequest = captureRequestBuilder.build()
                mCameraCaptureSession?.capture(captureRequest, null, mCameraHandler)
            } catch (e : Exception) {
                NLog.printStackTrace("TakePicture", e)
            }
        }

    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openCamera(){

        if(!OpenCVLoader.initDebug()){
            showToast(R.string.open_cv_init_failed)
            finish()
        }

        val cameraManager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(mCameraId, mStateCallback, mMainHandler)
            }
        } catch (e : Exception){
            NLog.printStackTrace(TAG, e)
        } catch (e : Error){
            NLog.printStackTrace(TAG, e)
        }
    }

    private fun getOptimalSize(sizeMap : Array<Size>, width: Int, height: Int) : Size{
        val sizeList : ArrayList<Size> = ArrayList()
        for(option : Size in sizeMap){
            if(width > height){
                if(option.width > width && option.height > height){
                    sizeList.add(option)
                }
            } else {
                if(option.width > height && option.height > width){
                    sizeList.add(option)
                }
            }
        }

        if(sizeList.isNotEmpty()){
            return Collections.min(sizeList, object : Comparator<Size>{
                override fun compare(lhs: Size, rhs: Size): Int {
                    val result : Int = lhs.width * lhs.height - rhs.width * rhs.height
                    return java.lang.Long.signum(result.toLong())
                }
            })
        }

        return sizeMap[0]
    }

    private val mStateCallback : CameraDevice.StateCallback = object : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
//            mCameraDevice?.close()
//            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            mCameraDevice = null
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        clearFindViewByIdCache()

        mCameraHandler?.removeCallbacksAndMessages("CameraThread")
        mCameraHandler = null
        super.onDestroy()
    }
}
