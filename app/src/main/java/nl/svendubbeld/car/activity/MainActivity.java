package nl.svendubbeld.car.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.service.FetchAddressIntentService;
import nl.svendubbeld.car.service.NotificationListenerService;
import nl.svendubbeld.car.widget.SpeedView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, MediaSessionManager.OnActiveSessionsChangedListener {

    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    private static final int REQUEST_CODE_PERMISSION_MEDIA_CONTROL = 2;

    private static final int GEOCODER_INTERVAL = 10000;

    private GoogleApiClient mGoogleApiClient;

    private TextClock mDateView;
    private SpeedView mSpeedView;
    private TextView mMediaTitleView;
    private TextView mMediaArtistView;
    private TextView mMediaAlbumView;
    private ImageView mMediaArtView;
    private ImageView mMediaPlayView;
    private ProgressBar mMediaPlayProgressView;
    private ImageView mMediaVolDownView;
    private ImageView mMediaPrevView;
    private ImageView mMediaNextView;
    private ImageView mMediaVolUpView;

    private LocationRequest mLocationRequest;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    private long mLastGeocoderMillis = 0;

    private MediaController mMediaController = null;
    private MediaSessionManager mMediaSessionManager;

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

        mSpeedView = (SpeedView) findViewById(R.id.speed);

        mMediaTitleView = (TextView) findViewById(R.id.media_title);
        mMediaArtistView = (TextView) findViewById(R.id.media_artist);
        mMediaAlbumView = (TextView) findViewById(R.id.media_album);
        mMediaArtView = (ImageView) findViewById(R.id.media_art);
        mMediaPlayView = (ImageView) findViewById(R.id.media_play);
        mMediaPlayProgressView = (ProgressBar) findViewById(R.id.media_play_progress);
        mMediaVolDownView = ((ImageView) findViewById(R.id.media_vol_down));
        mMediaPrevView = ((ImageView) findViewById(R.id.media_prev));
        mMediaNextView = ((ImageView) findViewById(R.id.media_next));
        mMediaVolUpView = ((ImageView) findViewById(R.id.media_vol_up));

        mMediaVolDownView.setOnClickListener(mMediaControlsListener);
        mMediaPrevView.setOnClickListener(mMediaControlsListener);
        mMediaPlayView.setOnClickListener(mMediaControlsListener);
        mMediaNextView.setOnClickListener(mMediaControlsListener);
        mMediaVolUpView.setOnClickListener(mMediaControlsListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMediaUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        stopMediaUpdates();
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

    //region Media

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

            mMediaPlayView.setImageTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor}).getColor(0, getResources().getColor(R.color.white))));
        } else {

            mMediaArtistView.setText("");
            mMediaAlbumView.setText("");
            mMediaTitleView.setText(R.string.media_idle);
            mMediaArtView.setImageResource(R.drawable.bg_default_album_art);

            Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
            PackageManager pm = getPackageManager();
            ResolveInfo info = pm.resolveActivity(i, 0);

            Drawable icon = info.loadIcon(pm);
            mMediaPlayView.setPadding(20, 20, 20, 20);
            mMediaPlayView.setImageDrawable(icon);

            mMediaPlayView.setImageTintList(null);
        }
    }

    protected void startMediaUpdates() {
        mMediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);

        ComponentName notificationListener = new ComponentName(this, NotificationListenerService.class);

        try {
            List<MediaController> controllers = mMediaSessionManager.getActiveSessions(notificationListener);
            onActiveSessionsChanged(controllers);

            mMediaSessionManager.addOnActiveSessionsChangedListener(this, notificationListener);
        } catch (SecurityException e) {
            Log.w("NotificationListener", "No Notification Access");
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_notification_access_title)
                    .setMessage(R.string.dialog_notification_access_message)
                    .setPositiveButton(R.string.dialog_notification_access_positive, (dialog, which) -> {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_notification_access_negative, (dialog, which) -> dialog.dismiss())
                    .show();

        }
    }

    protected void stopMediaUpdates() {
        if (mMediaSessionManager != null) {
            mMediaSessionManager.removeOnActiveSessionsChangedListener(this);
        }

        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaCallback);
            Log.d("MediaController", "MediaController removed");
        }
    }

    private MediaController.Callback mMediaCallback = new MediaController.Callback() {

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);

            if (mMediaTitleView != null && metadata != null) {
                mMediaArtView.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
                mMediaTitleView.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
                mMediaArtistView.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
                mMediaAlbumView.setText(metadata.getText(MediaMetadata.METADATA_KEY_ALBUM));
            }
        }

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);

            if (mMediaPlayView != null) {

                // Update play/pause button
                switch (state.getState()) {
                    case PlaybackState.STATE_BUFFERING:
                    case PlaybackState.STATE_CONNECTING:
                        mMediaPlayView.setVisibility(View.GONE);
                        mMediaPlayProgressView.setVisibility(View.VISIBLE);
                        mMediaPlayView.setImageResource(R.drawable.ic_av_pause);
                        break;
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayView.setVisibility(View.VISIBLE);
                        mMediaPlayProgressView.setVisibility(View.GONE);
                        mMediaPlayView.setImageResource(R.drawable.ic_av_pause);
                        break;
                    default:
                        mMediaPlayView.setVisibility(View.VISIBLE);
                        mMediaPlayProgressView.setVisibility(View.GONE);
                        mMediaPlayView.setImageResource(R.drawable.ic_av_play_arrow);
                        break;
                }
            }
        }
    };

    private View.OnClickListener mMediaControlsListener = new View.OnClickListener() {
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
                        if (mMediaController.getPlaybackState() != null) {
                            switch (mMediaController.getPlaybackState().getState()) {
                                case PlaybackState.STATE_BUFFERING:
                                case PlaybackState.STATE_CONNECTING:
                                    mMediaPlayView.setVisibility(View.GONE);
                                    mMediaPlayProgressView.setVisibility(View.VISIBLE);
                                case PlaybackState.STATE_PLAYING:
                                    mMediaController.getTransportControls().pause();
                                    break;
                                default:
                                    mMediaController.getTransportControls().play();
                                    break;
                            }
                        } else {
                            mMediaController.getTransportControls().play();
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
                    Snackbar.make(mMediaTitleView, R.string.media_disabled, Snackbar.LENGTH_LONG);
                } else {
                    startMediaUpdates();
                }
        }
    }

    //endregion

}
