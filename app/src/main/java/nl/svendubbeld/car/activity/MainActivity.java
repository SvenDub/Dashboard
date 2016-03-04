package nl.svendubbeld.car.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.service.FetchAddressIntentService;
import nl.svendubbeld.car.unit.speed.KilometerPerHour;
import nl.svendubbeld.car.unit.speed.MeterPerSecond;
import nl.svendubbeld.car.unit.speed.Speed;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;

    private static final int GEOCODER_INTERVAL = 10000;

    private GoogleApiClient mGoogleApiClient;

    private TextClock mDateView;
    private TextView mSpeedView;

    private LocationRequest mLocationRequest;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    private long mLastGeocoderMillis = 0;

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

        setContentView(R.layout.activity_main);

        mDateView = (TextClock) findViewById(R.id.date);
        DateFormat shortDateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());
        if (shortDateFormat instanceof SimpleDateFormat) {
            mDateView.setFormat24Hour(((SimpleDateFormat) shortDateFormat).toPattern());
            mDateView.setFormat12Hour(((SimpleDateFormat) shortDateFormat).toPattern());
        }

        mSpeedView = (TextView) findViewById(R.id.speed);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Dismiss
                    }
                })
                .show();
    }

    //endregion

    //region Location

    @Override
    public void onLocationChanged(Location location) {
        Speed speed = new MeterPerSecond(location.getSpeed());

        Speed speedKmh = new KilometerPerHour(speed);

        mSpeedView.setText(speedKmh.getValueString(0));

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
        }
    }

    //endregion

}
