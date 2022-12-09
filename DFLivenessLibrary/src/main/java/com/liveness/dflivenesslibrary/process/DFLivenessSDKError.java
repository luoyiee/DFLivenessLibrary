package com.liveness.dflivenesslibrary.process;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.R;

public enum DFLivenessSDKError {
    LIVENSE_EXPIRED(DFLivenessSDK.DF_LIVENESS_INIT_FAIL_OUT_OF_DATE, R.string.livenesslibrary_license_expired),
    LIVENSE_BUNDLE_APPLICATION_ID_ERROR(DFLivenessSDK.DF_LIVENESS_INIT_FAIL_BIND_APPLICATION_ID, R.string.livenesslibrary_livense_bundle_application_id_error);
    private int errorCode;
    private int errorHintResId;

    DFLivenessSDKError(int errorCode, int errorHintResId) {
        this.errorCode = errorCode;
        this.errorHintResId = errorHintResId;
    }

    public static DFLivenessSDKError getLivenessSDKError(int errorCode) {
        for (DFLivenessSDKError livenessSDKError : DFLivenessSDKError.values()) {
            if (errorCode == livenessSDKError.errorCode) {
                return livenessSDKError;
            }
        }
        return null;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getErrorHintResId() {
        return errorHintResId;
    }
}
