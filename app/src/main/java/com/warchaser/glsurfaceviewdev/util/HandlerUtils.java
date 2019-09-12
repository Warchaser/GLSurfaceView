package com.warchaser.glsurfaceviewdev.util;

import android.os.Handler;

public class HandlerUtils {

    private HandlerUtils(){

    }

    public static void sendMessage(Handler handler, int what, Object object, int arg1, int arg2){
        if(handler == null){
            return;
        }

        if(object == null){
            if(arg1 == -1 && arg2 == -1){
                handler.obtainMessage(what).sendToTarget();
            } else {
                handler.obtainMessage(what, arg1, arg2).sendToTarget();
            }
        } else {
            handler.obtainMessage(what, object).sendToTarget();
        }
    }

}
