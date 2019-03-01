package com.warchaser.glsurfaceviewdev.fragment.drawer

import android.widget.TextView
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.base.LazyFragment

class DownloadFragment : LazyFragment(){

    private var mTv : TextView? = null

    override val mLayoutResourceId: Int
        get() = R.layout.fragment_normal

    override fun onLoadView() {
        mTv = findById(R.id.mTv)
        mTv?.text = getText(R.string.item_downloaded)
    }

}