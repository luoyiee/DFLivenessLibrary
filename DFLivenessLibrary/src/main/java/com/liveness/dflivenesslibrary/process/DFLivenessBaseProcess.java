package com.liveness.dflivenesslibrary.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.liveness.DFLivenessBaseActivity;
import com.liveness.dflivenesslibrary.liveness.util.Constants;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;
import com.liveness.dflivenesslibrary.utils.DFViewShowUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DFLivenessBaseProcess implements PreviewCallback {
    private static final String TAG = "DFLivenessBaseProcess";
    //TODO will changed to use the web return value
    private static final int DETECT_WAIT_TIME = 1000;
    private static final boolean DEBUG_PREVIEW = false;

    protected OnLivenessCallBack mListener;
    private boolean mIsKilled = false;
    public boolean mPaused = true;
    private boolean mNV21DataIsReady = false;
    protected byte[] mNv21;
    protected DFLivenessSDK.DFLivenessMotion[] mMotionList;
    private boolean mLiveResult[];
    protected int mCurrentMotion = 0;
    protected DFLivenessSDK mDetector = null;
    private long mStartTime;
    private int mFrameCount = 0;
    private boolean mIsFirstPreviewFrame = true;
    private long mFirstFrameTime;
    private boolean mBeginShowWaitUIBoolean = true;
    private boolean mEndShowWaitUIBoolean = false;
    protected boolean mIsDetectorStartSuccess = false;
    public boolean mIsCreateHandleSuccess = false;
    private ExecutorService mDetectorExecutor;
    protected Intent mIntent;
    private WeakReference<Context> mContext;

    private static final int START_LIVENESS_ERROR = 1;
    private static final int ADD_PREIVEW_BUFFER = 2;
    private static final int FACE_DETECT_RESULT = 3;
    private static final int DETECTION_SUCCESS = 4;
    private static final int RESTART_DETECTOR = 5;
    private static final int DETECTION_TIMEOUT = 6;
    private CameraInfo mCameraInfo;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_LIVENESS_ERROR:
                    int error = msg.arg1;
                    createHandleError(error);
                    break;
                case ADD_PREIVEW_BUFFER:
                    if (mListener != null) {
                        mListener.addPreviewCallbackBuffer();
                    }
                    break;
                case FACE_DETECT_RESULT:
                    DFLivenessSDK.DFStatus status = (DFLivenessSDK.DFStatus) msg.obj;
                    DFLivenessSDK.DFRect faceRect = status.getFaceRect();
                    if (mListener != null) {
                        int currentMotion = msg.arg1;
                        mListener.onFaceDetect(mMotionList[currentMotion].getValue(), status.isHasFace(), status.isFaceValid(), faceRect);
                    }
                    break;
                case DETECTION_SUCCESS:
                    int livenessSuccess = msg.arg1;
                    if (mListener != null) {
                        mListener.onLivenessDetect(livenessSuccess,
                                mCurrentMotion, getLivenessResult(), getImageResult(), getVideoResult());
                        releaseDetector();
                        stopDetect();
                    }
                    break;
                case DETECTION_TIMEOUT:
                    if (mListener != null) {
                        mListener.onLivenessDetect(msg.arg1,
                                mCurrentMotion, getLivenessResult(), getImageResult(), null);
                    }
                    break;
                case RESTART_DETECTOR:
                    if (mListener != null && mCurrentMotion <= mMotionList.length - 1) {
                        mListener.onLivenessDetect(mMotionList[mCurrentMotion].getValue(), mCurrentMotion, null, null, null);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public DFLivenessBaseProcess(Activity context) {
        mIntent = context.getIntent();
        mContext = new WeakReference<Context>(context);
        setMotionList();

        mIsKilled = false;

        String sdkVersion = DFLivenessSDK.getSDKVersion();
        LivenessUtils.logI(TAG, "sdkVersion", sdkVersion);
    }

    protected DFLivenessSDK.DFLivenessOutputType getOutputType(Bundle bundle) {
        return DFLivenessSDK.DFLivenessOutputType.getOutputTypeByValue(Constants.MULTIIMG);
    }

    protected int getLivenessConfig(Intent intent) {
        Bundle bundle = intent.getExtras();
        DFLivenessSDK.DFLivenessOutputType outputType = getOutputType(bundle);

        return outputType.getValue();
    }

    private static class LivenessRunnable implements Runnable {
        private WeakReference<DFLivenessBaseProcess> mWeakRef;

        public LivenessRunnable(DFLivenessBaseProcess process) {
            mWeakRef = new WeakReference<>(process);
        }

        @Override
        public void run() {
            DFLivenessBaseProcess process = mWeakRef.get();
            if (process != null) {
                process.doRunnable();
            }
        }
    }

    private void doRunnable() {
        while (!mIsKilled) {
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mPaused) {
                continue;
            }
            if (!mPaused && mEndShowWaitUIBoolean) {
                synchronized (this) {
                    startLivenessIfNeed();
                }
                doDetect();
                mNV21DataIsReady = false;
            }
        }

        if (mDetector != null) {
            releaseDetector();
        }
    }

    LivenessRunnable mRunnable;

    public void startDetector() {

        if (mDetectorExecutor == null) {
            mDetectorExecutor = Executors.newSingleThreadExecutor();
        }

        mRunnable = new LivenessRunnable(this);

        mDetectorExecutor.execute(mRunnable);
    }

    private void releaseDetector() {
        synchronized (this) {
            if (mDetector != null) {

                mDetector.end();
                mDetector.destroy();
                mDetector = null;
                mRunnable = null;

            }
        }
    }

    /**
     * do liveness detecting
     */
    protected void doDetect() {

        DFLivenessSDK.DFStatus status = null;
        if (mDetector != null) {
            try {
                if (mCurrentMotion < mMotionList.length) {
                    if (mIsDetectorStartSuccess) {
                        synchronized (mNv21) {
                            status = mDetector.detect(mNv21,
                                    DFLivenessSDK.PIX_FMT_NV21,
                                    mCameraInfo.mPreviewWidth,
                                    mCameraInfo.mPreviewHeight,
                                    mCameraInfo.mCameraOrientation,
                                    mMotionList[mCurrentMotion],
                                    isHoldStill(mMotionList[mCurrentMotion]));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (status != null && status.getDetectStatus() != DFLivenessSDK.DFDetectStatus.FRAME_SKIP.getValue()) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(FACE_DETECT_RESULT);
                msg.obj = status;
                msg.arg1 = mCurrentMotion;
                mHandler.sendMessage(msg);
            }

            LivenessUtils.logI(TAG, "hanlz_doDetect", "getDetectStatus", status.getDetectStatus());
            LivenessUtils.logI(TAG, "hanlz_doDetect", "isPassed", status.isPassed());

            if (mCurrentMotion < mMotionList.length) {
                //The first action(hold_still) does not have track_miss
                if (!isHoldStill(mMotionList[mCurrentMotion])
                        && status.getDetectStatus() == DFLivenessSDK.DFDetectStatus.TRACKING_MISSED.getValue()) {
                    detectTimeout(Constants.LIVENESS_TRACKING_MISSED);
                }
            }

            if (status.getDetectStatus() == DFLivenessSDK.DFDetectStatus.PASSED.getValue() && status.isPassed()) {
                if (mCurrentMotion < mMotionList.length) {
                    mLiveResult[mCurrentMotion] = true;
                    if (mLiveResult[mCurrentMotion]) {
                        mCurrentMotion++;
                        mDetector.detect(mNv21,
                                DFLivenessSDK.PIX_FMT_NV21,
                                mCameraInfo.mPreviewWidth,
                                mCameraInfo.mPreviewHeight,
                                mCameraInfo.mCameraOrientation,
                                DFLivenessSDK.DFLivenessMotion.NONE,
                                isHoldStill(DFLivenessSDK.DFLivenessMotion.NONE));
                        if (mCurrentMotion == mMotionList.length) {
                            finishDetect(Constants.LIVENESS_SUCCESS, mCurrentMotion);
                        } else {
                            restartDetect(true);
                        }
                    }
                }
            }
        }
        mHandler.sendEmptyMessage(ADD_PREIVEW_BUFFER);
    }

    protected boolean isHoldStill(DFLivenessSDK.DFLivenessMotion motion) {
        return motion == DFLivenessSDK.DFLivenessMotion.HOLD_STILL;
    }

    private void detectTimeout(int livenessDetectStatus) {
        stopLiveness();
        Message msg = mHandler.obtainMessage(DETECTION_TIMEOUT);
        msg.arg1 = livenessDetectStatus;
        mHandler.sendMessage(msg);
    }

    private void finishDetect(int livenessSuccess, int mCurrentMotion) {
        stopLiveness();
        Message msg = mHandler.obtainMessage(DETECTION_SUCCESS);
        msg.arg1 = livenessSuccess;
        mHandler.sendMessage(msg);
    }

    public void stopDetect() {
        mIsKilled = true;
    }

    public void exitDetect() {
        stopDetectThread();
        if (mNv21 != null) {
            mNv21 = null;
        }
    }

    private byte[] getLivenessResult() {
        try {
            synchronized (this) {
                if (mDetector != null) {
                    mDetector.end();
                    return mDetector.getLivenessResult();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private DFLivenessSDK.DFLivenessImageResult[] getImageResult() {
        try {
            synchronized (this) {
                if (mDetector != null) {
                    mDetector.end();
                    return mDetector.getImageResult();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getVideoResult() {
        try {
            synchronized (this) {
                if (mDetector != null) {
                    mDetector.end();
                    return mDetector.getVideoResult();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * set the WrapperStaticInfo here.
     */
    public void setWrapperStaticInfo() {
        try {
            mDetector.setStaticInfo(DFLivenessSDK.DFWrapperStaticInfo.DEVICE.getValue(), android.os.Build.MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mDetector.setStaticInfo(DFLivenessSDK.DFWrapperStaticInfo.OS.getValue(), "Android");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mDetector.setStaticInfo(DFLivenessSDK.DFWrapperStaticInfo.SYS_VERSION.getValue(),
                    android.os.Build.VERSION.RELEASE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mDetector.setStaticInfo(DFLivenessSDK.DFWrapperStaticInfo.ROOT.getValue(), String.valueOf(LivenessUtils.isRootSystem()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mContext.get() != null) {
                mDetector.setStaticInfo(DFLivenessSDK.DFWrapperStaticInfo.CUSTOMER.getValue(), mContext.get().getApplicationContext().getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initStateAndPreviewCallBack() {
        mCurrentMotion = 0;
        if (mListener != null) {
            mCameraInfo = mListener.getCameraInfo();
            initNv21Data();
        }
    }

    public void initNv21Data() {
        mNv21 = null;
        mNv21 = new byte[mCameraInfo.mPreviewWidth * mCameraInfo.mPreviewHeight * 3
                / 2];
    }

    private void startLivenessIfNeed() {
        if (mDetector == null) {
            try {
                if (mContext.get() != null) {
                    mDetector = new DFLivenessSDK(mContext.get());
                    int createResultCode = mDetector.createHandle();
                    mIsCreateHandleSuccess = createResultCode == DFLivenessSDK.DF_LIVENESS_INIT_SUCCESS;
                    if (mIsCreateHandleSuccess) {
                        mIsDetectorStartSuccess = mDetector.start(getLivenessConfig(mIntent), mMotionList);
                        setDetectorParameters(mDetector);

                        if (mIsDetectorStartSuccess) {
                            setWrapperStaticInfo();
                        }
                    } else {
                        Message msg = mHandler.obtainMessage(START_LIVENESS_ERROR);
                        msg.arg1 = createResultCode;
                        mHandler.sendMessage(msg);
                    }
                }
            } catch (Throwable e) {
                Message msg = mHandler.obtainMessage(START_LIVENESS_ERROR);
                msg.arg1 = DFLivenessBaseActivity.RESULT_CREATE_HANDLE_ERROR;
                mHandler.sendMessage(msg);
            }
        }
    }

    private void createHandleError(int resultCode) {
        DFLivenessSDKError livenessSDKError = DFLivenessSDKError.getLivenessSDKError(resultCode);
        if (livenessSDKError != null) {
            Context context = mContext.get();
            String errorHint = DFViewShowUtils.getResourceString(context, livenessSDKError.getErrorHintResId());
            DFViewShowUtils.showToast(context, errorHint);
        }
        if (mListener != null) {
            mListener.onErrorHappen(resultCode);
        }
    }

    protected void setDetectorParameters(DFLivenessSDK detector) {
        /*
         * Set liveness motion's threshold
         * WARNING: this MUST be invoked after @start(int config, DFLivenessMotion[] motions) function.
         *
         * @param key, see DFLivenessKey definition:
         *         KEY_HOLD_STILL_FRAME: The interval number frames which HOLD_STILL motion do checking face position, default is 10
         *         KEY_HOLD_STILL_POS: The IOU value which calculate the current face with the initial face position. default is 0.95
         * @param value, [KEY_BLINK_KEY, KEY_MOUTH_KEY, KEY_YAW_KEY, KEY_PITCH_KEY, KEY_HOLD_STILL_POS]'s value must be in [0.f, 1.f]
         */

        if (haveSilentMotion()) {
            setSilentDetectorParameters(detector);
            setSilentDetectionRegion(detector);
        }
    }

    protected void setSilentDetectorParameters(DFLivenessSDK detector) {
        detector.setThreshold(DFLivenessSDK.DFLivenessKey.KEY_HOLD_STILL_DETECT_NUMBER, 1.f);
        detector.setThreshold(DFLivenessSDK.DFLivenessKey.KEY_HOLD_STILL_FACE_RET_MAX_RATE, 0.60f);
        detector.setThreshold(DFLivenessSDK.DFLivenessKey.KEY_BLUR_THRESHOLD, 0.30f);
    }

    /**
     * This function is used to calculate the circle region in detection UI.
     * The region is used to limit the detection range of silent, if there is a human face in this region,
     * we will gather clear images of this face.
     * The default region is the image size.
     *
     * @param detector liveness detector
     */
    protected void setSilentDetectionRegion(DFLivenessSDK detector) {

        try {
            if (mListener != null) {
                detector.setDetectRegion(mListener.getDetectRegion());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopDetectThread() {
        mIsCreateHandleSuccess = false;
        if (mDetectorExecutor != null) {
            mDetectorExecutor.shutdownNow();
            try {
                mDetectorExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDetectorExecutor = null;
        }
    }

    void restartDetect(boolean bRestartTime) {
        if (bRestartTime) {
            mHandler.sendEmptyMessage(RESTART_DETECTOR);
        }
    }

    public void resetStatus(boolean fAlert) {
        boolean bRestartTime = fAlert;
        if (mCurrentMotion > 0) {
            bRestartTime = true;
        }
        resetLivenessResult();
        mCurrentMotion = 0;
        restartDetect(bRestartTime);
    }

    private void resetLivenessResult() {
        int count = mLiveResult.length;
        for (int i = 0; i < count; i++) {
            mLiveResult[i] = false;
        }
    }

    private void setMotionList() {
        mMotionList = getMotionList();

        if (mMotionList != null && mMotionList.length > 0) {
            mLiveResult = new boolean[mMotionList.length];
            for (int i = 0; i < mMotionList.length; i++) {
                mLiveResult[i] = false;
            }
        }
    }

    protected boolean haveSilentMotion() {
        boolean haveSilentMotion = false;
        if (mMotionList != null) {
            for (DFLivenessSDK.DFLivenessMotion livenessMotion : mMotionList) {
                if (livenessMotion == DFLivenessSDK.DFLivenessMotion.HOLD_STILL) {
                    haveSilentMotion = true;
                    break;
                }
            }
        }
        return haveSilentMotion;
    }

    protected DFLivenessSDK.DFLivenessMotion[] getMotionList() {
        return null;
    }

    public void registerLivenessDetectCallback(OnLivenessCallBack callback) {
        mListener = callback;
    }

    public void onTimeEnd() {
        detectTimeout(Constants.LIVENESS_TIME_OUT);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (DEBUG_PREVIEW) {
            debugFps();
        }

        if (mDetectorExecutor == null) {
            initStateAndPreviewCallBack();
            startDetector();
        }

        if (mIsFirstPreviewFrame) {
            mFirstFrameTime = System.currentTimeMillis();
            mIsFirstPreviewFrame = false;
        }
        long intervalTime = System.currentTimeMillis() - mFirstFrameTime;
        if (intervalTime <= DETECT_WAIT_TIME) {
            if (mBeginShowWaitUIBoolean) {
                mListener.onLivenessDetect(Constants.DETECT_BEGIN_WAIT,
                        1, null, null, null);
                mBeginShowWaitUIBoolean = false;
            }
            if (mListener != null) {
                mListener.addPreviewCallbackBuffer();
            }
        } else {
            if (!mEndShowWaitUIBoolean) {
                mListener
                        .onLivenessDetect(Constants.DETECT_END_WAIT, 1, null, null, null);
                mEndShowWaitUIBoolean = true;
                startLiveness();
            }
            if (!mPaused && !mNV21DataIsReady && mNv21 != null) {
                synchronized (mNv21) {
                    if (data != null && mNv21.length >= data.length) {
                        System.arraycopy(data, 0, mNv21, 0, data.length);
                        mNV21DataIsReady = true;
                    }
                }
            }
        }
    }

//    private void previewImage(byte[] previewData){
//        try {
//            Bitmap previewBitmap = DFBitmapUtils.convertNv21ToBmp(previewData, mCameraBase.getPreviewWidth(), mCameraBase.getPreviewHeight());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static class CameraInfo {
        public int mPreviewWidth;
        public int mPreviewHeight;
        public int mCameraOrientation;
    }

    public interface OnLivenessCallBack {
        void onLivenessDetect(int value, int status, byte[] livenessEncryptResult,
                              DFLivenessSDK.DFLivenessImageResult[] imageResult, byte[] videoResult);

        void onFaceDetect(int value, boolean hasFace, boolean faceValid, DFLivenessSDK.DFRect rect);

        RectF getDetectRegion();

        CameraInfo getCameraInfo();

        void addPreviewCallbackBuffer();

        void onErrorHappen(int error);
    }

    public void stopLiveness() {
        mPaused = true;
    }

    public void startLiveness() {
        resetStatus(false);
        mPaused = false;
    }

    public void addSequentialInfo(int type, float[] values) {
        if (!mPaused && mDetector != null
                && mIsCreateHandleSuccess) {
            StringBuilder sb = new StringBuilder();
            sb.append(values[0])
                    .append(" ")
                    .append(values[1])
                    .append(" ")
                    .append(values[2])
                    .append(" ");
            DFLivenessSDK.DFWrapperSequentialInfo sequentialInfo = null;
            switch (type) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    sequentialInfo = DFLivenessSDK.DFWrapperSequentialInfo.MAGNETIC_FIELD;
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    sequentialInfo = DFLivenessSDK.DFWrapperSequentialInfo.ACCLERATION;
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    sequentialInfo = DFLivenessSDK.DFWrapperSequentialInfo.ROTATION_RATE;
                    break;
                case Sensor.TYPE_GRAVITY:
                    sequentialInfo = DFLivenessSDK.DFWrapperSequentialInfo.GRAVITY;
                    break;
                default:
                    break;
            }
            try {
                if (sequentialInfo != null) {
                    mDetector
                            .addSequentialInfo(sequentialInfo
                                    .getValue(), sb.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void debugFps() {
        if (mFrameCount == 0) {
            mStartTime = System.currentTimeMillis();
        }
        mFrameCount++;
        long testTime = System.currentTimeMillis() - mStartTime;
        if (testTime > 1000) {
            Log.i(TAG, "onPreviewFrame FPS = " + mFrameCount);
//            Toast.makeText(mContext, "FPS: " + mFrameCount, Toast.LENGTH_SHORT).show();
            mFrameCount = 0;
        }
    }
}
