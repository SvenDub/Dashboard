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
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.app.backup.BackupManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.GpsSatellite;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.text.SimpleDateFormat;
import java.util.List;

import nl.svendubbeld.car.Log;
import nl.svendubbeld.car.R;
import nl.svendubbeld.car.animation.FadeBackgroundInOutAnimation;
import nl.svendubbeld.car.animation.SpeakNotificationsIconAnimation;
import nl.svendubbeld.car.preference.Preferences;
import nl.svendubbeld.car.service.FetchAddressIntentService;
import nl.svendubbeld.car.service.NotificationListenerService;

/**
 * Activity to show when the systems runs in Car Mode.
 */
public class CarActivity extends Activity
        implements LocationListener, SharedPreferences.OnSharedPreferenceChangeListener, MediaSessionManager.OnActiveSessionsChangedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GpsStatus.Listener {

    /**
     * Constant for speed in km/h.
     */
    public static final int PREF_SPEED_UNIT_KMH = 1;
    /**
     * Constant for speed in mph.
     */
    public static final int PREF_SPEED_UNIT_MPH = 2;
    /**
     * Constant for speed in m/s.
     */
    public static final int PREF_SPEED_UNIT_MS = 0;
    /**
     * Minimum interval between GPS updates.
     */
    private static final int GPS_UPDATE_INTERVAL = 1000;
    /**
     * Threshold between GPS updated to detect locked GPS.
     */
    private static final int GPS_FIX_THRESHOLD = 2 * GPS_UPDATE_INTERVAL;
    /**
     * Minimum interval between geofencing.
     */
    private static final int GEOFENCING_INTERVAL = 60000;

    /**
     * "pref_key_speak_notifications"
     */
    private boolean mPrefSpeakNotifications = true;
    /**
     * "pref_key_apps_dialer"
     */
    private String mPrefAppsDialer = "default";
    /**
     * "pref_key_show_date"
     */
    private boolean mPrefShowDate = true;
    /**
     * "pref_key_show_media"
     */
    private boolean mPrefShowMedia = true;
    /**
     * "pref_key_show_speed"
     */
    private boolean mPrefShowSpeed = true;
    /**
     * "pref_key_show_road"
     */
    private boolean mPrefShowRoad = true;
    /**
     * "pref_key_unit_speed"
     */
    private int mPrefSpeedUnit = 1;
    /**
     * "pref_key_keep_screen_on"
     */
    private boolean mPrefKeepScreenOn = true;
    /**
     * "pref_key_night_mode"
     */
    private String mPrefNightMode = "auto";

    // Buttons
    private CardView mButtonDialer;
    private CardView mButtonExit;
    private CardView mButtonNavigation;
    private CardView mButtonSettings;
    private CardView mButtonSpeakNotifications;
    private ImageView mButtonSpeakNotificationsIcon;
    private CardView mButtonVoice;
    private AlertDialog mNotificationListenerDialog;

    // Date
    private CardView mDateContainer;
    private TextClock mDate;
    private TextClock mTime;

    // Media
    private CardView mMediaContainer;
    private ImageView mMediaArt;
    private TextView mMediaTitle;
    private TextView mMediaArtist;
    private TextView mMediaAlbum;
    private ImageView mMediaVolDown;
    private ImageView mMediaPrev;
    private ImageView mMediaPlay;
    private ProgressBar mMediaPlayProgress;
    private ImageView mMediaNext;
    private ImageView mMediaVolUp;
    private MediaSessionManager mMediaSessionManager;
    private MediaController mMediaController = null;

    // Speed
    private CardView mSpeedContainer;
    private TextView mSpeedAddress;
    private TextView mSpeed;
    private TextView mSpeedUnit;

    // GPS status
    private LinearLayout mGpsStatusContainer;
    private ImageView mGpsStatusIcon;
    private TextView mGpsStatus;
    private long mLastGpsMillis = 0l;

    private LocationManager mLocationManager;
    private UiModeManager mUiModeManager;
    private SharedPreferences mSharedPref;

    // Location
    private long mLastGeofencingMillis = 0l;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());

    /**
     * Callback for the MediaController.
     */
    private MediaController.Callback mMediaCallback = new MediaController.Callback() {

        @Override
        public void onAudioInfoChanged(MediaController.PlaybackInfo playbackInfo) {
            super.onAudioInfoChanged(playbackInfo);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if ((mMediaArt != null) && (metadata != null)) {

                // Update media container
                mMediaArt.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
                mMediaTitle.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
                mMediaArtist.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
                mMediaAlbum.setText(metadata.getText(MediaMetadata.METADATA_KEY_ALBUM));
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);

            if ((mMediaPlay != null) && (state != null)) {

                // Update play/pause button
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

        @Override
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }
    };

    /**
     * OnClickListener for the media controls.
     */
    private View.OnClickListener mMediaControlsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMediaController != null) {

                // Handle media controls
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

                Intent mediaIntent = new Intent(Intent.ACTION_MAIN);
                mediaIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
                mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mediaIntent);

            }
        }
    };

    private android.location.LocationListener mGpsLocationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastGpsMillis = SystemClock.elapsedRealtime();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * Resets the layout according to the
     */
    private void resetLayout() {
        setContentView(R.layout.activity_car);

        // Get date views
        mDateContainer = (CardView) findViewById(R.id.date_container);
        mDate = (TextClock) findViewById(R.id.date);
        mTime = (TextClock) findViewById(R.id.time);

        // Get speed views
        mSpeedContainer = (CardView) findViewById(R.id.speed_container);
        mSpeedAddress = (TextView) findViewById(R.id.address);
        mSpeed = (TextView) findViewById(R.id.speed);
        mSpeedUnit = (TextView) findViewById(R.id.speed_unit);

        // Get GPS status views
        mGpsStatusContainer = (LinearLayout) findViewById(R.id.gps_status_container);
        mGpsStatusIcon = (ImageView) findViewById(R.id.gps_status_icon);
        mGpsStatus = (TextView) findViewById(R.id.gps_status_label);

        // Get media views
        mMediaContainer = (CardView) findViewById(R.id.media_container);
        mMediaArt = (ImageView) findViewById(R.id.media_art);
        mMediaTitle = (TextView) findViewById(R.id.media_title);
        mMediaArtist = (TextView) findViewById(R.id.media_artist);
        mMediaAlbum = (TextView) findViewById(R.id.media_album);
        mMediaVolDown = (ImageView) findViewById(R.id.media_vol_down);
        mMediaPrev = (ImageView) findViewById(R.id.media_prev);
        mMediaPlay = (ImageView) findViewById(R.id.media_play);
        mMediaPlayProgress = (ProgressBar) findViewById(R.id.media_play_progress);
        mMediaNext = (ImageView) findViewById(R.id.media_next);
        mMediaVolUp = (ImageView) findViewById(R.id.media_vol_up);

        // Get buttons
        mButtonSettings = (CardView) findViewById(R.id.btn_settings);
        mButtonNavigation = (CardView) findViewById(R.id.btn_navigation);
        mButtonDialer = (CardView) findViewById(R.id.btn_dialer);
        mButtonVoice = (CardView) findViewById(R.id.btn_voice);
        mButtonSpeakNotifications = (CardView) findViewById(R.id.btn_speak_notifications);
        mButtonSpeakNotificationsIcon = (ImageView) findViewById(R.id.btn_speak_notifications_icon);
        mButtonExit = (CardView) findViewById(R.id.btn_exit);

        toggleDate();
        toggleSpeed();
        toggleMedia();

        // Format the date
        String dateFormat = ((SimpleDateFormat) DateFormat.getMediumDateFormat(getApplicationContext())).toPattern();
        mDate.setFormat12Hour(null);
        mDate.setFormat24Hour(dateFormat);

        // Format GPS status
        Animation gpsStatusIconAnimation = new FadeBackgroundInOutAnimation(mGpsStatusIcon);
        gpsStatusIconAnimation.setDuration(2000l);
        gpsStatusIconAnimation.setInterpolator(new LinearInterpolator());
        gpsStatusIconAnimation.setRepeatCount(Animation.INFINITE);
        mGpsStatusIcon.startAnimation(gpsStatusIconAnimation);

        mSpeedContainer.setOnClickListener(this);

        // Set media controls
        mMediaVolDown.setOnClickListener(mMediaControlsListener);
        mMediaPrev.setOnClickListener(mMediaControlsListener);
        mMediaPlay.setOnClickListener(mMediaControlsListener);
        mMediaNext.setOnClickListener(mMediaControlsListener);
        mMediaVolUp.setOnClickListener(mMediaControlsListener);
        mMediaContainer.setOnClickListener(this);

        // Set buttons
        mButtonSettings.setOnClickListener(this);
        mButtonNavigation.setOnClickListener(this);
        mButtonDialer.setOnClickListener(this);
        mButtonVoice.setOnClickListener(this);
        mButtonSpeakNotifications.setOnClickListener(this);
        mButtonExit.setOnClickListener(this);

        toggleSpeakNotificationsIcon(false);
    }

    /**
     * Shows or hides the date depending on {@link #mPrefShowDate}.
     */
    private void toggleDate() {
        mDateContainer.setVisibility(mPrefShowDate ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows or hides the media player depending on {@link #mPrefShowMedia}.
     */
    private void toggleMedia() {
        mMediaContainer.setVisibility(mPrefShowMedia ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets the night mode according to {@link #mPrefNightMode}.
     */
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

    /**
     * Sets the speak notifications toggle to reflect {@link #mPrefSpeakNotifications}. Animation is
     * enabled.
     */
    private void toggleSpeakNotificationsIcon() {
        toggleSpeakNotificationsIcon(true);
    }

    /**
     * Sets the speak notifications toggle to reflect {@link #mPrefSpeakNotifications}.
     *
     * @param animate Whether to use an animation for the change.
     */
    private void toggleSpeakNotificationsIcon(boolean animate) {
        int alphaEnabled = 255;
        int alphaDisabled = 64;

        if (animate) {
            Animation animation = new SpeakNotificationsIconAnimation(mButtonSpeakNotificationsIcon, mPrefSpeakNotifications, alphaEnabled, alphaDisabled);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(250l);
            mButtonSpeakNotificationsIcon.startAnimation(animation);
        } else {
            mButtonSpeakNotificationsIcon.setImageAlpha(mPrefSpeakNotifications ? alphaEnabled : alphaDisabled);
        }
    }

    /**
     * Shows or hides the speed and road depending on {@link #mPrefShowSpeed} and {@link
     * #mPrefShowRoad}.
     */
    private void toggleSpeed() {
        mSpeedContainer.setVisibility(mPrefShowSpeed ? View.VISIBLE : View.GONE);
        mSpeedAddress.setVisibility(mPrefShowRoad ? View.VISIBLE : View.GONE);
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

    /**
     * Called when the list of active {@link MediaController MediaControllers} changes.
     *
     * @param controllers List of active MediaControllers
     */
    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (controllers.size() > 0) {

            if (mMediaController != null) {
                if (!controllers.get(0).getSessionToken().equals(mMediaController.getSessionToken())) {
                    // Detach current controller
                    mMediaController.unregisterCallback(mMediaCallback);
                    Log.d("MediaController", "MediaController removed");
                    mMediaController = null;

                    // Attach new controller
                    mMediaController = controllers.get(0);
                    mMediaController.registerCallback(mMediaCallback);
                    mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
                    mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                    Log.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
                }
            } else {
                // Attach new controller
                mMediaController = controllers.get(0);
                mMediaController.registerCallback(mMediaCallback);
                mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
                mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                Log.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
            }

            mMediaPlay.setImageTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor}).getColor(0, getResources().getColor(R.color.white))));
        } else {

            mMediaArtist.setText("");
            mMediaAlbum.setText("");
            mMediaTitle.setText(R.string.media_idle);
            mMediaArt.setImageResource(R.drawable.bg_default_album_art);

            Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
            PackageManager pm = getPackageManager();
            ResolveInfo info = pm.resolveActivity(i, 0);

            Drawable icon = info.loadIcon(pm);
            mMediaPlay.setPadding(20, 20, 20, 20);
            mMediaPlay.setImageDrawable(icon);

            mMediaPlay.setImageTintList(null);
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key. Disables Car Mode and
     * closes the activity.
     */
    @Override
    public void onBackPressed() {
        mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
    }

    /**
     * OnClickListener for the buttons/cards.
     *
     * @param v The button/card that received the click.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
                break;
            case R.id.btn_navigation:
                try {
                    if (NotificationListenerService.getService().isMapsRunning()) {
                        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"));
                        startActivity(navigationIntent);
                    } else {
                        launchNavigation(v);
                    }
                } catch (Exception e) {
                    Log.e("NotificationListenerService", "Service not available.");
                    launchNavigation(v);
                }
                break;
            case R.id.btn_dialer:
                launchDialer(v);
                break;
            case R.id.btn_voice:
                // Launch voice assistance. It launches Google Now over the current activity instead
                // of switching to it, in contrast to Intent.ACTION_VOICE_COMMAND.
                Intent voiceIntent = new Intent("android.intent.action.VOICE_ASSIST");
                try {
                    startActivity(voiceIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_voice_assist_not_found_title)
                            .setMessage(R.string.dialog_voice_assist_not_found_message)
                            .setPositiveButton(R.string.okay, null)
                            .setNeutralButton(R.string.play_store, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox"));
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
                break;
            case R.id.btn_speak_notifications:
                // Toggle spoken notifications
                mSharedPref.edit().putBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, !mPrefSpeakNotifications).apply();
                break;
            case R.id.btn_exit:
                // Disable Car Mode and exit the activity
                mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
                break;
            case R.id.media_container:
                // Launch MediaActivity with a scene transition
                Intent mediaIntent = new Intent(this, MediaActivity.class);

                // Views to use in the transition
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
            case R.id.speed_container:
                // Launch OBD2Activity
                Intent obd2Intent = new Intent(this, ObdActivity.class);
                startActivity(obd2Intent);
        }
    }

    /**
     * Launches {@link NavigationActivity} with a ScaleUpAnimation.
     *
     * @param v The view to originate the animation from.
     */
    private void launchNavigation(View v) {
        Intent navigationIntent = new Intent(this, NavigationActivity.class);
        startActivity(navigationIntent, ActivityOptions.makeSceneTransitionAnimation(this, findViewById(R.id.btn_navigation_image), getString(R.string.transition_button_navigation)).toBundle());
    }

    /**
     * Launches either {@link DialerActivity} or the default Dialer application with an Animation.
     * What gets launched depends on {@link #mPrefAppsDialer}.
     *
     * @param v The view to originate the animation from.
     */
    private void launchDialer(View v) {
        switch (mPrefAppsDialer) {
            case "default":
                Intent defaultIntent = new Intent(Intent.ACTION_DIAL);
                startActivity(defaultIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
                break;
            case "builtin":
                Intent builtinIntent = new Intent(this, DialerActivity.class);
                startActivity(builtinIntent, ActivityOptions.makeSceneTransitionAnimation(this, v, getString(R.string.transition_button_dialer)).toBundle());
                break;
        }
    }

    /**
     * Gets all preferences, initializes {@link #mGoogleApiClient}, and sets the layout.
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

        // Initialize the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        // Create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPS_UPDATE_INTERVAL);
        mLocationRequest.setSmallestDisplacement(0f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Get all preferences
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mPrefShowDate = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_DATE, true);
        mPrefShowSpeed = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_SPEED, true);
        mPrefShowRoad = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_ROAD, true);
        mPrefShowMedia = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_MEDIA, true);
        mPrefSpeakNotifications = mSharedPref.getBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, true);
        mPrefKeepScreenOn = mSharedPref.getBoolean(Preferences.PREF_KEY_KEEP_SCREEN_ON, true);
        mPrefNightMode = mSharedPref.getString(Preferences.PREF_KEY_NIGHT_MODE, "auto");
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString(Preferences.PREF_KEY_UNIT_SPEED, "1"));
        mPrefAppsDialer = mSharedPref.getString(Preferences.PREF_KEY_DIALER, "builtin");

        // Get Managers
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mUiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);

        // Create dialog for when no notification access is granted
        mNotificationListenerDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_notification_access_title)
                .setMessage(R.string.dialog_notification_access_message)
                .setPositiveButton(R.string.dialog_notification_access_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_notification_access_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedPref.edit().putBoolean(Preferences.PREF_KEY_SHOW_MEDIA, false).putBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, false).apply();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();

        toggleNightMode();

        resetLayout();

        if (mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_TUTORIAL, true)) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.speed_container, this))
                    .setContentTitle(R.string.tutorial_title)
                    .setContentText(R.string.tutorial_text)
                    .hideOnTouchOutside()
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            mSharedPref.edit().putBoolean(Preferences.PREF_KEY_SHOW_TUTORIAL, false).apply();
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }
                    })
                    .build();
        }
    }

    /**
     * Disconnects {@link #mGoogleApiClient} and disables Car Mode.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    /**
     * Called when the current location is updated. Updates the current speed and road.
     *
     * @param location The new location
     */
    @Override
    public void onLocationChanged(Location location) {

        // Only fetch the road if it is visible and the minimum interval has elapsed
        if (mPrefShowSpeed && mPrefShowRoad && SystemClock.elapsedRealtime() - mLastGeofencingMillis > GEOFENCING_INTERVAL) {
            startIntentService(location);

            mLastGeofencingMillis = SystemClock.elapsedRealtime();
        }

        // Get the speed and convert it to the unit specified
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

    /**
     * Detaches al listeners.
     */
    @Override
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

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }

        mLocationManager.removeUpdates(mGpsLocationListener);
        mLocationManager.removeGpsStatusListener(this);
    }

    /**
     * Attaches all listeners and updates all preferences.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Reconnect GoogleApiClient
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }

        // Listen for GpsStatus
        mLocationManager.addGpsStatusListener(this);
        if (mLocationManager.isProviderEnabled("gps")) {
            mLocationManager.requestLocationUpdates("gps", GPS_UPDATE_INTERVAL, 0.0f, mGpsLocationListener);
        }

        // Get all preferences
        mSharedPref.registerOnSharedPreferenceChangeListener(this);
        mPrefShowDate = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_DATE, true);
        mPrefShowSpeed = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_SPEED, true);
        mPrefShowRoad = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_ROAD, true);
        mPrefShowMedia = mSharedPref.getBoolean(Preferences.PREF_KEY_SHOW_MEDIA, true);
        mPrefSpeakNotifications = mSharedPref.getBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, true);
        mPrefKeepScreenOn = mSharedPref.getBoolean(Preferences.PREF_KEY_KEEP_SCREEN_ON, true);
        mPrefNightMode = mSharedPref.getString(Preferences.PREF_KEY_NIGHT_MODE, "auto");
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString(Preferences.PREF_KEY_UNIT_SPEED, "1"));
        mPrefAppsDialer = mSharedPref.getString(Preferences.PREF_KEY_DIALER, "builtin");

        // Set Car Mode to either keep screen on or not
        mUiModeManager.enableCarMode(mPrefKeepScreenOn ? 0 : UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP);

        toggleDate();
        toggleSpeed();
        toggleMedia();
        toggleNightMode();
        toggleSpeakNotificationsIcon(false);

        // Check notification access
        if (mPrefShowMedia || mPrefSpeakNotifications) {
            String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(getPackageName())) {
                if (!mNotificationListenerDialog.isShowing()) {
                    mNotificationListenerDialog.show();
                }
            }
        }

        // Check media access and connect to session
        if (mPrefShowMedia) {
            try {
                mMediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
                List<MediaController> controllers = mMediaSessionManager.getActiveSessions(new ComponentName(this, NotificationListenerService.class));
                onActiveSessionsChanged(controllers);
                mMediaSessionManager.addOnActiveSessionsChangedListener(this, new ComponentName(this, NotificationListenerService.class));
            } catch (SecurityException e) {
                Log.w("NotificationListener", "No Notification Access");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Preferences.PREF_KEY_SHOW_DATE:
                mPrefShowDate = sharedPreferences.getBoolean(Preferences.PREF_KEY_SHOW_DATE, true);
                toggleDate();
                break;
            case Preferences.PREF_KEY_SHOW_SPEED:
                mPrefShowSpeed = sharedPreferences.getBoolean(Preferences.PREF_KEY_SHOW_SPEED, true);
                toggleSpeed();
                break;
            case Preferences.PREF_KEY_SHOW_ROAD:
                mPrefShowRoad = sharedPreferences.getBoolean(Preferences.PREF_KEY_SHOW_ROAD, true);
                toggleSpeed();
                break;
            case Preferences.PREF_KEY_KEEP_SCREEN_ON:
                mPrefKeepScreenOn = sharedPreferences.getBoolean(Preferences.PREF_KEY_KEEP_SCREEN_ON, true);
                mUiModeManager.enableCarMode(UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME | (mPrefKeepScreenOn ? 0 : UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP));
                break;
            case Preferences.PREF_KEY_SHOW_MEDIA:
                mPrefShowMedia = sharedPreferences.getBoolean(Preferences.PREF_KEY_SHOW_MEDIA, true);
                toggleMedia();
                break;
            case Preferences.PREF_KEY_SPEAK_NOTIFICATIONS:
                mPrefSpeakNotifications = sharedPreferences.getBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, true);

                // Check notification access
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
            case Preferences.PREF_KEY_NIGHT_MODE:
                mPrefNightMode = sharedPreferences.getString(Preferences.PREF_KEY_NIGHT_MODE, "auto");
                toggleNightMode();
                break;
            case Preferences.PREF_KEY_DIALER:
                mPrefAppsDialer = sharedPreferences.getString(Preferences.PREF_KEY_DIALER, "builtin");
                break;
        }

        new BackupManager(this).dataChanged();
    }

    /**
     * <p> After calling {@link GoogleApiClient#connect()}, this method will be invoked
     * asynchronously when the connect request has successfully completed. After this callback, the
     * application can make requests on other methods provided by the client and expect that no user
     * intervention is required to call methods that use account and scopes provided to the client
     * constructor.</p>
     *
     * <p> Note that the contents of the {@code connectionHint} Bundle are defined by the specific
     * services. Please see the documentation of the specific implementation of {@link
     * GoogleApiClient} you are using for more information.</p>
     *
     * @param connectionHint Bundle of data provided to clients by Google Play services. May be null
     *                       if no content is provided by the service.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * <p> Called when the client is temporarily in a disconnected state. This can happen if there
     * is a problem with the remote service (e.g. a crash or resource problem causes it to be killed
     * by the system). When called, all requests have been canceled and no outstanding listeners
     * will be executed. GoogleApiClient will automatically attempt to restore the connection.
     * Applications should disable UI components that require the service, and wait for a call to
     * {@link #onConnected(Bundle)} to re-enable them.</p>
     *
     * @param cause The reason for the disconnection. Defined by constants {@code \CAUSE_*}.
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /**
     * Called when there was an error connecting the client to the service.
     *
     * @param result A {@link ConnectionResult} that can be used for resolving the error, and
     *               deciding what sort of error occurred. To resolve the error, the resolution must
     *               be started from an activity with a non-negative {@code requestCode} passed to
     *               {@link ConnectionResult#startResolutionForResult(Activity, int)}. Applications
     *               should implement onActivityResult in their Activity to call {@link
     *               GoogleApiClient#connect()} again if the user has resolved the issue (resultCode
     *               is {@link #RESULT_OK}).
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    /**
     * Starts {@link FetchAddressIntentService} to resolve the current road and sends it results to
     * {@link #mResultReceiver}.
     *
     * @param location The location to resolve.
     */
    private void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        int satellites = 0;
        int fixedSatellites = 0;

        for (GpsSatellite satellite : mLocationManager.getGpsStatus(null).getSatellites()) {
            if (satellite.usedInFix()) {
                fixedSatellites++;
            }
            satellites++;
        }

        if (SystemClock.elapsedRealtime() - mLastGpsMillis < GPS_FIX_THRESHOLD) {
            mGpsStatusContainer.setVisibility(View.GONE);
        } else {
            mGpsStatusContainer.setVisibility(View.VISIBLE);
            mGpsStatusIcon.setImageResource(R.drawable.ic_device_gps_not_fixed);
            mGpsStatus.setText(fixedSatellites + "/" + satellites);
        }

    }

    /**
     * ResultReceiver for the current address.
     */
    private class AddressResultReceiver extends ResultReceiver {
        /**
         * Create a new ResultReceive to receive results.  Your {@link #onReceiveResult} method will
         * be called from the thread running <var>handler</var> if given, or from an arbitrary
         * thread if null.
         *
         * @param handler The thread to run {@code onReceiveResult}
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Updates the current road.
         *
         * @param resultCode Arbitrary result code delivered by the sender, as defined by the
         *                   sender.
         * @param resultData Any additional data provided by the sender.
         */
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