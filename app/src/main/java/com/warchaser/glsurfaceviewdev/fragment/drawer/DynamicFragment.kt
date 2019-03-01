package com.warchaser.glsurfaceviewdev.fragment.drawer

import android.support.v4.view.ViewPager
import com.flyco.tablayout.SlidingTabLayout
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.adapter.DynamicFragmentAdapter
import com.warchaser.glsurfaceviewdev.base.LazyFragment

class DynamicFragment : LazyFragment(){

    private var mViewPager : ViewPager? = null
    private var mSlidingTabLayout : SlidingTabLayout? = null

    override val mLayoutResourceId: Int
        get() = R.layout.fragment_dynamic

    override fun onLoadView() {
        mSlidingTabLayout = findById(R.id.mSlidingTabLayout)
        mViewPager = findById(R.id.mViewPager)

        val adapter = DynamicFragmentAdapter(childFragmentManager, activity?.applicationContext!!)
        mViewPager?.run {
            offscreenPageLimit = 3
            this.adapter = adapter
        }

        mSlidingTabLayout?.setViewPager(mViewPager)
        mViewPager?.currentItem = 1
    }

}