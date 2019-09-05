package com.warchaser.glsurfaceviewdev.activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.app.BaseActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize()
    }

    override fun onDestroy() {
        clearFindViewByIdCache()
        super.onDestroy()
    }

    private fun initialize(){
//        mBtnCamera.setOnClickListener(object : View.OnClickListener{
//            override fun onClick(v: View?) {
//                Intent(this@MainActivity, CameraActivity::class.java).apply {
//                    startActivity(this)
//                }
//            }
//        })

        mBtnPhotoView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Intent(this@MainActivity, PhotoViewActivity::class.java).apply {
                    startActivity(this)
                }
            }
        })

        mBtnLongPress.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Intent(this@MainActivity, TripleOnLongPressActivity::class.java).apply {
                    startActivity(this)
                }
            }
        })

        mBtnNavigationView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Intent(this@MainActivity, DrawerActivity::class.java).apply {
                    startActivity(this)
                }
            }
        })

        mBtnOpenGL.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Intent(this@MainActivity, OpenGLActivity::class.java).apply {
                    startActivity(this)
                }
            }
        })

        mBtnSpecifyFolder.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Intent(this@MainActivity, AlbumActivity::class.java).apply {
                    startActivity(this)
                }
            }
        })
    }


}
