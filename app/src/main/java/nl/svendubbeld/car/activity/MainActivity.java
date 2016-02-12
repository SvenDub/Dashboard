package nl.svendubbeld.car.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextClock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import nl.svendubbeld.car.R;

public class MainActivity extends AppCompatActivity {

    private TextClock mDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDateView = (TextClock) findViewById(R.id.date);
        DateFormat shortDateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());
        if (shortDateFormat instanceof SimpleDateFormat) {
            mDateView.setFormat24Hour(((SimpleDateFormat) shortDateFormat).toPattern());
            mDateView.setFormat12Hour(((SimpleDateFormat) shortDateFormat).toPattern());
        }
    }
}
