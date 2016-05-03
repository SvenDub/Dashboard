package nl.svendubbeld.car.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextClock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import nl.svendubbeld.car.R;

public class DateView extends FrameLayout {

    private TextClock mDateView;

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_date, this);

        mDateView = (TextClock) findViewById(R.id.date_date);
        DateFormat shortDateFormat;

        if (!isInEditMode()) {
            shortDateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext());
        } else {
            shortDateFormat = DateFormat.getDateInstance();
        }

        if (shortDateFormat instanceof SimpleDateFormat) {
            mDateView.setFormat24Hour(((SimpleDateFormat) shortDateFormat).toPattern());
            mDateView.setFormat12Hour(((SimpleDateFormat) shortDateFormat).toPattern());
        }
    }
}
