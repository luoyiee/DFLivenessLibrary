package com.liveness.dflivenesslibrary.callback;


import com.dfsdk.liveness.DFLivenessSDK;

public interface DFLivenessResultCallback {
    void saveFinalEncrytFile(byte[] livenessEncryptResult, byte[] imageResult, byte[] videoResult);

    void saveFile(byte[] livenessEncryptResult);

    void deleteLivenessFiles();
}
