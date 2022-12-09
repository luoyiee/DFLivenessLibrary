package com.liveness.dflivenesslibrary.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.liveness.dflivenesslibrary.R;
import com.liveness.dflivenesslibrary.camera.CameraBase;
import com.liveness.dflivenesslibrary.view.DFLivenessOverlayView;

/**
 * @author 学客
 */
public abstract class DFProductFragmentBase extends Fragment {

    protected SurfaceView mSurfaceView;
    protected DFLivenessOverlayView mOverlayView;
    protected View mRootView;
    protected CameraBase mCameraBase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(getLayoutResourceId(), container,
                false);
        mSurfaceView = mRootView.findViewById(R.id.surfaceViewCamera);
        mOverlayView = mRootView.findViewById(R.id.id_ov_mask);
        initCamera();
        initialize();
        return mRootView;
    }

    protected void initialize() {
    }

    private void initCamera() {
        if (mSurfaceView != null) {
            mCameraBase = new CameraBase(getActivity(), mSurfaceView, mOverlayView, isFrontCamera());
        }
    }

    protected void showToast(int showHintResId) {
        String showHint = getActivity().getString(showHintResId);
        Toast.makeText(getActivity(), showHint, Toast.LENGTH_SHORT).show();
    }

    protected boolean isFrontCamera() {
        return true;
    }

    protected abstract int getLayoutResourceId();

    protected void finishActivity(){
        Activity activity = getActivity();
        if (activity != null){
            activity.finish();
        }
    }

    public void stopPreview() {
        if (mCameraBase != null) {
            mCameraBase.stopPreview();
        }
    }

    public Rect getScanRect() {
        if (mOverlayView != null) {
            return mOverlayView.getScanRect();
        }
        return null;
    }

    public RectF getScanRatio() {
        if (mOverlayView != null) {
            return mOverlayView.getScanRectRatio();
        }
        return null;
    }

    public int getOverlapWidth() {
        if (mOverlayView != null) {
            return mOverlayView.getWidth();
        }
        return -1;
    }

    public int getOverlapHeight() {
        if (mOverlayView != null) {
            return mOverlayView.getHeight();
        }

        return -1;
    }

    public RectF getSilentDetectionRegion(float marginScale) {
        RectF region = new RectF();

        float scale = (float) mCameraBase.getPreviewWidth() / getOverlapHeight();

        Rect rect = getScanRect();
        region.left = (getOverlapHeight() - rect.bottom + 0.f) * scale;
        region.top = (getOverlapWidth() - rect.right + 0.f) * scale;
        region.right = region.left + rect.width() * scale;
        region.bottom = region.top + rect.height() * scale;

        int margin = (int) (rect.width() * marginScale);
        region.left = region.left - margin;
        region.top = region.top - margin;
        region.right = region.right + margin;
        region.bottom = region.bottom + margin;
        return region;
    }

    protected int dp2px(float dpValue) {
        double densityDpi = this.getResources().getDisplayMetrics().densityDpi;
        return (int) (dpValue * (densityDpi / 160.0));
    }
}
