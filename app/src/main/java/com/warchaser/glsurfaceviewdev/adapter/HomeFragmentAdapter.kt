package com.warchaser.glsurfaceviewdev.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.fragment.drawer.HotNewsInHomeFragment
import com.warchaser.glsurfaceviewdev.fragment.drawer.LiveInHomeFragment
import com.warchaser.glsurfaceviewdev.fragment.drawer.OrderAnimationInHomeFragment
import com.warchaser.glsurfaceviewdev.fragment.drawer.RecommendationInHomeFragment

class HomeFragmentAdapter(fm : FragmentManager, context : Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    private val mTitles : Array<String> = context.resources.getStringArray(R.array.home_titles)
    private val mFragments : Array<Fragment?>

    init {
        mFragments = arrayOfNulls(mTitles.size)
    }

    override fun getItem(p0: Int): Fragment {
        if(mFragments[p0] == null){
            when(p0){
                0-> mFragments[p0] = LiveInHomeFragment()
                1-> mFragments[p0] = RecommendationInHomeFragment()
                2-> mFragments[p0] = HotNewsInHomeFragment()
                3-> mFragments[p0] = OrderAnimationInHomeFragment()
                else->{

                }
            }
        }

        return mFragments[p0]!!
    }

    override fun getCount(): Int {
        return mTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }


}