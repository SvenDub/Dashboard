package nl.svendubbeld.car.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import nl.svendubbeld.car.AddressResult;

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        try {
            Response<JsonObject> response = Ion.with(this)
                    .load("https://maps.googleapis.com/maps/api/geocode/json")
                    .addQuery("latlng", location.getLatitude() + "," + location.getLongitude())
                    .asJsonObject()
                    .withResponse().get();

            JsonObject responseObject = response.getResult();
            if (responseObject.has("results") && responseObject.get("results").isJsonArray()) {
                Type listType = new TypeToken<List<AddressResult>>() {}.getType();

                List<AddressResult> results = new Gson().fromJson(responseObject.get("results"), listType);

                Optional<AddressResult> addressResultOptional = Stream
                        .of(results)
                        .filter(result -> result.getTypes().contains("street_address") || result.getTypes().contains("route"))
                        .findFirst();

                if (addressResultOptional.isPresent()) {
                    AddressResult addressResult = addressResultOptional.get();

                    Address address = new Address(Locale.getDefault());

                    Optional<AddressResult.Component> route = Stream
                            .of(addressResult.getAddressComponents())
                            .filter(component -> component.getTypes().contains("route"))
                            .findFirst();

                    if (route.isPresent()) {
                        AddressResult.Component component = route.get();

                        address.setThoroughfare(component.getLongName());
                    }

                    Optional<AddressResult.Component> locality = Stream
                            .of(addressResult.getAddressComponents())
                            .filter(component -> component.getTypes().contains("locality"))
                            .findFirst();

                    if (locality.isPresent()) {
                        AddressResult.Component component = locality.get();

                        address.setLocality(component.getLongName());
                    }

                    deliverResultToReceiver(Constants.SUCCESS_RESULT, address);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        deliverResultToReceiver(Constants.FAILURE_RESULT, null);
    }

    private void deliverResultToReceiver(int resultCode, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY, address);
        mReceiver.send(resultCode, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "nl.svendubbeld.car";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }
}
