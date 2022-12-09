package com.liveness.dflivenesslibrary.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.liveness.dflivenesslibrary.R;

public class DFGifView extends View {

    /**
     * The default is 1 second
     */
    private static final int DEFAULT_MOVIE_DURATION = 1000;

    private int mMovieResourceId;

    private Movie mMovie;

    private long mMovieStart;

    private int mCurrentAnimationTime = 0;

    private float mLeft;

    private float mTop;

    private float mScale;

    private int mMeasuredMovieWidth;

    private int mMeasuredMovieHeight;

    private boolean mVisible = true;

    private volatile boolean mPaused = false;

    private Path mClipPath;

    public DFGifView(Context context) {
        this(context, null);
    }

    public DFGifView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CustomTheme_gifViewStyle);
    }

    public DFGifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setViewAttributes(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    private void setViewAttributes(Context context, AttributeSet attrs,
                                   int defStyle) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Reading the GIF value from the description file creates the Movie instance
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.DFGifView, defStyle, R.style.Widget_GifView);
        mMovieResourceId = array.getResourceId(R.styleable.DFGifView_gif, -1);
        mPaused = array.getBoolean(R.styleable.DFGifView_paused, false);
        array.recycle();
        if (mMovieResourceId != -1) {
            mMovie = Movie.decodeStream(getResources().openRawResource(
                    mMovieResourceId));
        }
    }

    /**
     * Set up the GIF resource
     *
     * @param movieResId movie resource id
     */
    public void setMovieResource(int movieResId) {
        this.mMovieResourceId = movieResId;
        mMovie = Movie.decodeStream(getResources().openRawResource(
                mMovieResourceId));
        requestLayout();
    }

    public void setMovie(Movie movie) {
        this.mMovie = movie;
        requestLayout();
    }

    public Movie getMovie() {
        return mMovie;
    }

    public void setMovieTime(int time) {
        mCurrentAnimationTime = time;
        invalidate();
    }

    /**
     * set pause
     *
     * @param paused True stands for pause
     */
    public void setPaused(boolean paused) {
        this.mPaused = paused;
        if (!paused) {
            mMovieStart = android.os.SystemClock.uptimeMillis()
                    - mCurrentAnimationTime;
        }
        invalidate();
    }

    /**
     * Whether the GIF stopped or not
     *
     * @return is pause
     */
    public boolean isPaused() {
        return this.mPaused;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMovie != null) {
            int movieWidth = mMovie.width();
            int movieHeight = mMovie.height();
            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
            float scaleW = (float) movieWidth / (float) maximumWidth;
            mScale = 1f / scaleW;
            mMeasuredMovieWidth = maximumWidth;
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(),
                    getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
        mVisible = getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null) {

            canvas.save();
            int width = getWidth();
            int height = getHeight();

            if (mClipPath == null) {
                mClipPath = new Path();
            } else {
                mClipPath.reset();
            }
            mClipPath.addCircle(width / 2f, height / 2f, width / 2f, Path.Direction.CW);
            canvas.clipPath(mClipPath);

            if (!mPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }

            canvas.restore();
        }
    }

    @SuppressLint("NewApi")
    private void invalidateView() {
        if (mVisible) {
            postInvalidateOnAnimation();
        }
    }

    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        // If the first frame, record the start time
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        // Take the length of the animation
        int dur = mMovie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIE_DURATION;
        }
        // Figure out which frame to display
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    @SuppressLint("WrongConstant")
    private void drawMovieFrame(Canvas canvas) {
        // Set the frame to display and draw
        mMovie.setTime(mCurrentAnimationTime);
        canvas.save();
        canvas.scale(mScale, mScale);
        mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
        canvas.restore();
    }

    @SuppressLint("NewApi")
    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }
}
