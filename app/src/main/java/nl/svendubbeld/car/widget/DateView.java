package nl.svendubbeld.car.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import nl.svendubbeld.car.R;

public class DateView extends FrameLayout {

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_date, this);
    }
}
