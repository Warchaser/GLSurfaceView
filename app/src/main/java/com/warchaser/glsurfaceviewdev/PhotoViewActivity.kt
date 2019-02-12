package com.warchaser.glsurfaceviewdev

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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