package com.warchaser.glsurfaceviewdev.activity

import android.os.Bundle
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.app.BaseActivity

class OpenGLActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gl)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}