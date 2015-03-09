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

package nl.svendubbeld.car;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_settings);

        setActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            fragmentTransaction.replace(R.id.container, fragment).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        Intent findHomeIntent;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                view.findViewById(android.R.id.list).setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.nav_bar_height));
            }

            ListPreference prefLauncher = (ListPreference) findPreference("pref_key_launcher");

            PackageManager packageManager = getActivity().getPackageManager();
            findHomeIntent = new Intent(Intent.ACTION_MAIN);
            findHomeIntent.addCategory(Intent.CATEGORY_HOME);

            List<ResolveInfo> homeActivitiesFiltered = new ArrayList<>();
            List<ResolveInfo> homeActivities = packageManager.queryIntentActivities(findHomeIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : homeActivities) {
                if (!resolveInfo.activityInfo.name.equals("nl.svendubbeld.car.HomeActivity")) {
                    homeActivitiesFiltered.add(resolveInfo);
                    if (prefLauncher.getValue().isEmpty()) {
                        prefLauncher.setValue(resolveInfo.activityInfo.packageName + "/" + resolveInfo.activityInfo.name);
                    }
                }
            }

            CharSequence[] prefLauncherEntries = new CharSequence[homeActivitiesFiltered.size()];
            CharSequence[] prefLauncherValues = new CharSequence[homeActivitiesFiltered.size()];
            for (int i = 0; i < homeActivitiesFiltered.size(); i++) {
                ResolveInfo resolveInfo = homeActivitiesFiltered.get(i);
                prefLauncherEntries[i] = resolveInfo.activityInfo.loadLabel(packageManager);
                prefLauncherValues[i] = resolveInfo.activityInfo.packageName + "/" + resolveInfo.activityInfo.name;
            }

            prefLauncher.setEntries(prefLauncherEntries);
            prefLauncher.setEntryValues(prefLauncherValues);
        }
    }
}