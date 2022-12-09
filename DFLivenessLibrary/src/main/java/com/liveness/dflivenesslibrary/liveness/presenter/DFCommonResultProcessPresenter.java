package com.liveness.dflivenesslibrary.liveness.presenter;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.DFProductResult;

public class DFCommonResultProcessPresenter extends DFResultProcessBasePresenter {

    public DFCommonResultProcessPresenter(boolean returnImage, DFResultProcessCallback resultProcessCallback) {
        super(returnImage, resultProcessCallback);
    }

    @Override
    public void dealLivenessResult(byte[] livenessEncryptResult, DFLivenessSDK.DFLivenessImageResult[] imageResult, byte[] videoResult) {
        DFProductResult detectResult = getDetectResult(livenessEncryptResult, imageResult, videoResult);
        if (mResultProcessCallback != null) {
            mResultProcessCallback.returnDFProductResult(detectResult);
        }
    }
}
