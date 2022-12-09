package com.liveness.dflivenesslibrary.fragment;

import static com.liveness.dflivenesslibrary.liveness.DFActionLivenessActivity.EXTRA_MOTION_SEQUENCE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.DFAcitivityBase;
import com.liveness.dflivenesslibrary.R;
import com.liveness.dflivenesslibrary.fragment.model.DFLivenessOverlayModel;
import com.liveness.dflivenesslibrary.liveness.DFActionLivenessActivity;
import com.liveness.dflivenesslibrary.liveness.util.Constants;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;
import com.liveness.dflivenesslibrary.process.DFActionLivenessProcess;
import com.liveness.dflivenesslibrary.utils.DFBitmapUtils;
import com.liveness.dflivenesslibrary.utils.DFMediaPlayer;
import com.liveness.dflivenesslibrary.utils.DFViewShowUtils;
import com.liveness.dflivenesslibrary.view.DFAlertDialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;



public class DFActionLivenessFragment extends DFLivenessBaseFragment {
    private static final String TAG = DFActionLivenessFragment.class.getSimpleName();

    private DFAlertDialog mDialog;

    private boolean mIsStart = false;
    private DFMediaPlayer mMediaPlayer;

    @Override
    public void onResume() {
        super.onResume();
        initGuideAudio();
        mSensorManger.registerListener(mSensorEventListener);
        if (mIsStart) {
            LivenessUtils.logI(TAG, "onResume", "showDialog");
            showFailDialog();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseGuideAudio();
    }

    @Override
    protected void initialize() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            String motionString = bundle.getString(EXTRA_MOTION_SEQUENCE);
            if (motionString != null) {
                mDetectList = LivenessUtils.getDetectActionOrder(motionString);
            }
        }

        super.initialize();
        initGuideAudio();
        mProcess = new DFActionLivenessProcess(getActivity());
        mProcess.registerLivenessDetectCallback(mLivenessListener);
    }

    @Override
    protected void removeDetectWaitUI() {
        super.removeDetectWaitUI();
        mIsStart = true;
    }

    protected void updateUi(int animationId, int number) {
        LivenessUtils.logI(TAG, "mNoteTextView", "number", number);
        if (animationId != 0) {
            startAnimation(animationId);
        }
        if (number >= 0) {
            resetVGBottomDots();
            View childAt = mVGBottomDots.getChildAt(number);
            childAt.setEnabled(false);
        }
    }

    private void resetVGBottomDots() {
        if (mVGBottomDots != null) {
            int childCount = mVGBottomDots.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = mVGBottomDots.getChildAt(i);
                childAt.setEnabled(true);
            }
        }
    }

    protected void showFailDialog() {
        showDialog(R.string.livenesslibrary_failure_dialog_title_track_miss);
    }

    protected void showMoreThanFaceDialog() {
        showDialog(R.string.livenesslibrary_failure_dialog_title_more_than_face);
    }

    private boolean isDialogShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    private void restartAnimationAndLiveness() {
        setLivenessState(false);
//        mLivenessResultFileProcess.deleteLivenessFiles();
        if (mDetectList.length >= 1) {
            View childAt = mVGBottomDots.getChildAt(0);
            childAt.setEnabled(false);
        }
        startAnimation(CURRENT_ANIMATION);
    }

    protected void showDialog(int dialogId) {
        if (isDialogShowing()) {
            return;
        }
        if (mDetectList.length >= 1) {
            for (int i = 0; i < mDetectList.length; i++) {
                View childAt = mVGBottomDots.getChildAt(i);
                if (childAt != null) {
                    childAt.setEnabled(true);
                }
            }
        }

        hideTimeContoller();
        hideIndicateView();
        String dialogTitle = getStringWithID(dialogId);
        mDialog = new DFAlertDialog(getActivity()).builder().setCancelable(false).
                setTitle(dialogTitle).setNegativeButton(getStringWithID(R.string.cancel), new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        ((DFActionLivenessActivity) getActivity()).onErrorHappen(DFAcitivityBase.RESULT_BACK_PRESSED);
                    }
                }).setPositiveButton(getStringWithID(R.string.restart_preview), new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        showIndicateView();
                        mProcess.registerLivenessDetectCallback(mLivenessListener);
                        mProcess.initNv21Data();
                        restartAnimationAndLiveness();
                        mFaceProcessResult = null;
                    }
                });
        if (getActivity().isFinishing()) {
            return;
        }
        mDialog.show();
    }

    protected void initGuideAudio() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new DFMediaPlayer();
        }
    }

    protected void startGuideAudio(int audioResId) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setMediaSource(getActivity(), audioResId, false);
            mMediaPlayer.start();
        }
    }

    protected void stopGuideAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    protected void releaseGuideAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void stopGvView() {
        if (mGvView != null) {
            mGvView.setPaused(true);
        }
    }

    @Override
    protected void onLivenessDetectCallBack(final int value, final int status, final byte[] livenessEncryptResult,
                                            final DFLivenessSDK.DFLivenessImageResult[] imageResult, final byte[] videoResult) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value == DFLivenessSDK.DFLivenessMotion.BLINK.getValue()) {
                    updateUi(R.raw.raw_liveness_detect_blink, status);
                    startCountdown();
                } else if (value == DFLivenessSDK.DFLivenessMotion.MOUTH.getValue()) {
                    updateUi(R.raw.raw_liveness_detect_mouth, status);
                    startCountdown();
                } else if (value == DFLivenessSDK.DFLivenessMotion.NOD.getValue()) {
                    updateUi(R.raw.raw_liveness_detect_nod, status);
                    startCountdown();
                } else if (value == DFLivenessSDK.DFLivenessMotion.YAW.getValue()) {
                    updateUi(R.raw.raw_liveness_detect_yaw, status);
                    startCountdown();
                } else if (value == DFLivenessSDK.DFLivenessMotion.HOLD_STILL.getValue()) {
                    updateUi(R.raw.raw_liveness_detect_holdstill, status);
                } else if (value == Constants.LIVENESS_SUCCESS) {
                    mIsStart = false;
                    LivenessUtils.logI(TAG, "onLivenessDetectCallBack", "LIVENESS_SUCCESS");
                    stopPreview();
                    stopGvView();
                    stopGuideAudio();
                    stopCountDown();
                    if (imageResult != null) {
                        for (DFLivenessSDK.DFLivenessImageResult itemImageResult : imageResult) {
                            byte[] image = itemImageResult.image;
                            Bitmap cropBitmap;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            cropBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
                            Bitmap bmp = DFBitmapUtils.cropResultBitmap(cropBitmap, mCameraBase.getPreviewWidth(), mCameraBase.getPreviewHeight(), mCameraBase.getScanRatio());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            InputStream isBm = new ByteArrayInputStream(baos.toByteArray());
                        }
                    }
//                    mLivenessResultFileProcess.saveFinalEncrytFile(livenessEncryptResult, imageResult, videoResult);
//                    mLivenessResultFileProcess.saveFile(livenessEncryptResult);
                } else if (value == Constants.LIVENESS_TRACKING_MISSED) {
                    LivenessUtils.logI(TAG, "onLivenessDetect" + "track miss" + "DFMediaPlayer");
                    stopGuideAudio();
                    LivenessUtils.logI(TAG, "LIVENESS_TRACKING_MISSED", "showDialog");
                    showFailDialog();
                    mLivenessResultFileProcess.saveFile(livenessEncryptResult);
                } else if (value == Constants.LIVENESS_MORE_THAN_FACE) {
                    LivenessUtils.logI(TAG, "onLivenessDetect" + "LIVENESS_MORE_THAN_FACE");
                    stopGuideAudio();
                    LivenessUtils.logI(TAG, "LIVENESS_MORE_THAN_FACE", "showDialog");
                    showMoreThanFaceDialog();
                    mLivenessResultFileProcess.saveFile(livenessEncryptResult);
                } else if (value == Constants.LIVENESS_TIME_OUT) {
                    LivenessUtils.logI(TAG, "LIVENESS_TIME_OUT", "showDialog");
                    stopGuideAudio();
                    showFailDialog();
                    mLivenessResultFileProcess.saveFile(livenessEncryptResult);
                } else if (value == Constants.DETECT_BEGIN_WAIT) {
                    updateUi(R.raw.raw_liveness_detect_holdstill, 0);
                    showDetectWaitUI();
                } else if (value == Constants.DETECT_END_WAIT) {
                    removeDetectWaitUI();
                }
            }
        });
    }

    @Override
    protected void onFaceDetectCallback(int value, boolean hasFace, boolean faceValid, DFLivenessSDK.DFRect rect) {
//        if (value == DFLivenessSDK.DFLivenessMotion.HOLD_STILL.getValue()) {
        String action = String.valueOf(value);
        String hasFaceShow = DFViewShowUtils.booleanTrans(hasFace);
        String faceValidShow = DFViewShowUtils.booleanTrans(faceValid);
        String faceProcessResult = action.concat("_").concat(hasFaceShow).concat("_").concat(faceValidShow);
        if (!TextUtils.equals(mFaceProcessResult, faceProcessResult)) {
            mFaceProcessResult = faceProcessResult;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DFLivenessOverlayModel overlayModel = mFaceHintMap.get(mFaceProcessResult);
                    if (overlayModel != null) {
                        String hintID = overlayModel.getShowHint();
                        int borderColor = overlayModel.getBorderColor();
                        int audioGuideResId = overlayModel.getAudioGuideResId();
                        if (borderColor != -1) {
                            mOverlayView.showBorder();
                            mOverlayView.setBorderColor(borderColor);
                        }
                        if (audioGuideResId != 0) {
                            startGuideAudio(audioGuideResId);
                        }
                        refreshHintText(hintID);
                    }
                }
            });

        }
//        } else {
//            mOverlayView.hideBorder();
//        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mLivenessCallback != null) {
            mLivenessCallback.startDetect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    protected int isBottomDotsVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected void initFaceHintMap() {
        mFaceHintMap = new HashMap<>();

        mFaceHintMap.put("-1_0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, 0));

        mFaceHintMap.put("4_0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
        mFaceHintMap.put("4_0_1", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
        mFaceHintMap.put("4_1_0", new DFLivenessOverlayModel(mFaceNotValid, Color.RED, R.raw.audio_liveness_detect_move_away));
        mFaceHintMap.put("4_1_1", new DFLivenessOverlayModel(mHasFaceHint, Color.GREEN, R.raw.audio_liveness_detect_holdstill));

        mFaceHintMap.put("0_0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
        mFaceHintMap.put("0_0_1", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
        mFaceHintMap.put("0_1_0", new DFLivenessOverlayModel(mFaceNotValid, Color.RED, R.raw.audio_liveness_detect_move_away));
        mFaceHintMap.put("0_1_1", new DFLivenessOverlayModel(getStringWithID(R.string.note_blink), Color.GREEN, R.raw.audio_liveness_detect_blink));

//        mFaceHintMap.put("1_0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
//        mFaceHintMap.put("1_0_1", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
//        mFaceHintMap.put("1_1_0", new DFLivenessOverlayModel(mFaceNotValid, Color.RED, R.raw.audio_liveness_detect_move_away));
//        mFaceHintMap.put("1_1_1", new DFLivenessOverlayModel(getStringWithID(R.string.note_mouth), Color.GREEN, R.raw.audio_liveness_detect_mouth));

//        mFaceHintMap.put("2_0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
//        mFaceHintMap.put("2_0_1", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
//        mFaceHintMap.put("2_1_0", new DFLivenessOverlayModel(mFaceNotValid, Color.RED, R.raw.audio_liveness_detect_move_away));
//        mFaceHintMap.put("2_1_1", new DFLivenessOverlayModel(getStringWithID(R.string.note_yaw), Color.GREEN, R.raw.audio_liveness_detect_yaw));

//        mFaceHintMap.put("3_0_0", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
//        mFaceHintMap.put("3_0_1", new DFLivenessOverlayModel(mNoFaceHint, Color.RED, R.raw.audio_liveness_detect_no_face));
//        mFaceHintMap.put("3_1_0", new DFLivenessOverlayModel(mFaceNotValid, Color.RED, R.raw.audio_liveness_detect_move_away));
//        mFaceHintMap.put("3_1_1", new DFLivenessOverlayModel(getStringWithID(R.string.note_nod), Color.GREEN, R.raw.audio_liveness_detect_nod));
    }
}