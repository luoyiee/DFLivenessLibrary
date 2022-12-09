package com.liveness.dflivenesslibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.liveness.dflivenesslibrary.R;
import com.liveness.dflivenesslibrary.camera.DFCameraPreviewSize;
import com.liveness.dflivenesslibrary.liveness.util.LivenessUtils;
import com.liveness.dflivenesslibrary.utils.DFViewShowUtils;


public class DFLivenessOverlayView extends RelativeLayout {
    private static final String TAG = DFLivenessOverlayView.class.getSimpleName();

    /**
     * the color of four corner
     */
    private int mBorderColor = Color.TRANSPARENT;
    /**
     * paint of border
     */
    private Paint mBorderPaint;
    protected Paint mXmodePaint;
    protected Paint mFaceRectPaint;
    /**
     * color of background
     */
    protected int mBackgroundColor = Color.WHITE;
    /**
     * region of background
     */
    protected Path mLockedBackgroundPath;
    /**
     * background's paint
     */
    protected Paint mLockedBackgroundPaint;

    protected RectF mFaceRect;

    /**
     * scanner region
     */
    protected Rect mScanRect;
    private boolean mIsBorderHidden = true;

    private int mCircleRadius;
    private int mCircleCenterX, mCircleCenterY;

    public DFLivenessOverlayView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setWillNotDraw(false);

        final TypedArray attrArray = getContext().obtainStyledAttributes(attributeSet,
                R.styleable.DFLivenessOverlayView);
        int defaultCircleTop = DFViewShowUtils.dp2px(getContext(), 200);
        int defaultCircleRadius = DFViewShowUtils.dp2px(getContext(), 150);
        int circleTop = (int) attrArray.getDimension(R.styleable.DFLivenessOverlayView_overlay_circle_top, defaultCircleTop);
//        int circleRadius = (int) attrArray.getDimension(R.styleable.DFLivenessOverlayView_overlay_circle_radius, defaultCircleRadius);
        attrArray.recycle();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int circleRadius = displayMetrics.widthPixels * 5 / 12;
        initialize(circleTop, circleRadius);

        initBackgroundPaint();
        initBorderPaint();
        initFaceRectPaint();

        mXmodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXmodePaint.setFilterBitmap(false);
        initAnimator();
    }

    private void initAnimator() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    protected void initialize(int circleTop, int circleRadius) {
        mCircleRadius = circleRadius;
        mCircleCenterY = mCircleRadius + circleTop;

    }

    private void initBackgroundPaint() {
        mLockedBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockedBackgroundPaint.clearShadowLayer();
        mLockedBackgroundPaint.setStyle(Paint.Style.FILL);
        mLockedBackgroundPaint.setColor(mBackgroundColor); // 75% black
    }

    private void initBorderPaint() {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.clearShadowLayer();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(14);
    }

    private void initFaceRectPaint() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mFaceRectPaint = new Paint();
        mFaceRectPaint.setAntiAlias(true);
        mFaceRectPaint.setDither(true);
        mFaceRectPaint.setColor(Color.GREEN);
        mFaceRectPaint.setStrokeWidth(stroke);
        mFaceRectPaint.setStyle(Paint.Style.STROKE);
    }

    public void setBorderColor(int color) {
        if (mIsBorderHidden) {
            return;
        }
        mBorderColor = color;
        if (mBorderPaint != null) {
            mBorderPaint.setColor(mBorderColor);
        }
        postInvalidate();
    }

    public void showBorder() {
        mIsBorderHidden = false;
        setBorderColor(Color.RED);
    }

    public void hideBorder() {
        mIsBorderHidden = true;
        mBorderColor = Color.TRANSPARENT;
        if (mBorderPaint != null) {
            mBorderPaint.setColor(mBorderColor);
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOverlay(canvas);
    }

    private void drawOverlay(Canvas canvas) {
        if (mScanRect == null) {
            return;
        }
        canvas.drawPath(mLockedBackgroundPath, mLockedBackgroundPaint);
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mBorderPaint);
        drawBorder(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (mFaceRect != null && mFaceRect.width() > 0) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            int previewWidth = DFCameraPreviewSize.getInstance().getPreviewWidth();
            int previewHeight = DFCameraPreviewSize.getInstance().getPreviewHeight();
            float scaleX = (float) viewWidth / (float) previewHeight; // 480
            float scaleY = (float) viewHeight / (float) previewWidth; // 640
            RectF rectF = new RectF(mFaceRect.left * scaleX, mFaceRect.top * scaleY, mFaceRect.right * scaleX, mFaceRect.bottom * scaleY);
            float left = rectF.left;
            float right = rectF.right;
            rectF.left = getWidth() - right;
            rectF.right = getWidth() - left;
            // Log.e(TAG, String.format("draw rect: %f, %f, w:%f, h:%f", rectF.left, rectF.top, rectF.right - rectF.left, rectF.bottom - rectF.top));
            canvas.drawRect(rectF, mFaceRectPaint);
        }
    }


    public Rect getScanRect() {
        return mScanRect;
    }

    public RectF getScanRectRatio() {
        RectF ratioRectF = new RectF();
        ratioRectF.left = (float) mScanRect.left / getWidth();
        ratioRectF.top = (float) mScanRect.top / getHeight();
        ratioRectF.right = (float) mScanRect.right / getWidth();
        ratioRectF.bottom = (float) mScanRect.bottom / getHeight();
        return ratioRectF;
    }


    protected void initialInfo() {
        mCircleCenterX = getWidth() / 2;
        mScanRect = new Rect(mCircleCenterX - mCircleRadius, mCircleCenterY - mCircleRadius, mCircleCenterX + mCircleRadius, mCircleCenterY + mCircleRadius);
        mLockedBackgroundPath = new Path();
        mLockedBackgroundPath.addRect(new RectF(getLeft(), getTop(), getRight(), getBottom()), Path.Direction.CCW);
        mLockedBackgroundPath.addCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, Path.Direction.CW);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        LivenessUtils.logI(TAG, "layout", l, t, r, b);
        initialInfo();
        LivenessUtils.logI(TAG, "layout", "end");
        invalidate();
    }

    protected void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}