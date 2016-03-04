package nl.svendubbeld.car;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressResult {

    @SerializedName("address_components")
    private List<Component> mAddressComponents;
    @SerializedName("formatted_address")
    private String mFormattedAddress;
    @SerializedName("types")
    private List<String> mTypes;

    public AddressResult(List<Component> addressComponents, String formattedAddress, List<String> types) {
        mAddressComponents = addressComponents;
        mFormattedAddress = formattedAddress;
        mTypes = types;
    }

    public List<Component> getAddressComponents() {
        return mAddressComponents;
    }

    public String getFormattedAddress() {
        return mFormattedAddress;
    }

    public List<String> getTypes() {
        return mTypes;
    }

    public class Component {
        @SerializedName("long_name")
        private String mLongName;
        @SerializedName("short_name")
        private String mShortName;
        @SerializedName("types")
        private List<String> mTypes;

        public Component(String longName, String shortName, List<String> types) {
            mLongName = longName;
            mShortName = shortName;
            mTypes = types;
        }

        public String getLongName() {
            return mLongName;
        }

        public String getShortName() {
            return mShortName;
        }

        public List<String> getTypes() {
            return mTypes;
        }
    }
}
