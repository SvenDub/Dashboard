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
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class CarActivity extends Activity
        implements LocationListener, GpsStatus.Listener, SharedPreferences.OnSharedPreferenceChangeListener, MediaSessionManager.OnActiveSessionsChangedListener, View.OnClickListener {


    public static final int PREF_SPEED_UNIT_KMH = 1;
    public static final int PREF_SPEED_UNIT_MPH = 2;
    public static final int PREF_SPEED_UNIT_MS = 0;

    public static final int PREF_TEMP_UNIT_C = 0;
    public static final int PREF_TEMP_UNIT_F = 1;

    // Background
    FrameLayout mBackground;
    String mPrefBackground = "launcher";

    // Status
    CardView mStatusContainer;
    TextView mBattery;
    ImageView mBatteryImage;
    TextView mTemp;
    ImageView mTempImage;
    TextView mGPS;
    ImageView mGPSImage;
    int mPrefTempUnit = 0;
    boolean mPrefShowStatus = true;

    // Buttons
    CardView mButtonDialer;
    CardView mButtonExit;
    CardView mButtonNavigation;
    CardView mButtonSettings;
    CardView mButtonSpeakNotifications;
    ImageView mButtonSpeakNotificationsIcon;
    CardView mButtonVoice;
    boolean mPrefSpeakNotifications = true;

    // Date
    CardView mDateContainer;
    TextClock mDate;
    TextClock mTime;
    boolean mPrefShowDate = true;

    // Media
    CardView mMediaContainer;
    ImageView mMediaArt;
    TextView mMediaTitle;
    TextView mMediaArtist;
    TextView mMediaAlbum;
    ImageView mMediaVolDown;
    ImageView mMediaPrev;
    ImageView mMediaPlay;
    ProgressBar mMediaPlayProgress;
    ImageView mMediaNext;
    ImageView mMediaVolUp;
    MediaSessionManager mMediaSessionManager;
    MediaController mMediaController = null;
    boolean mPrefShowMedia = true;

    // Speed
    CardView mSpeedContainer;
    TextView mSpeed;
    TextView mSpeedUnit;
    int mPrefSpeedUnit = 1;
    boolean mPrefShowSpeed = true;


    UiModeManager mUiModeManager;

    Log mLog = new Log();

    SharedPreferences mSharedPref;
    boolean mPrefKeepScreenOn = true;
    String mPrefNightMode = "auto";

    // Location
    long mLastLocationMillis = 0l;
    LocationManager mLocationManager;
    private static final String LOCATION_PROVIDER = "gps";
    private static final int GPS_UPDATE_FIX_THRESHOLD = 2000;
    private static final int GPS_UPDATE_INTERVAL = 1000;

    private void resetLayout() {
        setContentView(R.layout.activity_car);

        // Get date views
        mDateContainer = ((CardView) findViewById(R.id.date_container));
        mDate = ((TextClock) findViewById(R.id.date));
        mTime = ((TextClock) findViewById(R.id.time));

        // Get speed views
        mSpeedContainer = ((CardView) findViewById(R.id.speed_container));
        mSpeed = ((TextView) findViewById(R.id.speed));
        mSpeedUnit = ((TextView) findViewById(R.id.speed_unit));

        // Get status views
        mStatusContainer = ((CardView) findViewById(R.id.status_container));
        mBattery = ((TextView) findViewById(R.id.battery));
        mBatteryImage = ((ImageView) findViewById(R.id.battery_img));
        mTemp = ((TextView) findViewById(R.id.temp));
        mTempImage = ((ImageView) findViewById(R.id.temp_img));
        mGPS = ((TextView) findViewById(R.id.gps));
        mGPSImage = ((ImageView) findViewById(R.id.gps_img));

        // Get media views
        mMediaContainer = ((CardView) findViewById(R.id.media_container));
        mMediaArt = ((ImageView) findViewById(R.id.media_art));
        mMediaTitle = ((TextView) findViewById(R.id.media_title));
        mMediaArtist = ((TextView) findViewById(R.id.media_artist));
        mMediaAlbum = ((TextView) findViewById(R.id.media_album));
        mMediaVolDown = ((ImageView) findViewById(R.id.media_vol_down));
        mMediaPrev = ((ImageView) findViewById(R.id.media_prev));
        mMediaPlay = ((ImageView) findViewById(R.id.media_play));
        mMediaPlayProgress = ((ProgressBar) findViewById(R.id.media_play_progress));
        mMediaNext = ((ImageView) findViewById(R.id.media_next));
        mMediaVolUp = ((ImageView) findViewById(R.id.media_vol_up));

        // Get buttons
        mButtonSettings = ((CardView) findViewById(R.id.btn_settings));
        mButtonNavigation = ((CardView) findViewById(R.id.btn_navigation));
        mButtonDialer = ((CardView) findViewById(R.id.btn_dialer));
        mButtonVoice = ((CardView) findViewById(R.id.btn_voice));
        mButtonSpeakNotifications = ((CardView) findViewById(R.id.btn_speak_notifications));
        mButtonSpeakNotificationsIcon = ((ImageView) findViewById(R.id.btn_speak_notifications_icon));
        mButtonExit = ((CardView) findViewById(R.id.btn_exit));

        // Get background
        mBackground = ((FrameLayout) findViewById(R.id.bg));

        toggleDate();
        toggleSpeed();
        toggleStatus();
        toggleMedia();

        // Format the date
        String dateFormat = ((SimpleDateFormat) DateFormat.getMediumDateFormat(getApplicationContext())).toPattern();
        mDate.setFormat12Hour(null);
        mDate.setFormat24Hour(dateFormat);

        // Set media controls
        mMediaVolDown.setOnClickListener(mMediaControlsListener);
        mMediaPrev.setOnClickListener(mMediaControlsListener);
        mMediaPlay.setOnClickListener(mMediaControlsListener);
        mMediaNext.setOnClickListener(mMediaControlsListener);
        mMediaVolUp.setOnClickListener(mMediaControlsListener);
        mMediaContainer.setOnClickListener(this);

        mMediaPlay.setImageTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor}).getColor(0, getResources().getColor(R.color.white))));

        // Set status views
        mGPSImage.setImageTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary}).getColor(0, getResources().getColor(android.R.color.secondary_text_light))));
        mBatteryImage.setImageTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary}).getColor(0, getResources().getColor(android.R.color.secondary_text_light))));

        // Set buttons
        mButtonSettings.setOnClickListener(this);
        mButtonNavigation.setOnClickListener(this);
        mButtonDialer.setOnClickListener(this);
        mButtonVoice.setOnClickListener(this);
        mButtonSpeakNotifications.setOnClickListener(this);
        mButtonExit.setOnClickListener(this);

        mButtonSpeakNotificationsIcon.setBackgroundTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary}).getColor(0, getResources().getColor(android.R.color.secondary_text_light))));

        toggleSpeakNotificationsIcon();
    }

    private void toggleDate() {
        mDateContainer.setVisibility(mPrefShowDate ? View.VISIBLE : View.GONE);
    }

    private void toggleMedia() {
        mMediaContainer.setVisibility(mPrefShowMedia ? View.VISIBLE : View.GONE);
    }

    private void toggleNightMode() {
        switch (mPrefNightMode) {
            default:
            case "auto":
                mUiModeManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
                break;
            case "always":
                mUiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                break;
            case "never":
                mUiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                break;
        }
    }

    private void toggleSpeakNotificationsIcon() {
        if (!mPrefSpeakNotifications) {
            mButtonSpeakNotificationsIcon.setBackgroundResource(R.drawable.ic_notification_do_not_disturb);
            mButtonSpeakNotificationsIcon.setImageAlpha(64);
        } else {
            this.mButtonSpeakNotificationsIcon.setBackground(null);
            this.mButtonSpeakNotificationsIcon.setImageAlpha(255);
        }
    }

    private void toggleSpeed() {
        mSpeedContainer.setVisibility(mPrefShowSpeed ? View.VISIBLE : View.GONE);
        switch (mPrefSpeedUnit) {
            case PREF_SPEED_UNIT_MS:
                mSpeedUnit.setText("m/s");
                break;
            case PREF_SPEED_UNIT_KMH:
                mSpeedUnit.setText("km/h");
                break;
            case PREF_SPEED_UNIT_MPH:
                mSpeedUnit.setText("mph");
                break;
            default:
                mSpeedUnit.setText("km/h");
                break;
        }
    }

    private void toggleStatus() {
        mStatusContainer.setVisibility(View.GONE);
    }

    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaCallback);
            mLog.d("MediaController", "MediaController removed");
            mMediaController = null;
        }
        if (controllers.size() > 0) {
            mMediaController = controllers.get(0);
            mMediaController.registerCallback(mMediaCallback);
            mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
            mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
            mLog.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
        }
    }

    public void onBackPressed() {
        mUiModeManager.disableCarMode(0);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
                break;
            case R.id.btn_navigation:
                Intent navigationIntent = new Intent("android.intent.action.VIEW", Uri.parse("geo:"));
                startActivity(navigationIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
                break;
            case R.id.btn_dialer:
                Intent dialerIntent = new Intent("android.intent.action.DIAL");
                startActivity(dialerIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
                break;
            case R.id.btn_voice:
                Intent voiceIntent = new Intent("android.intent.action.VOICE_ASSIST");
                startActivity(voiceIntent);
                break;
            case R.id.btn_speak_notifications:
                mSharedPref.edit().putBoolean("pref_key_speak_notifications", !mPrefSpeakNotifications).apply();
                break;
            case R.id.btn_exit:
                mUiModeManager.disableCarMode(0);
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;
            case R.id.media_container:
                Intent mediaIntent = new Intent(this, MediaActivity.class);
                Pair[] elements = new Pair[9];
                elements[0] = Pair.create((View) mMediaArt, getString(R.string.transition_media_art));
                elements[1] = Pair.create((View) mMediaTitle, getString(R.string.transition_media_title));
                elements[2] = Pair.create((View) mMediaArtist, getString(R.string.transition_media_artist));
                elements[3] = Pair.create((View) mMediaAlbum, getString(R.string.transition_media_album));
                elements[4] = Pair.create((View) mMediaVolDown, getString(R.string.transition_media_vol_down));
                elements[5] = Pair.create((View) mMediaPrev, getString(R.string.transition_media_prev));
                if (mMediaPlay.getVisibility() == View.VISIBLE) {
                    elements[6] = Pair.create((View) mMediaPlay, getString(R.string.transition_media_play));
                } else if (mMediaPlayProgress.getVisibility() == View.VISIBLE) {
                    elements[6] = Pair.create((View) mMediaPlayProgress, getString(R.string.transition_media_play_progress));
                }
                elements[7] = Pair.create((View) mMediaNext, getString(R.string.transition_media_next));
                elements[8] = Pair.create((View) mMediaVolUp, getString(R.string.transition_media_vol_up));
                startActivity(mediaIntent, ActivityOptions.makeSceneTransitionAnimation(this, elements).toBundle());
                break;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUiModeManager = ((UiModeManager) getSystemService(UI_MODE_SERVICE));

        mUiModeManager.enableCarMode(0);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mPrefShowDate = mSharedPref.getBoolean("pref_key_show_date", true);
        mPrefShowSpeed = mSharedPref.getBoolean("pref_key_show_speed", true);
        mPrefShowMedia = mSharedPref.getBoolean("pref_key_show_media", true);
        mPrefShowStatus = mSharedPref.getBoolean("pref_key_show_status", true);
        mPrefSpeakNotifications = mSharedPref.getBoolean("pref_key_speak_notifications", true);
        mPrefKeepScreenOn = mSharedPref.getBoolean("pref_key_keep_screen_on", true);
        mPrefNightMode = mSharedPref.getString("pref_key_night_mode", "auto");
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString("pref_key_unit_speed", "1"));
        mPrefTempUnit = Integer.parseInt(mSharedPref.getString("pref_key_unit_temp", "0"));
        mPrefBackground = mSharedPref.getString("pref_key_color_bg", "launcher");

        if (mPrefKeepScreenOn) {
            getWindow().addFlags(View.KEEP_SCREEN_ON);
        }

        toggleNightMode();

        resetLayout();
    }

    protected void onDestroy() {
        super.onDestroy();
        mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
    }

    public void onGpsStatusChanged(int event) {
        /*
        int i = 0;
        int j = 0;
        Iterator localIterator = this.mLocationManager.getGpsStatus(null).getSatellites().iterator();
        while (localIterator.hasNext()) {
            if (((GpsSatellite) localIterator.next()).usedInFix())
                j++;
            i++;
        }
        int k;
        ImageView localImageView;
        if (SystemClock.elapsedRealtime() - this.mLastLocationMillis < 2000L) {
            k = 1;
            int m = k;
            TextView localTextView = this.mGPS;
            StringBuilder localStringBuilder = new StringBuilder();
            localTextView.setText(j + "/" + i);
            localImageView = this.mGPSImage;
            if (m == 0)
                break label148;
        }
        label148:
        for (int n = 2130837580; ; n = 2130837581) {
            localImageView.setImageResource(n);
            return;
            k = 0;
            break;
        }
         */
    }

    public void onLocationChanged(Location location) {
        float rawSpeed = location.getSpeed();
        String speed = "--";
        if (rawSpeed > 1.0f) {
            switch (mPrefSpeedUnit) {
                default:
                    rawSpeed *= 3.6f;
                    break;
                case PREF_SPEED_UNIT_MS:
                    break;
                case PREF_SPEED_UNIT_KMH:
                    rawSpeed *= 3.6f;
                    break;
                case PREF_SPEED_UNIT_MPH:
                    rawSpeed /= 0.44704f;
                    break;
            }

            speed = Integer.toString(Math.round(rawSpeed));
        }

        mSpeed.setText(speed);

        mLastLocationMillis = SystemClock.elapsedRealtime();
    }

    protected void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(this);

        if (mPrefShowMedia) {
            mMediaSessionManager.removeOnActiveSessionsChangedListener(this);
            if (mMediaController != null) {
                mMediaController.unregisterCallback(mMediaCallback);
                mLog.d("MediaController", "MediaController removed");
            }
        }
        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    protected void onResume() {
        super.onResume();

        mSharedPref.registerOnSharedPreferenceChangeListener(this);
        mPrefShowDate = mSharedPref.getBoolean("pref_key_show_date", true);
        mPrefShowSpeed = mSharedPref.getBoolean("pref_key_show_speed", true);
        mPrefShowMedia = mSharedPref.getBoolean("pref_key_show_media", true);
        mPrefShowStatus = mSharedPref.getBoolean("pref_key_show_status", true);
        mPrefSpeakNotifications = mSharedPref.getBoolean("pref_key_speak_notifications", true);
        mPrefKeepScreenOn = mSharedPref.getBoolean("pref_key_keep_screen_on", true);
        mPrefNightMode = mSharedPref.getString("pref_key_night_mode", "auto");
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString("pref_key_unit_speed", "1"));
        mPrefTempUnit = Integer.parseInt(mSharedPref.getString("pref_key_unit_temp", "0"));
        mPrefBackground = mSharedPref.getString("pref_key_color_bg", "launcher");

        toggleDate();
        toggleSpeed();
        toggleMedia();
        toggleStatus();
        toggleNightMode();
        toggleSpeakNotificationsIcon();

        mLocationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));

        if (!mLocationManager.isProviderEnabled(LOCATION_PROVIDER)) {
            startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
        }

        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, GPS_UPDATE_INTERVAL, 0.0f, this);

        Location location = mLocationManager.getLastKnownLocation(LOCATION_PROVIDER);

        if (location != null) {
            onLocationChanged(location);
        }

        if (mPrefShowMedia || mPrefSpeakNotifications) {
            String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_notification_access_title)
                        .setMessage(R.string.dialog_notification_access_message)
                        .setPositiveButton(R.string.dialog_notification_access_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.dialog_notification_access_negative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mSharedPref.edit().putBoolean("pref_key_show_media", false).putBoolean("pref_key_speak_notifications", false).apply();
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }

        if (mPrefShowMedia) {
            try {
                mMediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
                List<MediaController> controllers = mMediaSessionManager.getActiveSessions(new ComponentName(this, NotificationListener.class));
                if (controllers.size() > 0) {
                    mMediaController = controllers.get(0);
                    mMediaController.registerCallback(mMediaCallback);
                    mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
                    mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                    mLog.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
                }
                mMediaSessionManager.addOnActiveSessionsChangedListener(this, new ComponentName(this, NotificationListener.class));
            } catch (SecurityException localSecurityException) {
                mLog.w("NotificationListener", "No Notification Access");
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "pref_key_show_date":
                mPrefShowDate = sharedPreferences.getBoolean("pref_key_show_date", true);
                toggleDate();
                break;
            case "pref_key_show_speed":
                mPrefShowSpeed = sharedPreferences.getBoolean("pref_key_show_speed", true);
                toggleSpeed();
                break;
            case "pref_key_show_status":
                mPrefShowStatus = sharedPreferences.getBoolean("pref_key_show_status", true);
                toggleStatus();
                break;
            case "pref_key_keep_screen_on":
                mPrefKeepScreenOn = sharedPreferences.getBoolean("pref_key_keep_screen_on", true);
                if (this.mPrefKeepScreenOn) {
                    getWindow().addFlags(View.KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(View.KEEP_SCREEN_ON);
                }
                break;
            case "pref_key_show_media":
                mPrefShowMedia = this.mSharedPref.getBoolean("pref_key_show_media", true);
                toggleMedia();
                break;
            case "pref_key_speak_notifications":
                mPrefSpeakNotifications = sharedPreferences.getBoolean("pref_key_speak_notifications", true);

                if (mPrefSpeakNotifications) {
                    String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
                    if ((enabledNotificationListeners == null) || (!enabledNotificationListeners.contains(getPackageName()))) {
                        mLog.w("NotificationListener", "No Notification Access");
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_notification_access_title)
                                .setMessage(R.string.dialog_notification_access_message)
                                .setPositiveButton(R.string.dialog_notification_access_positive, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(R.string.dialog_notification_access_negative, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSharedPref.edit().putBoolean("pref_key_show_media", false).putBoolean("pref_key_speak_notifications", false).apply();
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }

                toggleSpeakNotificationsIcon();
                break;
            case "pref_key_night_mode":
                mPrefNightMode = sharedPreferences.getString("pref_key_night_mode", "auto");
                toggleNightMode();
                break;
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    BroadcastReceiver mBroadcastReceiverBattery = new BroadcastReceiver() {
        public void onReceive(Context broadcastReceiver, Intent intent) {
            /*int i = intent.getIntExtra("status", -1);
            float f1;
            int m;
            int n;
            float f2;
            String str;
            if (i != -1) {
                int j = intent.getIntExtra("level", -1);
                int k = intent.getIntExtra("scale", -1);
                f1 = intent.getIntExtra("temperature", -1) / 10.0F;
                m = Math.round(100.0F * (j / k));
                if ((i != 2) && (i != 5))
                    break label306;
                if (m >= 0.2D)
                    break label214;
                n = 2130837572;
                TextView localTextView1 = CarActivity.this.mBattery;
                StringBuilder localStringBuilder1 = new StringBuilder();
                localTextView1.setText(String.valueOf(m) + "%");
                CarActivity.this.mBatteryImage.setImageResource(n);
                f2 = f1;
                switch (CarActivity.this.mPrefTempUnit) {
                    default:
                        str = "°C";
                    case 0:
                    case 1:
                }
            }
            while (true) {
                TextView localTextView2 = CarActivity.this.mTemp;
                StringBuilder localStringBuilder2 = new StringBuilder();
                localTextView2.setText(String.valueOf(f2) + str);
                return;
                label214:
                if (m < 0.3D) {
                    n = 2130837573;
                    break;
                }
                if (m < 0.5D) {
                    n = 2130837574;
                    break;
                }
                if (m < 0.6D) {
                    n = 2130837575;
                    break;
                }
                if (m < 0.8D) {
                    n = 2130837576;
                    break;
                }
                if (m < 0.9D) {
                    n = 2130837577;
                    break;
                }
                n = 2130837578;
                break;
                label306:
                if (m < 0.2D) {
                    n = 2130837566;
                    break;
                }
                if (m < 0.3D) {
                    n = 2130837567;
                    break;
                }
                if (m < 0.5D) {
                    n = 2130837568;
                    break;
                }
                if (m < 0.6D) {
                    n = 2130837569;
                    break;
                }
                if (m < 0.8D) {
                    n = 2130837570;
                    break;
                }
                if (m < 0.9D) {
                    n = 2130837571;
                    break;
                }
                n = 2130837579;
                break;
                str = "°C";
                continue;
                str = "°F";
                f2 = 32.0F + f1 * 1.8F;
            }*/
        }
    };

    MediaController.Callback mMediaCallback = new MediaController.Callback() {
        public void onAudioInfoChanged(MediaController.PlaybackInfo playbackInfo) {
            super.onAudioInfoChanged(playbackInfo);
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if ((mMediaArt != null) && (metadata != null)) {
                mMediaArt.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
                mMediaTitle.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
                mMediaArtist.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
                mMediaAlbum.setText(metadata.getText(MediaMetadata.METADATA_KEY_ALBUM));
            }
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);

            if ((mMediaPlay != null) && (state != null)) {
                switch (state.getState()) {
                    case PlaybackState.STATE_BUFFERING:
                    case PlaybackState.STATE_CONNECTING:
                        mMediaPlay.setVisibility(View.GONE);
                        mMediaPlayProgress.setVisibility(View.VISIBLE);
                        mMediaPlay.setImageResource(R.drawable.ic_av_pause);
                        break;
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlay.setVisibility(View.VISIBLE);
                        mMediaPlayProgress.setVisibility(View.GONE);
                        mMediaPlay.setImageResource(R.drawable.ic_av_pause);
                        break;
                    default:
                        mMediaPlay.setVisibility(View.VISIBLE);
                        mMediaPlayProgress.setVisibility(View.GONE);
                        mMediaPlay.setImageResource(R.drawable.ic_av_play_arrow);
                        break;
                }
            }
        }

        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }
    };

    View.OnClickListener mMediaControlsListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mMediaController != null) {
                switch (v.getId()) {
                    case R.id.media_vol_down:
                        mMediaController.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                        break;
                    case R.id.media_prev:
                        mMediaController.getTransportControls().skipToPrevious();
                        break;
                    case R.id.media_play:
                        switch (mMediaController.getPlaybackState().getState()) {
                            case PlaybackState.STATE_BUFFERING:
                            case PlaybackState.STATE_CONNECTING:
                                mMediaPlay.setVisibility(View.GONE);
                                mMediaPlayProgress.setVisibility(View.VISIBLE);
                            case PlaybackState.STATE_PLAYING:
                                mMediaController.getTransportControls().pause();
                                break;
                            default:
                                mMediaController.getTransportControls().play();
                                break;
                        }
                        break;
                    case R.id.media_next:
                        mMediaController.getTransportControls().skipToNext();
                        break;
                    case R.id.media_vol_up:
                        mMediaController.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                        break;
                }
            } else {
                Intent mediaIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                mediaIntent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                sendOrderedBroadcast(mediaIntent, null);

                mediaIntent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
                sendOrderedBroadcast(mediaIntent, null);

                mMediaPlay.setVisibility(View.GONE);
                mMediaPlayProgress.setVisibility(View.VISIBLE);
            }
        }
    };
}