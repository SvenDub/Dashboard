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
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.adapter.PlacesFragmentPagerAdapter;

/**
 * Activity for starting navigation.
 */
public class NavigationActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener {

    private GoogleMap mMap;
    private Location mLocation;

    private ViewPager mViewPager;

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

        // Make all system bars transparent and draw behind them
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Set layout
        setContentView(R.layout.activity_navigation);

        // Get last know location
        mLocation = ((LocationManager) getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Set map options
        GoogleMapOptions mapOptions = new GoogleMapOptions();

        if (mLocation != null) {
            mapOptions.camera(new CameraPosition(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 17, 60, mLocation.getBearing()));
        }

        // Create map fragment
        MapFragment mapFragment = MapFragment.newInstance(mapOptions);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        // Create view pager
        mViewPager = (ViewPager) findViewById(R.id.places_pager);
        mViewPager.setAdapter(new PlacesFragmentPagerAdapter(getFragmentManager(), this));

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
        googleMap.setMyLocationEnabled(true);
        googleMap.setTrafficEnabled(true);
        googleMap.setOnMyLocationChangeListener(this);
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
                String home = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_navigation_home", "");
                Intent intentHome = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + home));
                startActivity(intentHome);
                break;
            case R.id.btn_work:
                String work = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_navigation_work", "");
                Intent intentWork = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + work));
                startActivity(intentWork);
                break;
        }
    }
}
