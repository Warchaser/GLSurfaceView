package com.warchaser.glsurfaceviewdev.fragment.drawer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.warchaser.glsurfaceviewdev.R
import kotlinx.android.synthetic.main.activity_normal.*

class ThemeSelectionActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)

        initialize()
    }

    private fun initialize(){
        mTv.text = getText(R.string.item_theme)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}