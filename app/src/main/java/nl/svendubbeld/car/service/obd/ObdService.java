package nl.svendubbeld.car.service.obd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.annimon.stream.Stream;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.activity.MainActivity;
import nl.svendubbeld.car.preference.Preferences;

public class ObdService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int ONGOING_NOTIFICATION_ID = 1;
    public static final String TAG = "OBDII";

    private final IBinder mIBinder = new ObdBinder();
    private Status mStatus = Status.DISCONNECTED;
    private BluetoothSocket mSocket;
    private Thread mObdThread;
    private Thread mReconnectThread;

    private List<OnObdStatusChangeListener> mStatusChangeListeners = new CopyOnWriteArrayList<>();
    private List<OnObdDataReceivedListener> mDataReceivedListeners = new CopyOnWriteArrayList<>();

    private int mRpm;
    private int mSpeed;
    private float mEngineTemp;
    private String mVin = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Starting OBD II Service");

        startForeground(ONGOING_NOTIFICATION_ID, getNotification());

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        reconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Stopping OBD II Service");

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        if (mObdThread != null) {
            mObdThread.interrupt();
        }

        if (mReconnectThread != null) {
            mReconnectThread.interrupt();
        }

        try {
            disconnect();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing connection", e);
        } finally {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(ONGOING_NOTIFICATION_ID);
        }
    }

    private void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (mSocket != null && mSocket.isConnected() && mStatus == Status.CONNECTED) {
                try {
                    updateRpm();
                    updateSpeed();
                    updateEngineTemp();
                    updateVin();
                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "Error while executing command", e);
                    try {
                        disconnect();
                    } catch (IOException e1) {
                        Log.e(TAG, "Error while disconnecting");
                    }

                    if (!Thread.currentThread().isInterrupted()) {
                        doReconnect();
                    }
                } catch (UnableToConnectException e) {
                    Log.e(TAG, "Unable to connect", e);
                    setStatus(Status.CAR_OFF);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        Log.w(TAG, "OBD Thread interrupted while sleeping", e1);
                    }

                    try {
                        disconnect();
                    } catch (IOException e1) {
                        Log.e(TAG, "Error while disconnecting");
                    }

                    if (!Thread.currentThread().isInterrupted()) {
                        doReconnect();
                    }
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.w(TAG, "OBD Thread while sleeping", e);
                }
            }
        }
    }

    private void updateVin() throws IOException, InterruptedException {
        try {
            VinCommand vinCommand = new VinCommand();
            vinCommand.run(mSocket.getInputStream(), mSocket.getOutputStream());
            setVin(vinCommand.getCalculatedResult());
        } catch (NoDataException e) {
            Log.e(TAG, "No data available for VIN. Message: " + e.getMessage());
        } catch (UnsupportedCommandException e) {
            Log.e(TAG, "VIN is not supported. Message: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error while updating VIN. Message: " + e.getMessage());
        }
    }

    private void updateEngineTemp() throws IOException, InterruptedException {
        try {
            EngineCoolantTemperatureCommand temperatureCommand = new EngineCoolantTemperatureCommand();
            temperatureCommand.run(mSocket.getInputStream(), mSocket.getOutputStream());
            setEngineTemp(temperatureCommand.getTemperature());
        } catch (NoDataException e) {
            Log.e(TAG, "No data available for engine temperature. Message: " + e.getMessage());
        } catch (UnsupportedCommandException e) {
            Log.e(TAG, "Engine temperature is not supported. Message: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error while updating Engine temperature. Message: " + e.getMessage());
        }
    }

    private void updateSpeed() throws IOException, InterruptedException {
        try {
            SpeedCommand speedCommand = new SpeedCommand();
            speedCommand.run(mSocket.getInputStream(), mSocket.getOutputStream());
            setSpeed(speedCommand.getMetricSpeed());
        } catch (NoDataException e) {
            Log.e(TAG, "No data available for speed. Message: " + e.getMessage());
        } catch (UnsupportedCommandException e) {
            Log.e(TAG, "Speed is not supported. Message: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error while updating speed. Message: " + e.getMessage());
        }
    }

    private void updateRpm() throws IOException, InterruptedException {
        try {
            RPMCommand rpmCommand = new RPMCommand();
            rpmCommand.run(mSocket.getInputStream(), mSocket.getOutputStream());
            setRpm(rpmCommand.getRPM());
        } catch (NoDataException e) {
            Log.e(TAG, "No data available for RPM. Message: " + e.getMessage());
        } catch (UnsupportedCommandException e) {
            Log.e(TAG, "RPM is not supported. Message: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error while updating RPM. Message: " + e.getMessage());
        }
    }

    private String selectDeviceFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.contains(Preferences.PREF_KEY_OBD_DEVICE)) {
            return preferences.getString(Preferences.PREF_KEY_OBD_DEVICE, null);
        } else {
            return "";
        }
    }

    public void disconnect() throws IOException {
        setStatus(Status.DISCONNECTING);

        if (mObdThread != null) {
            mObdThread.interrupt();
        }

        if (mReconnectThread != null) {
            mReconnectThread.interrupt();
        }

        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } finally {
            setStatus(Status.DISCONNECTED);
        }
    }

    public void reconnect() {
        if (mStatus != Status.CONNECTING) {
            mReconnectThread = new Thread(this::doReconnect);
            mReconnectThread.start();

            mObdThread = new Thread(this::run);
            mObdThread.start();
        }
    }

    private void doReconnect() {
        setStatus(Status.CONNECTING);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        // Try to start Bluetooth if it is not already on
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBtIntent);
        }

        // Check if Bluetooth user chose to enable
        if (adapter.isEnabled()) {

            boolean connect = false;
            do {
                String addr = selectDeviceFromPreferences();

                if (addr.isEmpty()) {
                    setStatus(Status.DEVICE_NOT_SELECTED);
                } else {
                    connect = connect(addr);
                }

                if (!connect) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Thread interrupted while sleeping");
                        Thread.currentThread().interrupt();
                    }
                }
            }
            while (!connect && !Thread.currentThread().isInterrupted());
        } else {
            setStatus(Status.BLUETOOTH_OFF);
        }
    }

    private boolean connect(String deviceAddress) {
        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        try {
            BluetoothDevice device = btManager.getAdapter().getRemoteDevice(deviceAddress);

            try {
                mSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                mSocket.connect();

                new EchoOffCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
                new LineFeedOffCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
                new TimeoutCommand(25).run(mSocket.getInputStream(), mSocket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(mSocket.getInputStream(), mSocket.getOutputStream());

                //updateVin();

                setStatus(Status.CONNECTED);

                return true;
            } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                Log.e(TAG, "Could not connect to device", e);
                setStatus(Status.DEVICE_NOT_AVAILABLE);
                return false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Device does not behave as expected. Device might not be an OBD reader or it might be broken.", e);
                setStatus(Status.DEVICE_NOT_AVAILABLE);
                return false;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "The specified Bluetooth address is invalid", e);
            setStatus(Status.DEVICE_NOT_AVAILABLE);
            return false;
        }
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new Notification.Builder(this)
                .setContentTitle(getString(R.string.obd_service))
                .setContentText(mStatus.getString(this))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_engine)
                .build();
    }

    public void addOnObdStatusChangeListener(OnObdStatusChangeListener listener) {
        if (!mStatusChangeListeners.contains(listener)) {
            mStatusChangeListeners.add(listener);
        }
    }

    public void removeOnObdStatusChangeListener(OnObdStatusChangeListener listener) {
        if (mStatusChangeListeners.contains(listener)) {
            mStatusChangeListeners.remove(listener);
        }
    }

    public void addOnObdDataReceivedListener(OnObdDataReceivedListener listener) {
        if (!mDataReceivedListeners.contains(listener)) {
            mDataReceivedListeners.add(listener);
        }
    }

    public void removeOnObdDataReceivedListener(OnObdDataReceivedListener listener) {
        if (mDataReceivedListeners.contains(listener)) {
            mDataReceivedListeners.add(listener);
        }
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        if (mStatus != status) {
            mStatus = status;

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Stream
                    .of(mStatusChangeListeners)
                    .forEach(value -> value.onObdStatusChanged(status)));

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(ONGOING_NOTIFICATION_ID, getNotification());

            Log.i(TAG, "Status changed to: " + mStatus);
        }
    }

    public int getRpm() {
        return mRpm;
    }

    public void setRpm(int rpm) {
        if (mRpm != rpm) {
            mRpm = rpm;

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Stream
                    .of(mDataReceivedListeners)
                    .forEach(value -> value.onObdDataReceived(Data.RPM, rpm)));
        }
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        if (mSpeed != speed) {
            mSpeed = speed;

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Stream
                    .of(mDataReceivedListeners)
                    .forEach(value -> value.onObdDataReceived(Data.SPEED, speed)));
        }
    }

    public float getEngineTemp() {
        return mEngineTemp;
    }

    public void setEngineTemp(float engineTemp) {
        if (mEngineTemp != engineTemp) {
            mEngineTemp = engineTemp;

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Stream
                    .of(mDataReceivedListeners)
                    .forEach(value -> value.onObdDataReceived(Data.ENGINE_TEMP, engineTemp)));
        }
    }

    public String getVin() {
        return mVin;
    }

    public void setVin(String vin) {
        if (!mVin.equals(vin)) {
            mVin = vin;

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Stream
                    .of(mDataReceivedListeners)
                    .forEach(value -> value.onObdDataReceived(Data.VIN, vin)));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Preferences.PREF_KEY_OBD_ENABLED:
                try {
                    disconnect();
                } catch (IOException e) {
                    Log.e(TAG, "Error while closing connection", e);
                }

                if (sharedPreferences.getBoolean(key, true)) {
                    reconnect();
                }
                break;
            case Preferences.PREF_KEY_OBD_DEVICE:
                try {
                    disconnect();
                } catch (IOException e) {
                    Log.e(TAG, "Error while closing connection", e);
                }

                reconnect();
                break;
        }
    }

    public class ObdBinder extends Binder {
        public ObdService getService() {
            return ObdService.this;
        }
    }

    public enum Status {
        CONNECTED,
        DISCONNECTED,
        CONNECTING,
        DISCONNECTING,
        BLUETOOTH_OFF,
        DEVICE_NOT_SELECTED,
        DEVICE_NOT_AVAILABLE,
        CAR_OFF;

        @NonNull
        public String getString(Context context) {
            switch (this) {
                case CONNECTED:
                    return context.getString(R.string.obd_service_connected);
                case DISCONNECTED:
                    return context.getString(R.string.obd_service_disconnected);
                case CONNECTING:
                    return context.getString(R.string.obd_service_connecting);
                case DISCONNECTING:
                    return context.getString(R.string.obd_service_disconnecting);
                case BLUETOOTH_OFF:
                    return context.getString(R.string.obd_service_bluetooth_off);
                case DEVICE_NOT_SELECTED:
                    return context.getString(R.string.obd_service_device_not_selected);
                case DEVICE_NOT_AVAILABLE:
                    return context.getString(R.string.obd_service_device_not_available);
                case CAR_OFF:
                    return context.getString(R.string.obd_service_car_off);
                default:
                    return context.getString(R.string.obd_service_unknown);
            }
        }
    }

    public enum Data {
        RPM,
        SPEED,
        ENGINE_TEMP,
        VIN
    }
}