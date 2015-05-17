/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sven Dubbeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.svendubbeld.car.activity;

import android.app.Activity;
import android.app.UiModeManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import nl.svendubbeld.car.preference.Preferences;

/**
 * Helper activity that starts Car Mode.
 */
public class MainActivity extends Activity {

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
