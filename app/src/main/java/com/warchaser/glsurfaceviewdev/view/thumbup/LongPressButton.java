package com.warchaser.glsurfaceviewdev.view.thumbup;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LongPressButton extends android.support.v7.widget.AppCompatButton implements View.OnLongClickListener {

    private MessageRunnable mRunnable;
    private MessageHandler mMessageHandler;

    private final int TIME_DURATION = 17;
    private final int MAX_PROGRESS = 360;
    private final int STEP_LENGTH = 4;

    private static final int LONG_PRESS_ACTIVATED = 0x001;

    private static final int LONG_PRESS_UPDATE = 0x002;

    private static final int LONG_PRESS_ENDED = 0x003;

    private static final int LONG_PRESS_CANCELLED = 0x004;

    private OnLongPressListener mOnLongPressListener;

    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public LongPressButton(Context context) {
        super(context);
        initialize();
    }

    public LongPressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public LongPressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize(){

        mRunnable = new MessageRunnable();
        mMessageHandler = new MessageHandler(this);

        setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        if(mRunnable != null){
            mSingleThreadExecutor.execute(mRunnable);
        }
        return false;
    }

    private void sendMessage(int type, int currentProgress){
        if(mMessageHandler == null){
            return;
        }

        mMessageHandler.obtainMessage(type, currentProgress).sendToTarget();
    }

    public void setOnLongPressListener(OnLongPressListener listener){
        this.mOnLongPressListener = listener;
    }

    public void destroy(){
        if(mMessageHandler != null){
            mMessageHandler.removeCallbacks(mRunnable);
            mMessageHandler.removeCallbacksAndMessages(null);
            mMessageHandler = null;
        }

        if(mSingleThreadExecutor != null && !mSingleThreadExecutor.isShutdown()){
            mSingleThreadExecutor.shutdown();
        }

        mRunnable = null;

        mOnLongPressListener = null;
    }

    private class MessageRunnable implements Runnable{

        private int mNum = 0;
        @Override
        public void run() {
            mNum = 0;
            sendMessage(LONG_PRESS_ACTIVATED, -1);
            while (isPressed() && mNum <= MAX_PROGRESS){
                sendMessage(LONG_PRESS_UPDATE, mNum);
                mNum += STEP_LENGTH;
                SystemClock.sleep(TIME_DURATION);
            }

            if(mNum < MAX_PROGRESS){
                sendMessage(LONG_PRESS_CANCELLED, - 1);
            } else {
                sendMessage(LONG_PRESS_ENDED, -1);
            }

        }
    }

    private static class MessageHandler extends Handler{

        private final WeakReference<LongPressButton> mWeakReference;

        MessageHandler(LongPressButton longPressButton){
            mWeakReference = new WeakReference<>(longPressButton);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final LongPressButton longPressButton = mWeakReference.get();
            if(longPressButton.mOnLongPressListener == null){
                return;
            }

            switch (msg.what){
                case LONG_PRESS_ACTIVATED:
                    longPressButton.mOnLongPressListener.onLongPressActivated();
                    break;
                case LONG_PRESS_UPDATE:
                    longPressButton.mOnLongPressListener.onLongPressUpdate((Integer) msg.obj);
                    break;
                case LONG_PRESS_ENDED:
                    longPressButton.mOnLongPressListener.onLongPressEnded();
                    break;
                case LONG_PRESS_CANCELLED:
                    longPressButton.mOnLongPressListener.onLongPressCancelled();
                    break;
                default:
                    break;
            }
        }
    }

    public interface OnLongPressListener{

        void onLongPressActivated();

        void onLongPressUpdate(int progress);

        void onLongPressEnded();

        void onLongPressCancelled();
    }
}
