package com.liveness.dflivenesslibrary.view;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;

import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;

public class TimeViewContoller implements Runnable {
    public static final String TAG = "TimeViewContoller";

    private ITimeViewBase mTimeView;
    private Handler mHandler;
    private float mCurrentTime;
    private int mMaxTime;
    private boolean mStop;
    private CallBack mCallBack;
    private Activity mActivity;

    public interface CallBack {
        void onTimeEnd();
    }

    public TimeViewContoller(Activity activity, ITimeViewBase view) {
        mActivity = activity;
        mTimeView = view;
        HandlerThread handlerThread = new HandlerThread("TimeViewContoller");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        if (mTimeView != null) {
            mMaxTime = mTimeView.getMaxTime();
        }
    }

    @Override
    public void run() {
        if (mStop) {
            return;
        }
        LivenessUtils.logI(TAG, "run", Thread.currentThread().getName(), "mCurrentTime", mCurrentTime);
        if (mCurrentTime > mMaxTime) {
            onTimeEnd();
            return;
        }
        mHandler.postDelayed(this, 50);
        mCurrentTime = mCurrentTime + 0.05f;
        setProgress(mCurrentTime);
    }

    private void setProgress(final float progress) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (mTimeView != null) {
                    mTimeView.setProgress(progress);
                }
            }
        });
    }

    public void stop() {
        mStop = true;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void start() {
        start(true);
    }

    public void start(boolean again) {
        if (!again) {
            if (!mStop) {
                return;
            }
            mStop = false;
            if (mCurrentTime > mMaxTime) {
                onTimeEnd();
                return;
            }
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(this);
        } else {
            reset();
        }
    }

    private void onTimeEnd() {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                hide();
                if (null != mCallBack) {
                    mCallBack.onTimeEnd();
                }
            }
        });
    }

    public void setCallBack(CallBack callback) {
        mCallBack = callback;
    }

    private void reset() {
        show();
        mCurrentTime = 0;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(this);
    }

    public void hide() {
        mStop = true;
        mHandler.removeCallbacksAndMessages(null);
        if (mTimeView != null) {
            mTimeView.hide();
        }
    }

    public void show() {
        mStop = false;
        if (mTimeView != null) {
            mTimeView.show();
        }
    }

    private void runOnUIThread(Runnable runnable) {
        if (mActivity != null) {
            mActivity.runOnUiThread(runnable);
        }
    }

}
