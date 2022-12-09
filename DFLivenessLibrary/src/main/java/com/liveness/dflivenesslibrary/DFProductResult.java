package com.liveness.dflivenesslibrary;


import com.dfsdk.liveness.DFLivenessSDK;

import java.util.ArrayList;

public class DFProductResult {
    private ArrayList<byte[]> mResultImages; // jpeg images
    private byte[] mLivenessEncryptResult;
    private DFLivenessSDK.DFLivenessImageResult[] mLivenessImageResults;
    private byte[] mLivenessVideoResult;
    private boolean mAntiHackPass;
    private String mErrorMessage;

    public DFProductResult() {

    }


    public ArrayList<byte[]> getResultImages() {
        return mResultImages;
    }

    public void setResultImages(ArrayList<byte[]> resultImages) {
        this.mResultImages = resultImages;
    }

    public byte[] getLivenessEncryptResult() {
        return mLivenessEncryptResult;
    }

    public void setLivenessEncryptResult(byte[] livenessResult) {
        this.mLivenessEncryptResult = livenessResult;
    }

    public DFLivenessSDK.DFLivenessImageResult[] getLivenessImageResults() {
        return mLivenessImageResults;
    }

    public void setLivenessImageResults(DFLivenessSDK.DFLivenessImageResult[] imageResults) {
        this.mLivenessImageResults = imageResults;
    }

    public byte[] getLivenessVideoResult() {
        return mLivenessVideoResult;
    }

    public void setLivenessVideoResult(byte[] livenessVideoResult) {
        this.mLivenessVideoResult = livenessVideoResult;
    }

    public boolean isAntiHackPass() {
        return mAntiHackPass;
    }

    public void setAntiHackPass(boolean antiHackPass) {
        this.mAntiHackPass = antiHackPass;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.mErrorMessage = errorMessage;
    }
}
