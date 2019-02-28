package com.warchaser.glsurfaceviewdev.base

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

}