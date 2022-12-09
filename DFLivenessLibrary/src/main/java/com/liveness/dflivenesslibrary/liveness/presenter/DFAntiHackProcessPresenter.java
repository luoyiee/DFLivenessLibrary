package com.liveness.dflivenesslibrary.liveness.presenter;

import android.text.TextUtils;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.DFProductResult;
import com.liveness.dflivenesslibrary.net.DFNetworkUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DFAntiHackProcessPresenter extends DFResultProcessBasePresenter {

    protected DFProductResult mProductResult;

    protected ExecutorService mThreadPool;

    private Future<DFNetworkUtil.DFNetResult> mLivenessFuture;

    public DFAntiHackProcessPresenter(boolean returnImage, DFResultProcessCallback resultProcessCallback) {
        super(returnImage, resultProcessCallback);
    }

    private boolean checkStringLength(String value, int checkLength) {
        boolean checkResult = false;
        if (!TextUtils.isEmpty(value) && value.length() == checkLength) {
            checkResult = true;
        }
        return checkResult;
    }

    @Override
    public void checkResultDealParameter() {
        super.checkResultDealParameter();
        if (!checkStringLength(DFNetworkUtil.API_ID, 32)
                || !checkStringLength(DFNetworkUtil.API_SECRET, 32)) {
            throw new IllegalArgumentException("Invalid API_ID or API_SECRET");
        }
    }

    @Override
    public void dealLivenessResult(byte[] livenessEncryptResult, DFLivenessSDK.DFLivenessImageResult[] imageResult, byte[] videoResult) {
        showProgressDialog();
        mProductResult = getDetectResult(livenessEncryptResult, imageResult, videoResult);
        initThreadPool();
        antiHack();
        getNetworkResult();
    }

    private void initThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = Executors.newFixedThreadPool(3);
        }
    }

    private void antiHack() {
        mLivenessFuture = mThreadPool.submit(new Callable<DFNetworkUtil.DFNetResult>() {
            @Override
            public DFNetworkUtil.DFNetResult call() throws Exception {
                return networkProcess();
            }
        });
    }

    public DFNetworkUtil.DFNetResult networkProcess() {
        return DFNetworkUtil.doAntiHack(mProductResult.getLivenessEncryptResult());
    }

    protected void getNetworkResult() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final DFNetworkUtil.DFNetResult result = mLivenessFuture.get();
                    mProductResult.setAntiHackPass(result.mNetworkResultStatus);
                    int mNetworkErrorMsgID = result.mNetworkErrorMsgID;
                    hideProgressDialog();
                    returnDetectResult();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    hideProgressDialog();
                } catch (InterruptedException e) {
                    hideProgressDialog();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void returnDetectResult() {
        if (mResultProcessCallback != null){
            mResultProcessCallback.returnDFProductResult(mProductResult);
        }
    }
}
