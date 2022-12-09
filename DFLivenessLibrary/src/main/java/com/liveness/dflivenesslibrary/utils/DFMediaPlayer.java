package com.liveness.dflivenesslibrary.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;

import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;

import java.io.IOException;

public class DFMediaPlayer implements MediaPlayer.OnCompletionListener {
    public static final String TAG = "DFMediaPlayer=DFLivenessFragment=";

    private MediaPlayer mMediaPlayer;
    private Handler mHandler;

    public void setMediaSource(Context context, int resId, boolean restart) {
        LivenessUtils.logI(TAG, "setMediaSource");
        release();
        try {
            mHandler = new Handler();
            AssetFileDescriptor fileDescriptor = context.getResources().openRawResourceFd(resId);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            if (restart) {
                restartPrepareAndPlay();
            } else {
                prepareAndPlay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareAndPlay() {
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        LivenessUtils.logI(TAG, "stop");
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(null);
            if (mMediaPlayer.isPlaying()) {
                LivenessUtils.logI(TAG, "stop===2");
                mMediaPlayer.stop();
            }
        }

        removeRepeatPlayMessage();
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        removeRepeatPlayMessage();
    }

    private void removeRepeatPlayMessage() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void start() {
        LivenessUtils.logI(TAG, "start");
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(this);
        }
    }

    private void restartPrepareAndPlay() {
        LivenessUtils.logI(TAG, "restartPrepareAndPlay");
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LivenessUtils.logI(TAG, "onCompletion");
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LivenessUtils.logI(TAG, "onCompletion===postDelayed===");
                    start();
                }
            }, 1000);
        }
    }
}
