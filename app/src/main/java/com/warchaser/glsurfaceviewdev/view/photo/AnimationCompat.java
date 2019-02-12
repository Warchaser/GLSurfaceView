package com.warchaser.glsurfaceviewdev.view.photo;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import org.jetbrains.annotations.NotNull;

public class AnimationCompat {

    private static final int SIXTY_FPS_INTERNAL = 1000 / 60;

    private AnimationCompat(){

    }

    public static void postOnAnimation(View view, Runnable runnable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            postOnAnimationJellyBean(view, runnable);
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERNAL);
        }
    }

    @TargetApi(16)
    private static void postOnAnimationJellyBean(@NotNull View view, Runnable runnable){
        view.postOnAnimation(runnable);
    }

}
