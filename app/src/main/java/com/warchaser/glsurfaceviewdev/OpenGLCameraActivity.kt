package com.warchaser.glsurfaceviewdev

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.util.Size
import android.view.Surface
import android.view.TextureView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_open_gl_camera.*
import java.lang.Exception
import java.util.*

class OpenGLCameraActivity : AppCompatActivity() {

    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null
    private val TAG: String = "MainActivity"

    private var mPreviewSize: Size? = null
    private var mCameraId: String? = null
    private var mCameraDevice: CameraDevice? = null

    private var mCaptureRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureRequest: CaptureRequest? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gl_camera)

        initialize()

        initializeTextureView()
    }

    private fun initialize() {
        mCameraThread = HandlerThread("CameraThread")
        mCameraThread!!.start()
        mCameraHandler = Handler(mCameraThread!!.looper)
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
                initializeCamera(width, height)
                openCamera()
            }

        }
    }

    private fun initializeCamera(width : Int, height : Int){
        val cameraManager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
        val surfaceTexture : SurfaceTexture = mTextureView.surfaceTexture
        var previewSurface : Surface ? = null
        try {
            surfaceTexture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
            previewSurface = Surface(surfaceTexture)
            mCaptureRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mCaptureRequestBuilder?.addTarget(previewSurface)
            mCameraDevice?.createCaptureSession(
                    Arrays.asList(previewSurface),
                    object : CameraCaptureSession.StateCallback(){
                        override fun onConfigureFailed(session: CameraCaptureSession) {

                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            mCaptureRequest = mCaptureRequestBuilder?.build()
                            mCameraCaptureSession = session
                            mCameraCaptureSession?.setRepeatingRequest(mCaptureRequest, null, mCameraHandler)
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

    private fun openCamera(){
        val cameraManager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler)
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

        if(!sizeList.isEmpty()){
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
            camera.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            mCameraDevice = null
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        clearFindViewByIdCache()

        mCameraHandler?.removeCallbacksAndMessages("CameraThread")
        mCameraHandler = null
        super.onDestroy()
    }
}
