package com.liveness.dflivenesslibrary;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;

public class DFLivenessSDKManager {
    private static final String TAG = DFLivenessSDKManager.class.getSimpleName();
    private Context mContext;
    private static final String SDE_MODEL_NAME = "sde.dfmodel";
    private static final String SLD_MODEL_NAME = "sld.dfmodel";
    private static final String LICENSE_NAME = "DFLicense";
    private static final String LICENSE_NAME_OLD = "DeepFinch.lic";
    private static final String SDK_VERSION = "2.1.8";
    public static final int DF_LIVENESS_INIT_SUCCESS = 0;
    public static final int DF_LIVENESS_INIT_FAIL_BIND_APPLICATION_ID = -10;
    public static final int DF_LIVENESS_INIT_FAIL_OUT_OF_DATE = -6;
    public static final int DF_LIVENESS_LICENSE_TEST = -11;
    public com.dfsdk.liveness.DFLivenessSDK.DFLivenessResult mLivenessResult = null;
    public static final int PIX_FMT_NV21 = 3;
    public static final int PIX_FMT_BGR888 = 5;

    public DFLivenessSDKManager(Context context) {
        this.mContext = context;
        synchronized(this.getClass()) {
            this.copyModelIfNeed("sde.dfmodel");
            this.copyModelIfNeed("sld.dfmodel");
        }
    }

    private String getAssetResource(String licenseName) {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            isr = new InputStreamReader(this.mContext.getResources().getAssets().open(licenseName));
            br = new BufferedReader(isr);
            String line = null;

            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException var18) {
            var18.printStackTrace();
            Log.e(TAG, "getAssetResource===" + var18.toString());
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException var17) {
                    var17.printStackTrace();
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException var16) {
                    var16.printStackTrace();
                }
            }

        }

        return sb.toString();
    }


    private void copyModelIfNeed(String modelName) {
        String path = this.getModelPath(modelName);
        if (path != null) {
            File modelFile = new File(path);
            if (modelFile.exists()) {
                modelFile.delete();
            }

            try {
                InputStream in = this.mContext.getApplicationContext().getAssets().open(modelName);
                OutputStream out = new FileOutputStream(modelFile);
                byte[] buffer = new byte[4096];

                int n;
                while((n = in.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }

                in.close();
                out.close();
            } catch (IOException var8) {
                modelFile.delete();
                Log.e(TAG, var8.getLocalizedMessage(), var8);
            }
        }

    }

    @SuppressLint({"NewApi"})
    protected String getModelPath(String modelName) {
        String path = null;
        if (this.mContext == null) {
            return null;
        } else {
            File dataDir = this.mContext.getApplicationContext().getExternalFilesDir((String)null);
            boolean isExternal = true;
            if (dataDir != null) {
                path = dataDir.getAbsolutePath() + File.separator + modelName;

                try {
                    File tempFile = File.createTempFile("temp_", (String)null, dataDir);
                    tempFile.delete();
                } catch (IOException var6) {
                    var6.printStackTrace();
                    Log.e(TAG, "error msg: " + var6.getMessage());
                    isExternal = false;
                }
            }

            if (!isExternal) {
                dataDir = this.mContext.getApplicationContext().getFilesDir();
                path = dataDir.getAbsolutePath() + File.separator + modelName;
            }

            return path;
        }
    }

    public static String getSDKVersion() {
        return "2.1.8";
    }

    public class DFLivenessResult {
        public float left = 0.0F;
        public float top = 0.0F;
        public float right = 0.0F;
        public float bottom = 0.0F;
        public int faceRectStatus;
        public float score = 0.0F;
        public float blurScore;
        public float[] points_array = new float[42];
        public int points_count = 0;
        public int yaw = 0;
        public int pitch = 0;
        public int roll = 0;
        public int eye_dist = 0;
        public int ID = 0;
        public boolean passed = false;
        public int message = 0;
        public int trackStatus = 0;
        public boolean hasFace;

        public DFLivenessResult() {
        }
    }

    public static enum DFFaceStatus {
        DF_FACE_SUCCESS(0),
        DF_FACE_LARGE(1),
        DF_FACE_BLUR(2);

        private int statusResult;

        private DFFaceStatus(int statusResult) {
            this.statusResult = statusResult;
        }

        public int getStatusResult() {
            return this.statusResult;
        }

        public void setStatusResult(int statusResult) {
            this.statusResult = statusResult;
        }
    }

    public static enum DFLivenessKey {
        KEY_BLINK_KEY(0),
        KEY_MOUTH_KEY(1),
        KEY_YAW_KEY(2),
        KEY_PITCH_KEY(3),
        KEY_HOLD_STILL(4),
        KEY_TRACK_MISSING(5),
        KEY_PLACEHOLDER(100),
        KEY_HOLD_STILL_FRAME(101),
        KEY_HOLD_STILL_POS(102),
        KEY_HOLD_STILL_DETECT_NUMBER(103),
        KEY_HOLD_STILL_TIME_INTERVAL(104),
        KEY_HOLD_STILL_FACE_RET_MAX_RATE(105),
        KEY_HOLD_STILL_FACE_OFFSET_RATE(106),
        KEY_BLUR_THRESHOLD(107);

        private int mKeyValue;

        private DFLivenessKey(int key) {
            this.mKeyValue = key;
        }

        public int getKeyValue() {
            return this.mKeyValue;
        }
    }

    public class DFLivenessImageResult implements Serializable {
        public byte[] image = null;
        public byte[] detectImage = null;
        public int length = 0;
        public int motion = 0;

        public DFLivenessImageResult() {
        }
    }

    public class DFRect {
        public float left = 0.0F;
        public float top = 0.0F;
        public float right = 0.0F;
        public float bottom = 0.0F;

        public DFRect() {
        }
    }

    public static enum DFTrackStatus {
        PASSED(0),
        FACE_OUTOF_BOUND(-10),
        FACE_TOO_FAR(-11),
        FACE_TOO_CLOSE(-12);

        private int mValue;

        private DFTrackStatus(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public static enum DFDetectStatus {
        PASSED(0),
        DETECTING(-1),
        INTERNAL_ERROR(-2),
        TRACKING_MISSED(-8),
        MORE_THAN_FACE(-13),
        FRAME_SKIP(-14);

        private int mValue;

        private DFDetectStatus(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public static class DFStatus {
        private int detectStatus;
        private boolean passed;
        private boolean hasFace;
        private boolean faceValid;
        private com.dfsdk.liveness.DFLivenessSDK.DFRect faceRect;

        public DFStatus() {
        }

        public boolean isHasFace() {
            return this.hasFace;
        }

        public void setHasFace(boolean face) {
            this.hasFace = face;
        }

        public boolean isPassed() {
            return this.passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public int getDetectStatus() {
            return this.detectStatus;
        }

        public void setDetectStatus(int detectStatus) {
            this.detectStatus = detectStatus;
        }

        public boolean isFaceValid() {
            return this.faceValid;
        }

        public void setFaceValid(boolean faceValid) {
            this.faceValid = faceValid;
        }

        public void setFaceRect(com.dfsdk.liveness.DFLivenessSDK.DFRect rect) {
            this.faceRect = rect;
        }

        public com.dfsdk.liveness.DFLivenessSDK.DFRect getFaceRect() {
            return this.faceRect;
        }
    }

    public static enum DFDetectInfo {
        DETECTINFO((com.dfsdk.liveness.DFLivenessSDK.DFLivenessResult)null);

        private com.dfsdk.liveness.DFLivenessSDK.DFLivenessResult mValue;

        private DFDetectInfo(com.dfsdk.liveness.DFLivenessSDK.DFLivenessResult value) {
            this.mValue = value;
        }

        public com.dfsdk.liveness.DFLivenessSDK.DFLivenessResult getValue() {
            return this.mValue;
        }
    }

    public static enum DFWrapperSequentialInfo {
        ACCLERATION(0),
        ROTATION_RATE(1),
        GRAVITY(2),
        MAGNETIC_FIELD(3);

        private int mValue;

        private DFWrapperSequentialInfo(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public static enum DFLivenessOutputType {
        WRAPPER_OUTPUT_TYPE_SINGLE_IMAGE("singleImg", 0),
        WRAPPER_OUTPUT_TYPE_MULTI_IMAGE("multiImg", 1),
        WRAPPER_OUTPUT_TYPE_LOW_QUALITY_VIDEO("video", 2),
        WRAPPER_OUTPUT_TYPE_HIGH_QUALITY_VIDEO("fullVideo", 3);

        private final String mString;
        private int mValue = -1;

        private DFLivenessOutputType(String string, int value) {
            this.mValue = value;
            this.mString = string;
        }

        public int getValue() {
            return this.mValue;
        }

        public static com.dfsdk.liveness.DFLivenessSDK.DFLivenessOutputType getOutputTypeByValue(String value) {
            com.dfsdk.liveness.DFLivenessSDK.DFLivenessOutputType[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                com.dfsdk.liveness.DFLivenessSDK.DFLivenessOutputType outputType = var1[var3];
                if (outputType.mString.equalsIgnoreCase(value)) {
                    return outputType;
                }
            }

            return WRAPPER_OUTPUT_TYPE_MULTI_IMAGE;
        }
    }

    public static enum DFLivenessComplexity {
        WRAPPER_COMPLEXITY_EASY("easy", 0),
        WRAPPER_COMPLEXITY_NORMAL("normal", 256),
        WRAPPER_COMPLEXITY_HARD("hard", 512),
        WRAPPER_COMPLEXITY_HELL("hell", 768);

        private int mValue = -1;
        private String mString = null;

        private DFLivenessComplexity(String string, int value) {
            this.mValue = value;
            this.mString = string;
        }

        public int getValue() {
            return this.mValue;
        }

        public static com.dfsdk.liveness.DFLivenessSDK.DFLivenessComplexity getComplexityByValue(String value) {
            com.dfsdk.liveness.DFLivenessSDK.DFLivenessComplexity[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                com.dfsdk.liveness.DFLivenessSDK.DFLivenessComplexity complexity = var1[var3];
                if (complexity.mString.equalsIgnoreCase(value)) {
                    return complexity;
                }
            }

            return WRAPPER_COMPLEXITY_NORMAL;
        }
    }

    public static enum DFWrapperStaticInfo {
        DEVICE(0),
        OS(1),
        SDK_VERSION(2),
        SYS_VERSION(3),
        ROOT(4),
        IDFA(5),
        CONTROL_SEQ(6),
        CUSTOMER(7);

        private int mValue;

        private DFWrapperStaticInfo(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public static enum DFLivenessMotion {
        NONE(-1),
        BLINK(0),
        MOUTH(1),
        YAW(2),
        NOD(3),
        HOLD_STILL(4);

        private int mValue;

        private DFLivenessMotion(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }
}
