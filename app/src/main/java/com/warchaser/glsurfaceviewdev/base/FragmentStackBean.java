package com.warchaser.glsurfaceviewdev.base;

import androidx.fragment.app.Fragment;

/**
 * Fragment 栈管理Bean
 * */
public class FragmentStackBean {

    private String tag;

    private Fragment fragment;

    public FragmentStackBean(String tag, Fragment fragment){
        setTag(tag);
        setFragment(fragment);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}
