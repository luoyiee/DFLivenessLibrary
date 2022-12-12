package com.liveness.dflivenesslibrary.liveness;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.DFAcitivityBase;
import com.liveness.dflivenesslibrary.DFProductResult;
import com.liveness.dflivenesslibrary.DFTransferResultInterface;
import com.liveness.dflivenesslibrary.callback.DFLivenessResultCallback;
import com.liveness.dflivenesslibrary.fragment.DFProductFragmentBase;
import com.liveness.dflivenesslibrary.liveness.presenter.DFAntiHackProcessPresenter;
import com.liveness.dflivenesslibrary.liveness.presenter.DFCommonResultProcessPresenter;
import com.liveness.dflivenesslibrary.liveness.presenter.DFResultProcessBasePresenter;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;
import com.liveness.dflivenesslibrary.view.DFLivenessLoadingDialogFragment;

import java.io.File;


public class DFLivenessBaseActivity extends DFAcitivityBase implements DFLivenessResultCallback, DFResultProcessBasePresenter.DFResultProcessCallback {
    private static final String TAG = "LivenessActivity";
    /**
     * Error loading library file
     */
    public static final int RESULT_CREATE_HANDLE_ERROR = 1001;
    /**
     * The file path where the result is saved is passed in
     */
    public static final String EXTRA_RESULT_PATH = "com.dfsdk.liveness.resultPath";
    /**
     * Sets whether to return picture results or not
     */
    public static final String KEY_DETECT_IMAGE_RESULT = "key_detect_image_result";
    public static final String KEY_HINT_MESSAGE_HAS_FACE = "com.dfsdk.liveness.message.hasface";
    public static final String KEY_HINT_MESSAGE_NO_FACE = "com.dfsdk.liveness.message.noface";
    public static final String KEY_HINT_MESSAGE_FACE_NOT_VALID = "com.dfsdk.liveness.message.facenotvalid";
    public static final String KEY_ANTI_HACK = "key_anti_hack";
    public static final String LIVENESS_FILE_NAME = "livenessResult";
    private DFLivenessLoadingDialogFragment mProgressDialog = null;
    private DFResultProcessBasePresenter mResultProcessPresenter;
    private String mExtraResultPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraResultPath();
        initPresenter();
    }

    private void initExtraResultPath() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mExtraResultPath = bundle.getString(EXTRA_RESULT_PATH);
        }
        initPresenter();
        if (mExtraResultPath == null) {
            mExtraResultPath = Environment
                    .getExternalStorageDirectory().getAbsolutePath()
                    + File.separator
                    + "liveness" + File.separator;
        }
        File livenessFolder = new File(mExtraResultPath);
        if (!livenessFolder.exists()) {
            boolean mkdirs = livenessFolder.mkdirs();
        }
    }

    private void initPresenter() {
        Intent intent = getIntent();
        boolean antiHackModel = intent.getBooleanExtra(KEY_ANTI_HACK, false);
        boolean isReturnImage = intent.getBooleanExtra(KEY_DETECT_IMAGE_RESULT, false);
        if (antiHackModel) {
            mResultProcessPresenter = new DFAntiHackProcessPresenter(isReturnImage, this);
        } else {
            mResultProcessPresenter = new DFCommonResultProcessPresenter(isReturnImage, this);
        }
        mResultProcessPresenter.checkResultDealParameter();
    }

    @Override
    protected DFProductFragmentBase getFragment() {
        return null;
    }

    @Override
    public void saveFinalEncrytFile(byte[] livenessEncryptResult, DFLivenessSDK.DFLivenessImageResult[] imageResult, byte[] videoResult) {
        if (imageResult != null) {
            for (DFLivenessSDK.DFLivenessImageResult itemImageResult : imageResult) {
                mResultProcessPresenter.dealLivenessResult(livenessEncryptResult, itemImageResult.image, videoResult);
            }
        }
    }

    @Override
    public void showProgressDialog() {
        initProgressDialog();
        mProgressDialog.showDialog(getFragmentManager());
    }

    @Override
    public void hideProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isAdded()) {
                    mProgressDialog.dismissAllowingStateLoss();
                }
            }
        });

    }

    private void initProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = DFLivenessLoadingDialogFragment.getInstance();
        }
    }

    @Override
    public void deleteLivenessFiles() {
        LivenessUtils.deleteFiles(mExtraResultPath);
    }

    @Override
    public void saveFile(byte[] livenessEncryptResult) {
        LivenessUtils.saveFile(livenessEncryptResult, mExtraResultPath, LIVENESS_FILE_NAME);
    }

    @Override
    public void returnDFProductResult(DFProductResult productResult) {
        Intent intent = new Intent();
        ((DFTransferResultInterface) getApplication()).setResult(productResult);
        setResult(RESULT_OK, intent);
        finish();
    }
}
