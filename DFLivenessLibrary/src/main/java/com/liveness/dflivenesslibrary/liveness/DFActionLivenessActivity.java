package com.liveness.dflivenesslibrary.liveness;

import com.liveness.dflivenesslibrary.fragment.DFActionLivenessFragment;
import com.liveness.dflivenesslibrary.fragment.DFProductFragmentBase;

public class DFActionLivenessActivity extends DFLivenessBaseActivity {

    /**
     * The sequence of action motion
     */
    public static final String EXTRA_MOTION_SEQUENCE = "com.dfsdk.liveness.motionSequence";

    /**
     *  output type
     */
    public static final String OUTTYPE = "outType";

    /**
     * set complexity
     */
    public static final String COMPLEXITY = "complexity";

    @Override
    protected DFProductFragmentBase getFragment() {
        return new DFActionLivenessFragment();
    }
}
