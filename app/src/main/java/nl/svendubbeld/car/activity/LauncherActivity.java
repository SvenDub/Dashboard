package nl.svendubbeld.car.activity;

import android.app.UiModeManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import nl.svendubbeld.car.preference.Preferences;

/**
 * Helper activity that starts Car Mode.
 */
public class LauncherActivity extends AppCompatActivity {

    /**
     * Enables Car Mode and starts the Car Home.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState(Bundle)}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean prefKeepScreenOn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Preferences.PREF_KEY_KEEP_SCREEN_ON, true);

        // Enable Car Mode and start the Car Home, send ENABLE_CAR_MODE_ALLOW_SLEEP if the screen doesn't need to be kept on
        ((UiModeManager) getSystemService(UI_MODE_SERVICE))
                .enableCarMode(UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME
                        | (prefKeepScreenOn ? 0 : UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP));

        finish();
    }
}
