package com.warchaser.glsurfaceviewdev.view.photo;

/**
 * 自定义手势监听
 * */
public interface CustomGestureListener {

    void onDrag(float deltaX, float deltaY);

    void onFling(float startC, float startY, float velocityX, float velocityY);

    void onScale(float scaleFactor, float focusX, float focusY);
}
