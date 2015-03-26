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

package nl.svendubbeld.car.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import nl.svendubbeld.car.Log;

/**
 * IntentService for fetching the address for a given location.
 */
public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super("FetchAddressIntentService");
    }

    /**
     * Fetches the address for a given location.
     *
     * @param intent The value passed to {@link #startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        // Get receiver to send the results to
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        if (mReceiver == null) {
            Log.e("Geofencing", "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get location
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        if (location == null) {
            errorMessage = "No location specified.";
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            return;
        }

        // Get geocoder
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (geocoder.isPresent()) {

            List<Address> addresses = null;

            // Fetch address
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.e("Geofencing", errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(), illegalArgumentException);
            }

            if (addresses == null || addresses.size() == 0) {
                errorMessage = "No address found at location.";
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else {
                Address address = addresses.get(0);

                if (address.getThoroughfare() != null) {
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, address.getThoroughfare());
                } else {
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, address.getFeatureName());
                }
            }

        } else {
            errorMessage = "No geocoder present.";
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }

    }

    /**
     * Send the results back to the receiver.
     *
     * @param resultCode The result code to send back to the receiver.
     * @param message    The message to send back to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    /**
     * Constants for {@link FetchAddressIntentService}.
     */
    public final class Constants {
        /**
         * Constant to indicate a success.
         */
        public static final int SUCCESS_RESULT = 0;
        /**
         * Constant to indicate a failure.
         */
        public static final int FAILURE_RESULT = 1;
        /**
         * Constant for the package name.
         */
        public static final String PACKAGE_NAME =
                "nl.svendubbeld.car";
        /**
         * Constant for the receiver extra of the Intent.
         */
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        /**
         * Constant for the result data.
         */
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        /**
         * Constant for the location extra of the Intent.
         */
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }
}
