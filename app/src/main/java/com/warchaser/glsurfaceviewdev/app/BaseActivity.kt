package com.warchaser.glsurfaceviewdev.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.warchaser.glsurfaceviewdev.util.Constants

open class BaseActivity : AppCompatActivity(){

    protected lateinit var TAG : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = Constants.getSimpleClassName(this)
    }

    override fun onDestroy() {
        super.onDestroy()
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(strResourceId:Int){
        Toast.makeText(this,strResourceId, Toast.LENGTH_SHORT).show()
    }

    fun showToastLong(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showToastLong(strResourceId:Int){
        Toast.makeText(this, strResourceId, Toast.LENGTH_LONG).show()
    }

}