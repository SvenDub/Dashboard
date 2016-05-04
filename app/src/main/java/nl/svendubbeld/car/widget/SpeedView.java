package nl.svendubbeld.car.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.unit.speed.KilometerPerHour;
import nl.svendubbeld.car.unit.speed.MeterPerSecond;
import nl.svendubbeld.car.unit.speed.MilesPerHour;
import nl.svendubbeld.car.unit.speed.Speed;

public class SpeedView extends FrameLayout implements LocationListener {

    public static final int UNIT_MS = 0;
    public static final int UNIT_KMH = 1;
    public static final int UNIT_MPH = 2;

    private boolean mShowLabel = true;
    private int mUnit = 0;

    private TextView mSpeedView;
    private TextView mLabelView;

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SpeedView,
                0, 0
        );

        try {
            mShowLabel = a.getBoolean(R.styleable.SpeedView_showLabel, true);
            mUnit = a.getInteger(R.styleable.SpeedView_unit, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_speed, this);

        mSpeedView = (TextView) findViewById(R.id.speed_value);
        mLabelView = (TextView) findViewById(R.id.speed_label);
    }

    public boolean isShowLabel() {
        return mShowLabel;
    }

    public void setShowLabel(boolean showLabel) {
        mShowLabel = showLabel;
        invalidate();
        requestLayout();
    }

    public int getUnit() {
        return mUnit;
    }

    public void setUnit(int unit) {
        if (unit >= 3 || unit < 0) {
            throw new IllegalArgumentException("\"unit\" has to be one of UNIT_MS, UNIT_KMH or UNIT_MPH");
        }

        mUnit = unit;
        invalidate();
        requestLayout();
    }

    public void setSpeed(Speed speed) {
        Speed newSpeed;

        switch (mUnit) {
            default:
            case UNIT_MS:
                newSpeed = new MeterPerSecond(speed);
                break;
            case UNIT_KMH:
                newSpeed = new KilometerPerHour(speed);
                break;
            case UNIT_MPH:
                newSpeed = new MilesPerHour(speed);
                break;
        }

        mSpeedView.setText(newSpeed.getValueString(0));
        mLabelView.setText(newSpeed.getUnit());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mLabelView.setVisibility(mShowLabel ? VISIBLE : GONE);

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onLocationChanged(Location location) {
        Speed speed = new MeterPerSecond(location.getSpeed());

        setSpeed(speed);
    }
}
