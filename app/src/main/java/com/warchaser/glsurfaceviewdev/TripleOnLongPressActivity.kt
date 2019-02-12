package com.warchaser.glsurfaceviewdev

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AccelerateDecelerateInterpolator
import kotlinx.android.synthetic.main.activity_triple_on_press.*

class TripleOnLongPressActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_triple_on_press)

        initialize()
    }

    private fun initialize(){
        mBtnLongPress.setOnLongPressListener(object : LongPressButton.OnLongPressListener{
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
                val widthAnimation = ValueAnimator.ofInt(mBtnCoin.progress, 0)
                widthAnimation.duration = 1000
                widthAnimation.interpolator = AccelerateDecelerateInterpolator()
                widthAnimation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener{
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        val value = animation?.animatedValue as Int
                        mBtnCoin.progress = value
                        mBtn2Collection.progress = value
                    }
                })

                widthAnimation.start()
            }

        })
    }

    override fun onDestroy() {
        mBtnLongPress?.destroy()
        super.onDestroy()
    }

}