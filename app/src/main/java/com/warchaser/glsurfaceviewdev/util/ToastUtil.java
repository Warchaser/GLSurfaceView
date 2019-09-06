package com.warchaser.glsurfaceviewdev.util;

import android.content.Context;
import android.widget.Toast;

import com.warchaser.glsurfaceviewdev.base.App;

public class ToastUtil {

    private ToastUtil(){

    }

    public static void showToast(String message){
        Toast.makeText(getAppContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int resId){
        Toast.makeText(getAppContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(String message){
        Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
    }

    public static void showToastLong(int resId){
        Toast.makeText(getAppContext(), resId, Toast.LENGTH_LONG).show();
    }

    private static Context getAppContext(){
        return App.getInstance().getApplicationContext();
    }

}
