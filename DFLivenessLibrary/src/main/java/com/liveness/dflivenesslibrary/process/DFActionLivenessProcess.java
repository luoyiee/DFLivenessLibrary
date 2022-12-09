package com.liveness.dflivenesslibrary.process;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.dfsdk.liveness.DFLivenessSDK;
import com.liveness.dflivenesslibrary.liveness.DFActionLivenessActivity;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;

public class DFActionLivenessProcess extends DFLivenessBaseProcess {

    public DFActionLivenessProcess(Activity context) {
        super(context);
    }

    protected DFLivenessSDK.DFLivenessOutputType getOutputType(Bundle bundle) {
        String output = bundle.getString(DFActionLivenessActivity.OUTTYPE);
        return DFLivenessSDK.DFLivenessOutputType.getOutputTypeByValue(output);
    }

    protected DFLivenessSDK.DFLivenessComplexity getComplexity(Bundle bundle) {
        if (bundle != null) {
            String complexity = bundle.getString(DFActionLivenessActivity.COMPLEXITY);
            return DFLivenessSDK.DFLivenessComplexity.getComplexityByValue(complexity);
        }
        return DFLivenessSDK.DFLivenessComplexity.WRAPPER_COMPLEXITY_EASY;
    }

    @Override
    protected int getLivenessConfig(Intent intent) {
        Bundle bundle = intent.getExtras();
        DFLivenessSDK.DFLivenessOutputType outputType = getOutputType(bundle);

        DFLivenessSDK.DFLivenessComplexity complexity = getComplexity(bundle);

        return outputType.getValue() | complexity.getValue();
    }

    protected boolean isHoldStill(DFLivenessSDK.DFLivenessMotion motion) {
        return motion == DFLivenessSDK.DFLivenessMotion.HOLD_STILL;
    }

    protected DFLivenessSDK.DFLivenessMotion[] getMotionList() {
        return LivenessUtils.getMctionOrder(mIntent
                .getStringExtra(DFActionLivenessActivity.EXTRA_MOTION_SEQUENCE));
    }
}
