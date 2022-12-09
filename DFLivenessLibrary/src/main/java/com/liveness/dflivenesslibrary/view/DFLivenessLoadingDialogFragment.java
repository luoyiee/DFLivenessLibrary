package com.liveness.dflivenesslibrary.view;

import android.app.Dialog;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.liveness.dflivenesslibrary.R;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;


/**
 * @author 学客
 */
public class DFLivenessLoadingDialogFragment extends android.app.DialogFragment {
    private static final String TAG = "DFLivenessLoadingDialogFragment";

    public static DFLivenessLoadingDialogFragment getInstance() {
        return new DFLivenessLoadingDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LivenessUtils.logI(TAG, "onActivityCreated");
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }
        setTranslucentStatus();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.liveness_transparent)));
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        LivenessUtils.logI(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.liveness_fragment_loading, null);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.LivenessLoadingDialog);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        setCancelable(false);

        ImageView progressView = view.findViewById(R.id.id_iv_progress_spinner);
        RotateAnimation rotateAnimation = new RotateAnimation(0f, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(700);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        progressView.startAnimation(rotateAnimation);
    }

    private void setTranslucentStatus() {
        Dialog dialog = getDialog();
        //5.0
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    public void showDialog(FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (fragmentManager.isStateSaved()) {
                return;
            }
        }
        if (isAdded()) {
            fragmentManager.beginTransaction().remove(this).commit();
        }
        show(fragmentManager, this.getClass().getSimpleName());
    }

}