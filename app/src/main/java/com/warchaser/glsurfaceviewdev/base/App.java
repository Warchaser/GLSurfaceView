package com.warchaser.glsurfaceviewdev.base;

import android.app.Application;

import com.warchaser.glsurfaceviewdev.util.NLog;

public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        NLog.initLogFile(this);
    }

    public static App getInstance(){
        return mInstance;
    }
}
