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
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.List;

public class CarActivity extends Activity
        implements LocationListener, GpsStatus.Listener, SharedPreferences.OnSharedPreferenceChangeListener, MediaSessionManager.OnActiveSessionsChangedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static final int PREF_SPEED_UNIT_KMH = 1;
    public static final int PREF_SPEED_UNIT_MPH = 2;
    public static final int PREF_SPEED_UNIT_MS = 0;

    // Background
    ScrollView mBackground;
    String mPrefBackground = "launcher";

    // Buttons
    CardView mButtonDialer;
    CardView mButtonExit;
    CardView mButtonNavigation;
    CardView mButtonSettings;
    CardView mButtonSpeakNotifications;
    ImageView mButtonSpeakNotificationsIcon;
    CardView mButtonVoice;
    boolean mPrefSpeakNotifications = true;
    String mPrefAppsDialer = "default";
    AlertDialog mNotificationListenerDialog;

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
    TextView mSpeedAddress;
    TextView mSpeed;
    TextView mSpeedUnit;
    int mPrefSpeedUnit = 1;
    boolean mPrefShowSpeed = true;

    UiModeManager mUiModeManager;

    SharedPreferences mSharedPref;
    boolean mPrefKeepScreenOn = true;
    String mPrefNightMode = "auto";

    // Location
    long mLastLocationMillis = 0l;
    LocationManager mLocationManager;
    private static final String LOCATION_PROVIDER = "gps";
    private static final int GPS_UPDATE_INTERVAL = 1000;
    private static final int GEOFENCING_INTERVAL = 60000;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());

    private void resetLayout() {
        setContentView(R.layout.activity_car);

        // Get date views
        mDateContainer = ((CardView) findViewById(R.id.date_container));
        mDate = ((TextClock) findViewById(R.id.date));
        mTime = ((TextClock) findViewById(R.id.time));

        // Get speed views
        mSpeedContainer = ((CardView) findViewById(R.id.speed_container));
        mSpeedAddress = (TextView) findViewById(R.id.address);
        mSpeed = ((TextView) findViewById(R.id.speed));
        mSpeedUnit = ((TextView) findViewById(R.id.speed_unit));

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
        mBackground = ((ScrollView) findViewById(R.id.bg));

        toggleDate();
        toggleSpeed();
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

    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaCallback);
            Log.d("MediaController", "MediaController removed");
            mMediaController = null;
        }
        if (controllers.size() > 0) {
            mMediaController = controllers.get(0);
            mMediaController.registerCallback(mMediaCallback);
            mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
            mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
            Log.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
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
                launchNavigation(v);
                break;
            case R.id.btn_dialer:
                launchDialer(v);
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

    private void launchNavigation(View source) {
        Intent navigationIntent = new Intent("android.intent.action.VIEW", Uri.parse("geo:"));
        startActivity(navigationIntent, ActivityOptions.makeScaleUpAnimation(source, 0, 0, source.getWidth(), source.getWidth()).toBundle());
    }

    private void launchDialer(View source) {
        switch (mPrefAppsDialer) {
            case "default":
                Intent defaultIntent = new Intent("android.intent.action.DIAL");
                startActivity(defaultIntent, ActivityOptions.makeScaleUpAnimation(source, 0, 0, source.getWidth(), source.getWidth()).toBundle());
                break;
            case "builtin":
                Intent builtinIntent = new Intent(this, DialerActivity.class);
                startActivity(builtinIntent, ActivityOptions.makeSceneTransitionAnimation(this, source, getString(R.string.transition_button_dialer)).toBundle());
                break;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPS_UPDATE_INTERVAL);
        mLocationRequest.setSmallestDisplacement(0f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mPrefShowDate = mSharedPref.getBoolean("pref_key_show_date", true);
        mPrefShowSpeed = mSharedPref.getBoolean("pref_key_show_speed", true);
        mPrefShowMedia = mSharedPref.getBoolean("pref_key_show_media", true);
        mPrefSpeakNotifications = mSharedPref.getBoolean("pref_key_speak_notifications", true);
        mPrefKeepScreenOn = mSharedPref.getBoolean("pref_key_keep_screen_on", true);
        mPrefNightMode = mSharedPref.getString("pref_key_night_mode", "auto");
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString("pref_key_unit_speed", "1"));
        mPrefBackground = mSharedPref.getString("pref_key_color_bg", "launcher");
        mPrefAppsDialer = mSharedPref.getString("pref_key_dialer", "default");

        mUiModeManager = ((UiModeManager) getSystemService(UI_MODE_SERVICE));

        mUiModeManager.enableCarMode(mPrefKeepScreenOn ? 0 : UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP);

        mNotificationListenerDialog = new AlertDialog.Builder(this)
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
                .setCancelable(false)
                .create();

        toggleNightMode();

        resetLayout();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();
        mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
    }

    public void onGpsStatusChanged(int event) {
    }

    public void onLocationChanged(Location location) {
        if (SystemClock.elapsedRealtime() - mLastLocationMillis > GEOFENCING_INTERVAL) {
            startIntentService(location);

            mLastLocationMillis = SystemClock.elapsedRealtime();
        }
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
    }

    protected void onPause() {
        super.onPause();

        if (mPrefShowMedia) {
            mMediaSessionManager.removeOnActiveSessionsChangedListener(this);
            if (mMediaController != null) {
                mMediaController.unregisterCallback(mMediaCallback);
                Log.d("MediaController", "MediaController removed");
            }
        }
        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    protected void onResume() {
        super.onResume();

        mSharedPref.registerOnSharedPreferenceChangeListener(this);
        mPrefShowDate = mSharedPref.getBoolean("pref_key_show_date", true);
        mPrefShowSpeed = mSharedPref.getBoolean("pref_key_show_speed", true);
        mPrefShowMedia = mSharedPref.getBoolean("pref_key_show_media", true);
        mPrefSpeakNotifications = mSharedPref.getBoolean("pref_key_speak_notifications", true);
        mPrefKeepScreenOn = mSharedPref.getBoolean("pref_key_keep_screen_on", true);
        mPrefNightMode = mSharedPref.getString("pref_key_night_mode", "auto");
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString("pref_key_unit_speed", "1"));
        mPrefBackground = mSharedPref.getString("pref_key_color_bg", "launcher");
        mPrefAppsDialer = mSharedPref.getString("pref_key_dialer", "default");

        mUiModeManager.enableCarMode(mPrefKeepScreenOn ? 0 : UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP);

        toggleDate();
        toggleSpeed();
        toggleMedia();
        toggleNightMode();
        toggleSpeakNotificationsIcon();

        mLocationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));

        if (!mLocationManager.isProviderEnabled(LOCATION_PROVIDER)) {
            startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
        }

        Location location = mLocationManager.getLastKnownLocation(LOCATION_PROVIDER);

        if (location != null) {
            onLocationChanged(location);
        }

        if (mPrefShowMedia || mPrefSpeakNotifications) {
            String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(getPackageName())) {
                if (!mNotificationListenerDialog.isShowing()) {
                    mNotificationListenerDialog.show();
                }
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
                    Log.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
                }
                mMediaSessionManager.addOnActiveSessionsChangedListener(this, new ComponentName(this, NotificationListener.class));
            } catch (SecurityException localSecurityException) {
                Log.w("NotificationListener", "No Notification Access");
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
            case "pref_key_keep_screen_on":
                mPrefKeepScreenOn = sharedPreferences.getBoolean("pref_key_keep_screen_on", true);
                mUiModeManager.enableCarMode(mPrefKeepScreenOn ? 0 : UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP);
                break;
            case "pref_key_show_media":
                mPrefShowMedia = sharedPreferences.getBoolean("pref_key_show_media", true);
                toggleMedia();
                break;
            case "pref_key_speak_notifications":
                mPrefSpeakNotifications = sharedPreferences.getBoolean("pref_key_speak_notifications", true);

                if (mPrefSpeakNotifications) {
                    String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
                    if ((enabledNotificationListeners == null) || (!enabledNotificationListeners.contains(getPackageName()))) {
                        Log.w("NotificationListener", "No Notification Access");
                        if (!mNotificationListenerDialog.isShowing()) {
                            mNotificationListenerDialog.show();
                        }
                    }
                }

                toggleSpeakNotificationsIcon();
                break;
            case "pref_key_night_mode":
                mPrefNightMode = sharedPreferences.getString("pref_key_night_mode", "auto");
                toggleNightMode();
                break;
            case "pref_key_dialer":
                mPrefAppsDialer = sharedPreferences.getString("pref_key_dialer", "default");
                break;
        }
    }

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

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                mSpeedAddress.setText(resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY));
            } else {
                mSpeedAddress.setText("");
            }

        }
    }
}