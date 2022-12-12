package com.liveness.dflivenesslibrary.fragment;

import static com.liveness.dflivenesslibrary.liveness.DFLivenessBaseActivity.KEY_HINT_MESSAGE_FACE_NOT_VALID;
import static com.liveness.dflivenesslibrary.liveness.DFLivenessBaseActivity.KEY_HINT_MESSAGE_HAS_FACE;
import static com.liveness.dflivenesslibrary.liveness.DFLivenessBaseActivity.KEY_HINT_MESSAGE_NO_FACE;

import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liveness.dflivenesslibrary.BiopsyManager;
import com.liveness.dflivenesslibrary.R;
import com.liveness.dflivenesslibrary.callback.DFLivenessResultCallback;
import com.liveness.dflivenesslibrary.fragment.model.DFLivenessOverlayModel;
import com.liveness.dflivenesslibrary.liveness.util.DFSensorManager;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;
import com.liveness.dflivenesslibrary.process.DFLivenessBaseProcess;
import com.liveness.dflivenesslibrary.utils.DFViewShowUtils;
import com.liveness.dflivenesslibrary.view.CircleTimeView;
import com.liveness.dflivenesslibrary.view.DFGifView;
import com.liveness.dflivenesslibrary.view.TimeViewContoller;

import java.util.HashMap;
import java.util.Map;


public class DFLivenessBaseFragment extends DFProductFragmentBase implements Camera.PreviewCallback {
    private static final String TAG = "DFLivenessFragment";

    protected static final int CURRENT_ANIMATION = -1;
    protected DFLivenessBaseProcess mProcess;
    protected DFGifView mGvView;
    protected TextView mNoteTextView;
    protected ViewGroup mVGBottomDots;
    private RelativeLayout mWaitDetectView;
    private View mAnimFrame;
    protected CircleTimeView mTimeView;
    protected String[] mDetectList = null;
    protected TimeViewContoller mTimeViewContoller;
    //    protected DFLivenessSDK.DFLivenessMotion[] mMotionList;
    protected DFSensorManager mSensorManger;
    protected DFLivenessResultCallback mLivenessResultFileProcess;

    protected String mHasFaceHint, mNoFaceHint, mFaceNotValid;
    protected Map<String, DFLivenessOverlayModel> mFaceHintMap;
    protected String mFaceProcessResult;

    protected DFLivenessCallback mLivenessCallback;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.layout_liveness_fragment;
    }

    protected void initView() {
        mGvView = mRootView.findViewById(R.id.id_gv_play_action);
        mNoteTextView = mRootView.findViewById(R.id.noteText);
        mWaitDetectView = mRootView.findViewById(R.id.wait_time_notice);
        mWaitDetectView.setVisibility(View.VISIBLE);
        mAnimFrame = mRootView.findViewById(R.id.anim_frame);
        mAnimFrame.setVisibility(View.INVISIBLE);
//        mVGBottomDots = mRootView.findViewById(R.id.viewGroup);
        View vBack = mRootView.findViewById(R.id.id_ll_back);
        vBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        if (mDetectList != null && mDetectList.length >= 1) {
            for (int i = 0; i < mDetectList.length; i++) {
                TextView tvBottomCircle = new TextView(getActivity());
                tvBottomCircle.setBackgroundResource(R.drawable.drawable_liveness_detect_bottom_cicle_bg_selector);
                tvBottomCircle.setEnabled(i != 0);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp2px(8),
                        dp2px(8));
                layoutParams.leftMargin = dp2px(8);
//                mVGBottomDots.addView(tvBottomCircle, layoutParams);
            }
        }

        mTimeView = mRootView.findViewById(R.id.time_view);
        mTimeViewContoller = new TimeViewContoller(getActivity(), mTimeView);

        mHasFaceHint = getActivity().getIntent().getStringExtra(KEY_HINT_MESSAGE_HAS_FACE);
        mNoFaceHint = getActivity().getIntent().getStringExtra(KEY_HINT_MESSAGE_NO_FACE);
        mFaceNotValid = getActivity().getIntent().getStringExtra(KEY_HINT_MESSAGE_FACE_NOT_VALID);

        initFaceHintMap();
    }

    @Override
    public void initialize() {
        mLivenessResultFileProcess = (DFLivenessResultCallback) getActivity();
        mSensorManger = new DFSensorManager(getActivity());

        mProcess = new DFLivenessBaseProcess(getActivity());
        mProcess.registerLivenessDetectCallback(mLivenessListener);

        mCameraBase.setPreviewCallback(this);
        mCameraBase.addPreviewCallbackBuffer();

        initView();
    }

    private void onBack() {
        finishActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
//        setLivenessState(false);
        mSensorManger.registerListener(mSensorEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        setLivenessState(true);
        mSensorManger.unregisterListener(mSensorEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProcess != null) {
            mProcess.registerLivenessDetectCallback(null);
            mProcess.stopDetect();
            mProcess.exitDetect();

            mProcess = null;
        }
        if (mTimeViewContoller != null) {
            mTimeViewContoller.setCallBack(null);
            mTimeViewContoller = null;
        }
    }

    protected DFLivenessBaseProcess.OnLivenessCallBack mLivenessListener = new DFLivenessBaseProcess.OnLivenessCallBack() {
        @Override
        public void onLivenessDetect(final int value, final int status, byte[] livenessEncryptResult,
                                     final byte[] imageResult, byte[] videoResult) {
            LivenessUtils.logI(TAG, "onLivenessDetect", "value", value);

            if (imageResult != null) {
                onLivenessDetectCallBack(value, status, livenessEncryptResult, imageResult, videoResult);
            }
        }

        @Override
        public void onFaceDetect(int value, boolean hasFace, boolean faceValid) {

        }

        @Override
        public RectF getDetectRegion() {
            return getSilentDetectionRegion(0.05f);
        }

        @Override
        public DFLivenessBaseProcess.CameraInfo getCameraInfo() {
            DFLivenessBaseProcess.CameraInfo cameraInfo = new DFLivenessBaseProcess.CameraInfo();
            cameraInfo.mPreviewWidth = mCameraBase.getPreviewWidth();
            cameraInfo.mPreviewHeight = mCameraBase.getPreviewHeight();
            cameraInfo.mCameraOrientation = mCameraBase.getCameraOrientation();
            return cameraInfo;
        }

        @Override
        public void addPreviewCallbackBuffer() {
            mCameraBase.addPreviewCallbackBuffer();
        }

        @Override
        public void onErrorHappen(int error) {
            mCameraBase.onErrorHappen(error);
        }
    };

    protected void removeDetectWaitUI() {
        mWaitDetectView.setVisibility(View.GONE);
        setLivenessState(false);
        mAnimFrame.setVisibility(View.VISIBLE);
        if (!isSilent()) {
            int value = BiopsyManager.DFLivenessMotion.NONE.getValue();
            onLivenessDetectCallBack(value, 0, null, null, null);
        }
    }

    protected void showDetectWaitUI() {
        mWaitDetectView.setVisibility(View.VISIBLE);
        if (mTimeViewContoller != null) {
            mTimeViewContoller.setCallBack(null);
        }
    }

    protected boolean isSilent() {
        return false;
    }

    protected void setLivenessState(boolean pause) {
        if (null == mProcess) {
            return;
        }
        if (pause) {
            mProcess.stopLiveness();
        } else {
            mProcess.startLiveness();
        }
    }

    protected String getIntentString(String key) {
        return getActivity().getIntent().getStringExtra(key);
    }

    protected String getHint(String hintKey, int defaultHintResId) {
        String blinkHint = getIntentString(hintKey);
        if (TextUtils.isEmpty(blinkHint)) {
            blinkHint = getStringWithID(defaultHintResId);
        }
        return blinkHint;
    }

    protected void onLivenessDetectCallBack(final int value, final int status, final byte[] livenessEncryptResult,
                                            final byte[] imageResult, byte[] videoResult) {

    }

    protected void onFaceDetectCallback(int value, boolean hasFace, boolean faceValid) {
        if (value == BiopsyManager.DFLivenessMotion.HOLD_STILL.getValue()) {
            String hasFaceShow = DFViewShowUtils.booleanTrans(hasFace);
            String faceValidShow = DFViewShowUtils.booleanTrans(faceValid);
            String faceProcessResult = hasFaceShow.concat("_").concat(faceValidShow);
            if (!TextUtils.equals(mFaceProcessResult, faceProcessResult)) {
                mFaceProcessResult = faceProcessResult;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LivenessUtils.logI(TAG, "mFaceProcessResult", mFaceProcessResult);
                        DFLivenessOverlayModel overlayModel = mFaceHintMap.get(mFaceProcessResult);
                        String hintID = overlayModel.getShowHint();
                        int borderColor = overlayModel.getBorderColor();
                        if (borderColor != -1) {
                            mOverlayView.showBorder();
                            mOverlayView.setBorderColor(borderColor);
                        }
                        refreshHintText(hintID);
                    }
                });

            }

//            if (hasFace) {
//                mOverlayView.setFaceRect(new RectF(rect.left, rect.top, rect.right, rect.bottom), mCameraBase.getCameraOrientation());
//            } else {
//                mOverlayView.setFaceRect(new RectF(0, 0, 0,0), mCameraBase.getCameraOrientation());
//            }
        } else {
            mOverlayView.hideBorder();
        }
    }

    protected void refreshHintText(String hintStr) {
        if (hintStr != null) {
            mNoteTextView.setText(hintStr);
        }
    }

    protected int isBottomDotsVisibility() {
        return View.VISIBLE;
    }

    protected void showIndicateView() {
        if (mGvView != null) {
            mGvView.setVisibility(View.VISIBLE);
        }
//        if (mVGBottomDots != null) {
//            mVGBottomDots.setVisibility(isBottomDotsVisibility());
//        }
        if (mNoteTextView != null && !isSilent()) {
            mNoteTextView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideTimeContoller() {
        if (mTimeViewContoller != null) {
            mTimeViewContoller.hide();
        }
    }

    protected void hideIndicateView() {
        if (mGvView != null) {
            mGvView.setVisibility(View.GONE);
        }
//        if (mVGBottomDots != null) {
//            mVGBottomDots.setVisibility(View.GONE);
//        }
        if (mNoteTextView != null) {
            mNoteTextView.setVisibility(View.GONE);
        }
    }

    protected void startAnimation(int animation) {
        if (animation != CURRENT_ANIMATION && mGvView != null) {
            mGvView.setMovieResource(animation);
        }
    }

    protected void startCountdown() {
        if (mTimeViewContoller != null) {
            mTimeViewContoller.start();
            mTimeViewContoller.setCallBack(() -> mProcess.onTimeEnd());
        }
    }

    protected void stopCountDown() {
        if (mTimeViewContoller != null) {
            mTimeViewContoller.stop();
            mTimeViewContoller.setCallBack(null);
        }
    }


    protected void updateUi(int stringId, int animationId, int number) {
        String stringWithID = getStringWithID(stringId);
        updateUi(stringWithID, animationId, number);
    }

    protected void updateUi(String string, int animationId, int number) {
        LivenessUtils.logI(TAG, "mNoteTextView", "stringId", string);
        mNoteTextView.setText(string);
        if (animationId != 0) {
            startAnimation(animationId);
        }
        if (number >= 0) {
//            View childAt = mVGBottomDots.getChildAt(number);
//            childAt.setEnabled(false);
        }
    }

    protected String getStringWithID(int id) {
        return getResources().getString(id);
    }

    protected SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mProcess.addSequentialInfo(event.sensor.getType(), event.values);
        }
    };

    protected void initFaceHintMap() {
        mFaceHintMap = new HashMap<>();
        mFaceHintMap.put("0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED));
        mFaceHintMap.put("0_1", new DFLivenessOverlayModel(mNoFaceHint, Color.RED));
        mFaceHintMap.put("1_0", new DFLivenessOverlayModel(mFaceNotValid, Color.RED));
        mFaceHintMap.put("1_1", new DFLivenessOverlayModel(mHasFaceHint, Color.GREEN));
    }

    public void setLivenessCallback(DFLivenessCallback mLivenessCallback) {
        this.mLivenessCallback = mLivenessCallback;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mProcess != null) {
            mProcess.onPreviewFrame(data, camera);
        }
    }

    public interface DFLivenessCallback {
        void startDetect();
    }

}
