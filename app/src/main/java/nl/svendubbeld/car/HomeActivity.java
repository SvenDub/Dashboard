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
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.List;

public class HomeActivity extends Activity {

    Intent findHomeIntent;
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findHomeIntent = new Intent(Intent.ACTION_MAIN);
        findHomeIntent.addCategory(Intent.CATEGORY_HOME);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (((UiModeManager) getSystemService(UI_MODE_SERVICE)).getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
            Intent carIntent = new Intent(this, CarActivity.class);
            startActivity(carIntent);
        } else {
            String launcher = mSharedPref.getString("pref_key_launcher", "");

            Intent homeIntent = new Intent();
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            ComponentName componentName = null;

            if (launcher.isEmpty()) {
                List<ResolveInfo> activities = getPackageManager().queryIntentActivities(findHomeIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : activities) {
                    if (!resolveInfo.activityInfo.name.equals("nl.svendubbeld.car.HomeActivity")) {
                        componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                        break;
                    }
                }
            } else {
                String[] split = launcher.split("/");
                componentName = new ComponentName(split[0], split[1]);
                homeIntent.setComponent(componentName);

                if (getPackageManager().resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
                    List<ResolveInfo> activities = getPackageManager().queryIntentActivities(findHomeIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : activities) {
                        if (!resolveInfo.activityInfo.name.equals("nl.svendubbeld.car.HomeActivity")) {
                            componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                            break;
                        }
                    }
                }
            }


            homeIntent.setComponent(componentName);
            startActivity(homeIntent);
        }
        overridePendingTransition(0, 0);
    }
}