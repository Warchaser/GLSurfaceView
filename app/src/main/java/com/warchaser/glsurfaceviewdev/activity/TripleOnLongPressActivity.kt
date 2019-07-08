package com.warchaser.glsurfaceviewdev.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import com.warchaser.glsurfaceviewdev.view.thumbup.LongPressButton
import com.warchaser.glsurfaceviewdev.util.NLog
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.app.BaseActivity
import kotlinx.android.synthetic.main.activity_triple_on_press.*

class TripleOnLongPressActivity : BaseActivity(){

    private var mValueAnimator : ValueAnimator ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_triple_on_press)

        initialize()
    }

    private fun initialize(){
        mBtnLongPress.setOnLongPressListener(object : LongPressButton.OnLongPressListener {
            override fun onLongPressActivated() {
                mBtnCoin.progress = 0
                mBtn2Collection.progress = 0
            }

            override fun onLongPressUpdate(progress: Int) {
                NLog.e("TripleOnLongPressActivity", "OnLongPressListener.onLongPressUpdate: $progress")
                mBtnCoin.progress = progress
                mBtn2Collection.progress = progress
            }

            override fun onLongPressEnded() {
                mBtnCoin.progress = 0
                mBtn2Collection.progress = 0
            }

            override fun onLongPressCancelled() {
                startAnimation(mBtnCoin.progress, 0)
            }

        })
    }

    private fun startAnimation(vararg values : Int){

        if(mValueAnimator == null){
            mValueAnimator = ValueAnimator.ofInt(*values)
            mValueAnimator?.run{
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener{
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        val value = animation?.animatedValue as Int
                        mBtnCoin.progress = value
                        mBtn2Collection.progress = value
                    }
                })
            }

        } else {
            mValueAnimator?.setIntValues(*values)
        }
        
        mValueAnimator?.start()
    }

    override fun onDestroy() {
        mBtnLongPress?.destroy()
        super.onDestroy()
    }

}