package nl.svendubbeld.car.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import nl.svendubbeld.car.R;

public class DialView extends View implements ValueAnimator.AnimatorUpdateListener {

    public static final float MODE_RATIO = 2f;
    private final Rect mTextBounds = new Rect();
    private boolean mShowText;
    private int mNeedleColor;
    private int mRingColor;
    private int mTextColor;
    private float mMax;
    private float mProgress;
    private ValueAnimator mProgressAnimator = ValueAnimator.ofFloat(0f, 1f);
    private List<Float> mLabels = new ArrayList<>();
    private List<Float> mHighlightedLabels = new ArrayList<>();
    private int mDecimals;
    private Drawable mIcon;
    private String mFormat;
    private Paint mTextPaint;
    private Paint mTextPaintHighlight;
    private Paint mTextPaintBig;
    private Paint mNeedlePaint;
    private Paint mRingPaint;
    private Paint mRingMarkerPaint;
    private RectF mBounds = new RectF(0, 0, 0, 0);
    private RectF mLabelBounds = new RectF(0, 0, 0, 0);
    private RectF mMarkerBounds = new RectF(0, 0, 0, 0);
    private Handler mHandler;

    public DialView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialView, 0, 0);

        try {
            mShowText = a.getBoolean(R.styleable.DialView_showValue, false);
            mNeedleColor = a.getColor(R.styleable.DialView_needleColor, getResources().getColor(R.color.accent));
            mRingColor = a.getColor(R.styleable.DialView_ringColor, getResources().getColor(R.color.gray));
            mTextColor = a.getColor(R.styleable.DialView_android_textColor, getResources().getColor(R.color.white));
            mMax = a.getFloat(R.styleable.DialView_max, 255f);
            mProgress = a.getFloat(R.styleable.DialView_progress, 0f);

            CharSequence[] highlightedLabels = a.getTextArray(R.styleable.DialView_labelsHighlighted);
            if (highlightedLabels != null) {
                mHighlightedLabels = createHighlightedLabels(highlightedLabels);
            }

            float labels = a.getFloat(R.styleable.DialView_labels, 0);
            mLabels = createLabels(labels);

            mDecimals = a.getInt(R.styleable.DialView_decimals, -1);
            setDecimals(mDecimals);

            mIcon = a.getDrawable(R.styleable.DialView_bottomIcon);
        } finally {
            a.recycle();
        }

        init();

        if (isInEditMode()) {
            setProgress(getMax() / 3);
            mProgressAnimator.setFloatValues(mProgress);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mProgressAnimator.setCurrentFraction(1f);
            }
        }
    }

    private void init() {
        mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNeedlePaint.setColor(mNeedleColor);
        mNeedlePaint.setStyle(Paint.Style.STROKE);
        mNeedlePaint.setStrokeWidth(10);

        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setColor(mRingColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(10);

        mRingMarkerPaint = new Paint(mRingPaint);
        mRingMarkerPaint.setStrokeWidth(5);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(35);

        mTextPaintHighlight = new Paint(mTextPaint);
        mTextPaintHighlight.setColor(mNeedleColor);

        mTextPaintBig = new Paint(mTextPaint);
        mTextPaintBig.setTextSize(150);

        mProgressAnimator.setDuration(300);
        mProgressAnimator.addUpdateListener(this);

        mHandler = new Handler();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        int widthWithoutPadding = widthSpecSize - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = heightSpecSize - getPaddingTop() - getPaddingBottom();

        if (widthSpecMode != MeasureSpec.EXACTLY && heightSpecMode != MeasureSpec.UNSPECIFIED) {
            height = heightWithoutPadding;
            //noinspection SuspiciousNameCombination
            width = height;
        } else if (widthSpecMode != MeasureSpec.UNSPECIFIED && heightSpecMode != MeasureSpec.EXACTLY) {
            width = widthWithoutPadding;
            //noinspection SuspiciousNameCombination
            height = width;
        } else if (widthSpecMode == MeasureSpec.UNSPECIFIED) {
            width = Math.max(widthWithoutPadding, heightWithoutPadding);
            //noinspection SuspiciousNameCombination
            height = width;
        } else {
            height = heightWithoutPadding;
            width = widthWithoutPadding;
        }

        setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), height + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        float xPad = (float) (getPaddingLeft() + getPaddingRight());
        float yPad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xPad;
        float hh = (float) h - yPad;

        mBounds = new RectF(0f, 0f, ww, hh);
        mBounds.offsetTo(getPaddingLeft(), getPaddingTop());

        mLabelBounds = new RectF(mBounds);
        mLabelBounds.inset(((float) Math.sqrt(ww)) * 4f, ((float) Math.sqrt(hh)) * 4f * (hh / ww));

        mMarkerBounds = new RectF(mBounds);
        mMarkerBounds.inset(35, 35 * (hh / ww));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBounds.width() > mBounds.height() * MODE_RATIO) {
            canvas.drawArc(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom * 2, 200f, 140f, false, mRingPaint);
        } else {
            canvas.drawArc(mBounds, 120f, 300f, false, mRingPaint);
        }

        float[] speed = getPointAtArc(((float) mProgressAnimator.getAnimatedValue()));
        canvas.drawLine(mBounds.centerX(), mBounds.centerY(), speed[0], speed[1], mNeedlePaint);

        for (float s : getLabels()) {
            float[] labelPos = getPointAtArc(s, mLabelBounds);
            float[] ringPos = getPointAtArc(s);
            float[] markerPos = getPointAtArc(s, mMarkerBounds);

            canvas.drawLine(markerPos[0], markerPos[1], ringPos[0], ringPos[1], mRingMarkerPaint);

            drawTextCentred(formatLabel(s), labelPos[0], labelPos[1], mTextPaint, canvas);
        }

        for (float s : getHighlightedLabels()) {
            float[] labelPos = getPointAtArc(s, mLabelBounds);
            float[] ringPos = getPointAtArc(s);
            float[] markerPos = getPointAtArc(s, mMarkerBounds);

            canvas.drawLine(markerPos[0], markerPos[1], ringPos[0], ringPos[1], mRingMarkerPaint);

            drawTextCentred(formatLabel(s), labelPos[0], labelPos[1], mTextPaintHighlight, canvas);
        }

        if (isShowText()) {
            if (mBounds.width() > mBounds.height() * MODE_RATIO) {
                drawTextCentred(formatLabel((float) mProgressAnimator.getAnimatedValue()), mBounds.centerX(), mBounds.centerY() + mTextBounds.height(), mTextPaintBig, canvas);
            } else {
                drawTextCentred(formatLabel((float) mProgressAnimator.getAnimatedValue()), mBounds.centerX(), mBounds.centerY(), mTextPaintBig, canvas);
            }
        }

        if (mIcon != null) {
            mIcon.setTint(getNeedleColor());
            int left = (int) getPointAtArc(0)[0];
            int right = (int) getPointAtArc(getMax())[0];
            float width = right - left;
            double height = (mIcon.getIntrinsicHeight() / (float) mIcon.getIntrinsicWidth()) * (0.6 * width);
            mIcon.setBounds((int) (left + 0.2 * width), (int) (mBounds.bottom - height), (int) (right - 0.2 * width), (int) mBounds.bottom);
            mIcon.draw(canvas);
        }
    }

    private float[] getPointAtArc(float value) {
        return getPointAtArc(value, mBounds);
    }

    private float[] getPointAtArc(float value, RectF bounds) {
        double theta;
        float x;
        float y;

        if (mBounds.width() > mBounds.height() * MODE_RATIO) {
            theta = Math.toRadians(Math.max(Math.min(value / getMax(), 1), 0) * 140f + 200f);
            x = (float) Math.cos(theta) * (bounds.centerX() - bounds.left) + bounds.centerX();
            y = (float) Math.sin(theta) * (bounds.centerY() * 2 - bounds.top) + bounds.centerY() * 2;
        } else {
            theta = Math.toRadians(Math.max(Math.min(value / getMax(), 1), 0) * 300f + 120f);
            x = (float) Math.cos(theta) * (bounds.centerX() - bounds.left) + bounds.centerX();
            y = (float) Math.sin(theta) * (bounds.centerY() - bounds.top) + bounds.centerY();
        }

        return new float[]{x, y};
    }

    private List<Float> createHighlightedLabels(CharSequence[] labels) {
        List<Float> labelsF = new ArrayList<>();

        for (CharSequence label : labels) {
            float labelF = Float.parseFloat(String.valueOf(label));
            if (labelF <= getMax()) {
                labelsF.add(labelF);
            }
        }

        return labelsF;
    }

    private List<Float> createLabels(float labels) {
        List<Float> labelsF = new ArrayList<>();

        if (labels > 0) {
            for (float i = 0; i <= getMax(); i += labels) {
                if (!mHighlightedLabels.contains(i)) {
                    labelsF.add(i);
                }
            }
        }

        return labelsF;
    }

    private String formatLabel(float label) {
        return new DecimalFormat(mFormat).format(label);
    }

    public void drawTextCentred(String text, float x, float y, Paint paint, Canvas canvas) {
        paint.getTextBounds(text, 0, text.length(), mTextBounds);
        canvas.drawText(text, x - mTextBounds.exactCenterX(), y - mTextBounds.exactCenterY(), paint);
    }

    public boolean isShowText() {
        return mShowText;
    }

    public void setShowText(boolean showText) {
        mShowText = showText;
        postInvalidate();
    }

    public int getNeedleColor() {
        return mNeedleColor;
    }

    public void setNeedleColor(int needleColor) {
        mNeedleColor = needleColor;
        postInvalidate();
    }

    public int getRingColor() {
        return mRingColor;
    }

    public void setRingColor(int ringColor) {
        mRingColor = ringColor;
        postInvalidate();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        postInvalidate();
    }

    public float getMax() {
        return mMax;
    }

    public void setMax(float max) {
        mMax = max;
        postInvalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(final float progress) {
        mProgress = progress;
        mHandler.post(() -> {
            mProgressAnimator.setFloatValues(((float) mProgressAnimator.getAnimatedValue()), progress);
            mProgressAnimator.setInterpolator(new LinearInterpolator());
            mProgressAnimator.setDuration(600);
            mProgressAnimator.start();
            invalidate();
        });
    }

    public List<Float> getLabels() {
        return mLabels;
    }

    public void setLabels(List<Float> labels) {
        mLabels = labels;
    }

    public List<Float> getHighlightedLabels() {
        return mHighlightedLabels;
    }

    public void setHighlightedLabels(ArrayList<Float> highlightedLabels) {
        mHighlightedLabels = highlightedLabels;
    }

    public int getDecimals() {
        return mDecimals;
    }

    public void setDecimals(int decimals) {
        mDecimals = decimals;

        mFormat = "0";
        if (mDecimals > 0) {
            mFormat += ".";
            for (int i = 0; i < mDecimals; i++) {
                mFormat += "0";
            }
        }
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }
}
