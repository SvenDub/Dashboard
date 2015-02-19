package nl.svendubbeld.car;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;

public class HomeActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        if (((UiModeManager) getSystemService(UI_MODE_SERVICE)).getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
            Intent carIntent = new Intent(this, CarActivity.class);
            startActivity(carIntent);
        } else {
            Intent homeIntent = new Intent();
            homeIntent.setComponent(new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL"));
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
        overridePendingTransition(0, 0);
    }
}