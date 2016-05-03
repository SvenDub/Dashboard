package nl.svendubbeld.car.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.animation.SpeakNotificationsIconAnimation;
import nl.svendubbeld.car.preference.Preferences;
import nl.svendubbeld.car.service.FetchAddressIntentService;
import nl.svendubbeld.car.widget.DateView;
import nl.svendubbeld.car.widget.MediaControlView;
import nl.svendubbeld.car.widget.SpeedView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    private static final int REQUEST_CODE_PERMISSION_MEDIA_CONTROL = 2;

    private static final int GEOCODER_INTERVAL = 10000;

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

    private SharedPreferences mSharedPref;

    private GoogleApiClient mGoogleApiClient;

    private DateView mDateView;
    private SpeedView mSpeedView;
    private MediaControlView mMediaView;

    private CardView mDialerButton;
    private CardView mNavigationButton;
    private CardView mVoiceButton;
    private CardView mNotificationsButton;
    private ImageView mNotificationsIcon;
    private CardView mSettingsButton;
    private CardView mExitButton;

    private AlertDialog mNotificationListenerDialog;

    private LocationRequest mLocationRequest;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    private long mLastGeocoderMillis = 0;

    private UiModeManager mUiModeManager;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();

        mUiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main);

        mDateView = (DateView) findViewById(R.id.date);
        mSpeedView = (SpeedView) findViewById(R.id.speed);
        mMediaView = (MediaControlView) findViewById(R.id.media);

        mDialerButton = (CardView) findViewById(R.id.btn_dialer);
        mNavigationButton = (CardView) findViewById(R.id.btn_navigation);
        mVoiceButton = (CardView) findViewById(R.id.btn_voice);
        mNotificationsButton = (CardView) findViewById(R.id.btn_speak_notifications);
        mNotificationsIcon = (ImageView) findViewById(R.id.btn_speak_notifications_icon);
        mSettingsButton = (CardView) findViewById(R.id.btn_settings);
        mExitButton = (CardView) findViewById(R.id.btn_exit);

        mDialerButton.setOnClickListener(v -> {
        });
        mNavigationButton.setOnClickListener(v -> {
        });
        mVoiceButton.setOnClickListener(v -> {
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
                        .setNeutralButton(R.string.play_store, (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox"));
                            startActivity(intent);
                        })
                        .show();
            }
        });
        mNotificationsButton.setOnClickListener(v -> mSharedPref.edit().putBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, !mPrefSpeakNotifications).apply());
        mSettingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
        });
        mExitButton.setOnClickListener(v -> {
            mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
            finish();
        });

        mNotificationListenerDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_notification_access_title)
                .setMessage(R.string.dialog_notification_access_message)
                .setPositiveButton(R.string.dialog_notification_access_positive, (dialog, which) -> {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_notification_access_negative, (dialog, which) -> {
                    mSharedPref.edit().putBoolean(Preferences.PREF_KEY_SHOW_MEDIA, false).putBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, false).apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create();

    }

    @Override
    protected void onResume() {
        super.onResume();

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

        toggleDate();
        toggleMedia();
        toggleNightMode();
        toggleSpeakNotificationsIcon(false);
        toggleSpeed();
        toggleRoad();

        mMediaView.startMediaUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);

        stopLocationUpdates();
        mMediaView.stopMediaUpdates();
    }

    @Override
    public void onBackPressed() {
        mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
        finish();
    }

    //endregion

    //region Google Play Services

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar
                .make(findViewById(R.id.layout), R.string.play_services_missing, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, v -> {
                })
                .show();
    }

    //endregion

    //region Location

    @Override
    public void onLocationChanged(Location location) {
        if (mPrefShowRoad && SystemClock.elapsedRealtime() - mLastGeocoderMillis > GEOCODER_INTERVAL) {
            startIntentService(location);

            mLastGeocoderMillis = SystemClock.elapsedRealtime();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSION_LOCATION);
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, mSpeedView);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    private void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                Address address = resultData.getParcelable(FetchAddressIntentService.Constants.RESULT_DATA_KEY);

                if (getSupportActionBar() != null) {
                    if (address != null) {
                        String road = address.getThoroughfare();
                        String locality = address.getLocality();

                        if (road != null) {
                            getSupportActionBar().setTitle(road);

                            if (locality != null) {
                                getSupportActionBar().setSubtitle(locality);
                            } else {
                                getSupportActionBar().setSubtitle("");
                            }
                        } else {
                            if (locality != null) {
                                getSupportActionBar().setTitle(locality);
                                getSupportActionBar().setSubtitle("");
                            } else {
                                getSupportActionBar().setTitle(R.string.app_name);
                                getSupportActionBar().setSubtitle("");
                            }
                        }
                    } else {
                        getSupportActionBar().setTitle(R.string.app_name);
                        getSupportActionBar().setSubtitle("");
                    }
                }
            }

        }
    }

    //endregion

    //region Permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Snackbar.make(mSpeedView, R.string.location_disabled, Snackbar.LENGTH_LONG);
                } else {
                    startLocationUpdates();
                }
                break;
            case REQUEST_CODE_PERMISSION_MEDIA_CONTROL:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Snackbar.make(mMediaView, R.string.media_disabled, Snackbar.LENGTH_LONG);
                } else {
                    mMediaView.startMediaUpdates();
                }
        }
    }

    //endregion

    //region Preferences

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
                toggleRoad();
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
    }

    private void toggleRoad() {
        if (!mPrefShowRoad) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
                getSupportActionBar().setSubtitle("");
            }
        }
    }

    private void toggleDate() {
        ((View) mDateView.getParent()).setVisibility(mPrefShowDate ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows or hides the media player depending on {@link #mPrefShowMedia}.
     */
    private void toggleMedia() {
        ((View) mMediaView.getParent()).setVisibility(mPrefShowMedia ? View.VISIBLE : View.GONE);
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
            Animation animation = new SpeakNotificationsIconAnimation(mNotificationsIcon, mPrefSpeakNotifications, alphaEnabled, alphaDisabled);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(250L);
            mNotificationsIcon.startAnimation(animation);
        } else {
            mNotificationsIcon.setImageAlpha(mPrefSpeakNotifications ? alphaEnabled : alphaDisabled);
        }
    }

    /**
     * Shows or hides the speed depending on {@link #mPrefShowSpeed}.
     */
    private void toggleSpeed() {
        ((View) mSpeedView.getParent()).setVisibility(mPrefShowSpeed ? View.VISIBLE : View.GONE);
        mSpeedView.setUnit(mPrefSpeedUnit);
    }

    //endregion
}
