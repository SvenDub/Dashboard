package nl.svendubbeld.car.preference;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ObdDevicePreference extends ListPreference {

    public ObdDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        List<String> deviceNames = new ArrayList<>();
        final List<String> deviceAddresses = new ArrayList<>();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceNames.add(device.getName() + "\n" + device.getAddress());
                deviceAddresses.add(device.getAddress());
            }
        }

        setEntries(deviceNames.toArray(new CharSequence[deviceNames.size()]));
        setEntryValues(deviceAddresses.toArray(new CharSequence[deviceAddresses.size()]));

        super.onPrepareDialogBuilder(builder);
    }
}
