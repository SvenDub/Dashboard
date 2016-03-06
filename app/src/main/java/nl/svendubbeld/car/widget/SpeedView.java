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
import nl.svendubbeld.car.unit.speed.Speed;

public class SpeedView extends FrameLayout implements LocationListener {

    private boolean mShowLabel = true;

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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mLabelView.setVisibility(mShowLabel ? VISIBLE : GONE);

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onLocationChanged(Location location) {
        Speed speed = new MeterPerSecond(location.getSpeed());

        Speed speedKmh = new KilometerPerHour(speed);

        mSpeedView.setText(speedKmh.getValueString(0));
    }
}
