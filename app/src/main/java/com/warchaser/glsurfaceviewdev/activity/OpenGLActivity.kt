package com.warchaser.glsurfaceviewdev.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.warchaser.glsurfaceviewdev.R

class OpenGLActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gl)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}