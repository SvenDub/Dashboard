package nl.svendubbeld.car.activity;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import nl.svendubbeld.car.OnTargetChangeListener;
import nl.svendubbeld.car.R;
import nl.svendubbeld.car.adapter.NavigationFavoritesAdapter;
import nl.svendubbeld.car.fragment.NavigationFavoritesFragment;
import nl.svendubbeld.car.preference.Preferences;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, OnTargetChangeListener {

    private static final int REQUEST_CODE_SPEECH = 0;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    public static final int REQUEST_CODE_PERMISSION_CONTACTS = 2;

    private GoogleMap mMap;
    private Location mLocation;

    private AutoCompleteTextView mTxtTarget;
    private ImageView mTargetVoice;
    private ImageView mTargetClear;
    private NavigationFavoritesAdapter mFavoritesAdapter;

    /**
     * Sets the layout and initializes the map.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState(Bundle)}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_navigation);

        // Create fragment
        NavigationFavoritesFragment favoritesFragment = new NavigationFavoritesFragment();
        String favoritesTag = "favorites_fragment";

        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();

        if (getFragmentManager().findFragmentByTag(favoritesTag) == null) {
            fragmentTransaction.add(R.id.fragment_favorites, favoritesFragment, favoritesTag);
        }

        fragmentTransaction.commit();

        mTargetVoice = (ImageView) findViewById(R.id.target_voice);
        mTargetClear = (ImageView) findViewById(R.id.target_clear);

        mTxtTarget = (AutoCompleteTextView) findViewById(R.id.target);
        mTxtTarget.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                startNavigation();
                return true;
            }

            return false;
        });
        mTxtTarget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasInput = s.length() > 0;
                mTargetClear.setVisibility(hasInput ? View.VISIBLE : View.GONE);
                mTargetVoice.setVisibility(hasInput ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mFavoritesAdapter = new NavigationFavoritesAdapter(this, R.layout.list_item_auto_complete_navigation_favorite, true);

        mTxtTarget.setAdapter(mFavoritesAdapter);

        loadMap();
    }

    private void loadMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSION_LOCATION);
            return;
        }

        // Get last know location
        mLocation = ((LocationManager) getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Set map options
        GoogleMapOptions mapOptions = new GoogleMapOptions();

        if (mLocation != null) {
            mapOptions.camera(new CameraPosition(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 17, 60, mLocation.getBearing()));
        }

        // Create fragment
        MapFragment mapFragment = MapFragment.newInstance(mapOptions);
        String mapTag = "map_fragment";

        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();

        if (getFragmentManager().findFragmentByTag(mapTag) == null) {
            fragmentTransaction.add(R.id.map, mapFragment, mapTag);
        }

        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Snackbar.make(mTxtTarget, R.string.location_disabled, Snackbar.LENGTH_LONG);
                } else {
                    loadMap();
                }
                break;
            case REQUEST_CODE_PERMISSION_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Snackbar.make(mTxtTarget, R.string.contacts_disabled, Snackbar.LENGTH_LONG);
                } else {
                    mFavoritesAdapter.loadFromDatabase();
                }
        }
    }

    /**
     * Called when the map is ready to be used. Sets various map settings.
     *
     * @param googleMap A non-null instance of a GoogleMap associated with the {@link MapFragment}
     *                  or {@link MapView} that defines the callback.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        googleMap.setTrafficEnabled(true);
        googleMap.setOnMyLocationChangeListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    /**
     * Called when the current location is updated. Updates the position of the map.
     *
     * @param location The new location
     */
    @Override
    public void onMyLocationChange(Location location) {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        double latitudeDiff = Math.abs(cameraPosition.target.latitude - location.getLatitude());
        double longitudeDiff = Math.abs(cameraPosition.target.longitude - location.getLongitude());
        float bearingDiff = Math.abs(cameraPosition.bearing - location.getBearing());

        if (latitudeDiff > 1.0E-6 || longitudeDiff > 1.0E-6 || bearingDiff > 1) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 17, 60, location.getBearing())));
            mLocation = location;
        }
    }

    /**
     * Called when the home or work button gets clicked. Launches navigation to said location.
     *
     * @param v The button that got clicked.
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_home:
                String home = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PREF_KEY_NAVIGATION_HOME, "");
                onTargetChanged(home);
                break;
            case R.id.btn_work:
                String work = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PREF_KEY_NAVIGATION_WORK, "");
                onTargetChanged(work);
                break;
            case R.id.btn_navigation:
                startNavigation();
                break;
            case R.id.target_clear:
                onTargetChanged("");
                break;
            case R.id.target_voice:
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.navigation_input));
                startActivityForResult(intent, REQUEST_CODE_SPEECH);
                break;
        }
    }

    private void startNavigation() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + mTxtTarget.getText().toString()));
        startActivity(intent);
    }

    @Override
    public void onTargetChanged(String target) {
        mTxtTarget.setText(target);
        mTxtTarget.setSelection(target.length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_CODE_SPEECH && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            onTargetChanged(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
