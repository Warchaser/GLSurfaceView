package com.warchaser.glsurfaceviewdev.fragment.drawer

import androidx.viewpager.widget.ViewPager
import com.flyco.tablayout.SlidingTabLayout
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.adapter.HomeFragmentAdapter
import com.warchaser.glsurfaceviewdev.base.LazyFragment

class HomeFragment : LazyFragment(){

    private var mViewPager : ViewPager? = null
    private var mSlidingTabLayout : SlidingTabLayout ? = null

    override val mLayoutResourceId: Int
        get() = R.layout.fragment_home

    override fun onLoadView() {
        mSlidingTabLayout = findById(R.id.mSlidingTabLayout)
        mViewPager = findById(R.id.mViewPager)

        val adapter = HomeFragmentAdapter(childFragmentManager, activity?.applicationContext!!)
        mViewPager?.run {
            offscreenPageLimit = 4
            this.adapter = adapter
        }

        mSlidingTabLayout?.setViewPager(mViewPager)
        mViewPager?.currentItem = 1
    }

}