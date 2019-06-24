package com.warchaser.glsurfaceviewdev.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.fragment.drawer.GeneralInDynamicFragment
import com.warchaser.glsurfaceviewdev.fragment.drawer.HotInDynamicFragment
import com.warchaser.glsurfaceviewdev.fragment.drawer.VideosInDynamicFragment

class DynamicFragmentAdapter(fm : FragmentManager, context : Context) : FragmentPagerAdapter(fm){

    private val mTitles : Array<String> = context.resources.getStringArray(R.array.dynamic_titles)
    private val mFragment : Array<Fragment?>

    init {
        mFragment = arrayOfNulls(mTitles.size)
    }

    override fun getItem(p0: Int): Fragment? {
        if(mFragment[p0] == null){
            when(p0){
                0-> mFragment[p0] = VideosInDynamicFragment()
                1-> mFragment[p0] = GeneralInDynamicFragment()
                2-> mFragment[p0] = HotInDynamicFragment()
                else-> {

                }
            }
        }

        return mFragment[p0]
    }

    override fun getCount(): Int {
        return mTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }


}