package nl.svendubbeld.car.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.service.FetchAddressIntentService;
import nl.svendubbeld.car.widget.MediaControlView;
import nl.svendubbeld.car.widget.SpeedView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    private static final int REQUEST_CODE_PERMISSION_MEDIA_CONTROL = 2;

    private static final int GEOCODER_INTERVAL = 10000;

    private GoogleApiClient mGoogleApiClient;

    private SpeedView mSpeedView;
    private MediaControlView mMediaView;

    private CardView mDialerButton;
    private CardView mNavigationButton;
    private CardView mVoiceButton;
    private CardView mNotificationsButton;
    private CardView mSettingsButton;
    private CardView mExitButton;

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

        setContentView(R.layout.activity_main);

        mSpeedView = (SpeedView) findViewById(R.id.speed);
        mMediaView = (MediaControlView) findViewById(R.id.media);

        mDialerButton = (CardView) findViewById(R.id.btn_dialer);
        mNavigationButton = (CardView) findViewById(R.id.btn_navigation);
        mVoiceButton = (CardView) findViewById(R.id.btn_voice);
        mNotificationsButton = (CardView) findViewById(R.id.btn_speak_notifications);
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
        mNotificationsButton.setOnClickListener(v -> {
        });
        mSettingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getWidth()).toBundle());
        });
        mExitButton.setOnClickListener(v -> {
            mUiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaView.startMediaUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if (SystemClock.elapsedRealtime() - mLastGeocoderMillis > GEOCODER_INTERVAL) {
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

}
