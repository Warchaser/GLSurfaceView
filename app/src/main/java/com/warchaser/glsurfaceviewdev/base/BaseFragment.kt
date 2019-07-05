package com.warchaser.glsurfaceviewdev.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.*

abstract class BaseFragment : Fragment(){

    private var mRootView : View ? = null

    protected abstract val mLayoutResourceId : Int

    protected abstract fun onLoadView()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View ?{
        mRootView = if(mLayoutResourceId != 0){
            inflater.inflate(mLayoutResourceId, null)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }

        onLoadView()
        return mRootView
    }

    protected fun <T : View> findById(resId : Int) : T?{
        return mRootView?.findViewById(resId)
    }

    override fun onDestroyView() {
        clearFindViewByIdCache()
        super.onDestroyView()
    }
}