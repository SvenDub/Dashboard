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
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.preference.Preferences;

/**
 * Activity for modifying preferences.
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Sets the layout and loads {@link nl.svendubbeld.car.activity.SettingsActivity.SettingsFragment}.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState(Bundle)}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make all system bars transparent and draw behind them
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Set layout
        setContentView(R.layout.activity_settings);

        // Set action bar
        setActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load SettingsFragment
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            fragmentTransaction.replace(R.id.container, fragment).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        new BackupManager(this).dataChanged();
    }

    /**
     * Fragment for modifying preferences.
     */
    public static class SettingsFragment extends PreferenceFragment {

        /**
         * Adds all preferences.
         *
         * @param savedInstanceState If the activity is being re-initialized after previously being
         *                           shut down then this Bundle contains the data it most recently
         *                           supplied in {@link #onSaveInstanceState(Bundle)}.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            findPreference(Preferences.PREF_KEY_LICENSES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.pref_title_licenses)
                            .setView(R.layout.dialog_licenses)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();

                    return true;
                }
            });
            findPreference(Preferences.PREF_KEY_VERSION).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SvenDub/Dashboard/releases/tag/" + getString(R.string.app_version_name)));
                    startActivity(intent);
                    return true;
                }
            });
        }

        /**
         * Adds some padding to compensate for the nav bar.
         *
         * @param view               The View returned by {@link #onCreateView(LayoutInflater,
         *                           ViewGroup, Bundle)}.
         * @param savedInstanceState If non-null, this fragment is being re-constructed from a
         *                           previous saved state as given here.
         */
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Add padding to compensate for the nav bar.
            if ((getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) || (getActivity().getResources().getConfiguration().smallestScreenWidthDp >= 600)) {
                view.findViewById(android.R.id.list).setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.nav_bar_height));
            }
        }
    }
}