package com.liveness.dflivenesslibrary.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.liveness.dflivenesslibrary.DFAcitivityBase;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;
import com.liveness.dflivenesslibrary.view.DFLivenessOverlayView;

import java.util.List;

public class CameraBase {

    public static final boolean DEBUG = true;
    private static final boolean DEBUG_PREVIEW_SIZE = false;
    private static final String TAG = "CameraOverlapFragment";
    protected Camera mCamera = null;
    protected CameraInfo mCameraInfo = null;
    protected SurfaceView mSurfaceview = null;
    protected DFLivenessOverlayView mOverlap = null;
    protected SurfaceHolder mSurfaceHolder = null;
    private Matrix mMatrix;
    protected int mCameraFacing = CameraInfo.CAMERA_FACING_FRONT;
    private boolean mHasFrontCamera;
    protected Context mContext;

    private Camera.PreviewCallback mChildPreviewCallback;

    private byte[] mPreviewCallbackData;

    public CameraBase(Context context, SurfaceView view, View overlap, boolean isFront) {
        mContext = context;
        mSurfaceview = view;
        mOverlap = (DFLivenessOverlayView) overlap;
        mSurfaceHolder = mSurfaceview.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(mSurfaceViewCallBack);
        if (!isFront) {
            mCameraFacing = CameraInfo.CAMERA_FACING_BACK;
        }
        mMatrix = new Matrix();
//        mSurfaceview = (SurfaceView) view.findViewById(R.id.surfaceViewCamera);
//        mOverlap = (DFOverlayView) view.findViewById(R.id.id_ov_mask);
    }

    private void openCamera() {
        releaseCamera();
        mHasFrontCamera = false;
        openCameraFront();
        if (!mHasFrontCamera) {
            openCamera(true);
        }
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            initCameraParameters();
            initPreviewCallbackData();
            addPreviewCallbackBuffer();
        } catch (Exception ex) {
            releaseCamera();
            onOpenCameraError(mHasFrontCamera);
        }
    }

    private void initPreviewCallbackData() {
        if (mPreviewCallbackData == null) {
            mPreviewCallbackData = new byte[getPreviewWidth() * getPreviewHeight() * 3 / 2];
        }
    }

    public void addPreviewCallbackBuffer() {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(mPreviewCallbackData);
        }
    }

    private void openCameraFront() {
        openCamera(false);
    }

    private void openCamera(boolean any) {
        CameraInfo info = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            boolean front = info.facing == mCameraFacing;
            if (any || front) {
                if (front) {
                    mHasFrontCamera = true;
                }
                try {
                    mCamera = Camera.open(i);
                    mCameraInfo = info;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                    }
                    continue;
                }
                break;
            }
        }
    }

    public int getCameraOrientation() {
        return mCameraInfo.orientation;
    }

    public int getPreviewWidth() {
        return DFCameraPreviewSize.getInstance().getPreviewWidth();
    }

    public int getPreviewHeight() {
        return DFCameraPreviewSize.getInstance().getPreviewHeight();
    }

    private void onOpenCameraError(boolean hasFrontCamera) {
        onErrorHappen(DFAcitivityBase.RESULT_CAMERA_ERROR_NOPRERMISSION_OR_USED);
    }

    public void onErrorHappen(int errorCode) {
        if (mContext == null) {
            if (DEBUG) {
                Log.e(TAG, "onOpenCameraError getActivity() = null");
            }
            return;
        }
//        ((DFAcitivityBase) mContext).onErrorHappen(errorCode);
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mChildPreviewCallback = previewCallback;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    private SurfaceHolder.Callback mSurfaceViewCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int width, int height) {
            if (DEBUG) {
                Log.i(TAG, "SurfaceHolder.Callback?Surface Changed " + width
                        + "x" + height);
            }
            LivenessUtils.logI(TAG, "SurfaceHolder", "surfaceChanged");
            mMatrix.reset();
            mMatrix.setScale(width / (float) getPreviewHeight(), height / (float) getPreviewWidth());
            initCameraParameters();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LivenessUtils.logI(TAG, "SurfaceHolder", "surfaceCreated");
            mCamera = null;
            openCamera();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LivenessUtils.logI(TAG, "SurfaceHolder", "surfaceDestroyed");
            if (DEBUG) {
                Log.d(TAG, "SurfaceHolder.Callback surfaceDestroyed");
            }
            releaseCamera();
            if (mPreviewCallbackData != null) {
                mPreviewCallbackData = null;
            }
        }
    };

    protected void initCameraParameters() {
        if (mCamera == null) {
            if (DEBUG) {
                Log.e(TAG, "initCameraParameters mCamera == null");
            }
            return;
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);
            if (DEBUG_PREVIEW_SIZE) {
                debugPreviewSize();
            }

//            if (isSupportPreviewSize(parameters, 1280, 720)) {
//            DFCameraPreviewSize.getInstance().setPreviewWidth(1280);
//            DFCameraPreviewSize.getInstance().setPreviewHeight(720);
//            } else
            if (isSupportPreviewSize(parameters, 640, 480)) {
                DFCameraPreviewSize.getInstance().setPreviewWidth(640);
                DFCameraPreviewSize.getInstance().setPreviewHeight(480);
            } else {
                List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                if (supportedPreviewSizes.size() > 0) {
                    Size previewSize = supportedPreviewSizes.get(0);
                    DFCameraPreviewSize.getInstance().setPreviewWidth(previewSize.width);
                    DFCameraPreviewSize.getInstance().setPreviewHeight(previewSize.height);
                }
            }

            parameters.setPreviewSize(getPreviewWidth(), getPreviewHeight());
            if (DEBUG) {
                Log.d(TAG, "min:" + parameters.getMinExposureCompensation() + "max:" + parameters.getMaxExposureCompensation());
            }
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

            if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                parameters.set("rotation", 90);
                if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT && mCameraInfo.orientation == 90) {
                    mCamera.setDisplayOrientation(270);
                } else {
                    mCamera.setDisplayOrientation(90);
                }
                if (DEBUG) {
                    Log.d(TAG, "orientation: portrait");
                }
            } else {
                parameters.set("orientation", "landscape");
                mCamera.setDisplayOrientation(0);
                if (DEBUG) {
                    Log.d(TAG, "orientation: landscape");
                }
            }

            mCamera.setParameters(parameters);
            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
            mCamera.startPreview();

            if (DEBUG_PREVIEW_SIZE) {
                debugPreviewSizeAfterSetParameter();
            }
        } catch (Exception e) {
            LivenessUtils.logI(TAG, "initCameraParameters", e.toString());
            e.printStackTrace();
        }
    }

    public boolean isSupportPreviewSize(Camera.Parameters parameters, int previewWidth, int previewHeight) {
        boolean isSupport = false;
        if (parameters != null) {
            List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            if (supportedPreviewSizes != null) {
                int size = supportedPreviewSizes.size();
                for (int i = 0; i < size; i++) {
                    Camera.Size previewSize = supportedPreviewSizes.get(i);
                    if (previewSize != null) {
                        if (previewSize.width == previewWidth && previewSize.height == previewHeight) {
                            isSupport = true;
                        }
                    }
                }
            }
        }
        return isSupport;
    }

    public void releaseCamera() {
        if (null == mCamera) {
            return;
        }
        try {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        mCamera = null;
    }

    private void debugPreviewSizeAfterSetParameter() {
        if (mCamera == null || mCamera.getParameters() == null) {
            if (DEBUG) {
                Log.e(TAG, "debugPreviewSizeAfterSetParameter mCamera == null or getParameters = null");
            }
            return;
        }
        Size csize = mCamera.getParameters().getPreviewSize();
        if (csize == null) {
            if (DEBUG) {
                Log.e(TAG, "debugPreviewSizeAfterSetParameter csize == null");
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "initCamera after setting, previewSize:width: " + csize.width + " height: " + csize.height);
        }

    }

    public Rect getScanRect() {
        if (mOverlap != null) {
            return mOverlap.getScanRect();
        }
        return null;
    }

    public RectF getScanRatio() {
        if (mOverlap != null) {
            return mOverlap.getScanRectRatio();
        }
        return null;
    }

    public int getOverlapWidth() {
        if (mOverlap != null) {
            return mOverlap.getWidth();
        }
        return -1;
    }

    public int getOverlapHeight() {
        if (mOverlap != null) {
            return mOverlap.getHeight();
        }

        return -1;
    }

    private void debugPreviewSize() {
        if (mCamera == null) {
            if (DEBUG) {
                Log.e(TAG, "debugPreviewSize mCamera == null");
            }
            return;
        }
    }

    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mChildPreviewCallback != null) {
                mChildPreviewCallback.onPreviewFrame(data, camera);
            }
        }
    };

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }
}
