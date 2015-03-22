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

package nl.svendubbeld.car.navigation;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import nl.svendubbeld.car.R;

public class NavigationActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener {

    GoogleMap mMap;
    Location mLocation;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_navigation);

        mLocation = ((LocationManager) getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);

        GoogleMapOptions mapOptions = new GoogleMapOptions();

        if (mLocation != null) {
            mapOptions.camera(new CameraPosition(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 17, 60, mLocation.getBearing()));
        }

        MapFragment mapFragment = MapFragment.newInstance(mapOptions);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        mViewPager = (ViewPager) findViewById(R.id.places_pager);
        mViewPager.setAdapter(new PlacesFragmentAdapter(getFragmentManager()));

    }

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

        Geocoder geocoder = new Geocoder(this);

        if (Geocoder.isPresent()) {
            try {
                String home = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_navigation_home", "");
                String work = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_navigation_work", "");

                if (!home.isEmpty()) {
                    List<Address> addresses = geocoder.getFromLocationName(home, 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_home)));
                    }
                }

                if (!work.isEmpty()) {
                    List<Address> addresses = geocoder.getFromLocationName(work, 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_work)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private class PlacesFragmentAdapter extends FragmentPagerAdapter {

        private Fragment mFavorites;
        private Fragment mInput;

        public PlacesFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getFavorites();
                case 1:
                    return getInput();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.navigation_favorites);
                case 1:
                    return getString(R.string.navigation_input);
            }

            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        private Fragment getFavorites() {
            if (mFavorites != null) {
                return mFavorites;
            } else {
                mFavorites = new NavigationFavoritesFragment();
                return mFavorites;
            }
        }

        private Fragment getInput() {
            if (mInput != null) {
                return mInput;
            } else {
                mInput = new NavigationInputFragment();
                return mInput;
            }
        }
    }
}
