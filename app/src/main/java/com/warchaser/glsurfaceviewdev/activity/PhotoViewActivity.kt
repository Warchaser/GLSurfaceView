package com.warchaser.glsurfaceviewdev.activity

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.warchaser.glsurfaceviewdev.R
import kotlinx.android.synthetic.main.activity_photo_view_activity.*


class PhotoViewActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view_activity)

        initialize()
    }

    private fun initialize(){
        val drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher)
        mPhotoView.setImageDrawable(drawable)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}