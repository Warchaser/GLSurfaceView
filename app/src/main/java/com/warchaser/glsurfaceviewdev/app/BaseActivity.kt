package com.warchaser.glsurfaceviewdev.app

import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.warchaser.glsurfaceviewdev.util.Constants
import com.warchaser.glsurfaceviewdev.util.ToastUtil

open class BaseActivity : AppCompatActivity(){

    protected lateinit var TAG : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = Constants.getSimpleClassName(this)
        AppManager.getInstance().addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.getInstance().removeActivity(this)
    }

    protected fun fullScreen(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * 弹出toast的公共方法
     */
    fun showToast(message: String) {
        ToastUtil.showToast(message)
    }

    fun showToast(strResourceId:Int){
        ToastUtil.showToast(strResourceId)
    }

    fun showToastLong(message: String){
        ToastUtil.showToastLong(message)
    }

    fun showToastLong(strResourceId:Int){
        ToastUtil.showToastLong(strResourceId)
    }

    protected fun getCameraManager() : CameraManager{
        return AppManager.getInstance().getCameraManager(this)
    }

}