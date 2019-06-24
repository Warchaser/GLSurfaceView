package com.warchaser.glsurfaceviewdev.base

import com.warchaser.glsurfaceviewdev.util.NLog

abstract class LazyFragment : BaseFragment(){


    private var mIsVisible : Boolean = false

    private var mIsPrepared : Boolean = false

    private var mIsVisibleOnce : Boolean = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(userVisibleHint){
            mIsVisible = true
            onVisible()
        } else {
            mIsVisible = false
            onInvisible()
        }
        NLog.e("LazyFragment", getSimpleClassName() + "setUserVisibleHint." + isVisibleToUser)
    }

    /**
     * fragment显示时才加载数据
     * */
    protected open fun onVisible(){
        if(!mIsVisibleOnce){
            onVisibleOnce()
            mIsVisibleOnce = true
        } else {
            lazyLoad()
        }
    }

    /**
     * fragment隐藏
     * */
    protected open fun onInvisible(){

    }

    protected open fun lazyLoad(){

    }

    protected open fun onVisibleOnce(){

    }

    private fun getSimpleClassName(): String {
        val clazz = this.javaClass
        val str1 = clazz.name.replace("$", ".")
        val str2 = str1.replace(clazz.getPackage().name, "") + "."

        return str2.substring(1)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        NLog.e("LazyFragment", getSimpleClassName() + "onHiddenChanged." + hidden)

    }

}