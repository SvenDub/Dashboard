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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

import nl.svendubbeld.car.Log;
import nl.svendubbeld.car.R;
import nl.svendubbeld.car.database.DatabaseHandler;
import nl.svendubbeld.car.obd.Car;
import nl.svendubbeld.car.obd.ObdListener;
import nl.svendubbeld.car.obd.VehicleIdentificationNumberCommand;
import nl.svendubbeld.car.widget.DialView;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import pt.lighthouselabs.obd.enums.ObdProtocols;

public class ObdActivity extends Activity implements ObdListener {

    private final static String TAG_OBDII = "OBDII";
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean mBtDialogOpen = false;

    private ProgressDialog mProgressDialog;
    private AlertDialog mDisconnectedDialog;

    private String mDeviceAddress;
    private BluetoothSocket mSocket;
    private Thread mThread = null;
    private DatabaseHandler mDatabaseHandler;

    private String mVin = "unknown";
    private Car mCar;

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

        setContentView(R.layout.activity_obd);

        mDatabaseHandler = new DatabaseHandler(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.dialog_bluetooth_connecting));
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        mProgressDialog.setIndeterminate(true);

        mDisconnectedDialog = new AlertDialog.Builder(ObdActivity.this)
                .setTitle(R.string.dialog_bluetooth_disconnected_title)
                .setPositiveButton(R.string.dialog_bluetooth_disconnected_reconnect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onObdDeviceSelected();
                    }
                })
                .setNeutralButton(R.string.dialog_bluetooth_disconnected_choose_device, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDeviceChooser();
                    }
                })
                .setNegativeButton(R.string.dialog_bluetooth_disconnected_disconnect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBtDialogOpen) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                mBtDialogOpen = true;
            } else {
                if (mSocket != null && !mSocket.isConnected()) {
                    onObdDeviceSelected();
                } else {
                    showDeviceChooser();
                }
            }
        } else {
            mBtDialogOpen ^= true;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mThread != null) {
            mThread.interrupt();
        }

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDisconnectedDialog != null && mDisconnectedDialog.isShowing()) {
            mDisconnectedDialog.dismiss();
        }

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void showDeviceChooser() {
        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                mDeviceAddress = devices.get(position);
                onObdDeviceSelected();
            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }

    @Override
    public void onObdDeviceSelected() {
        Log.i(TAG_OBDII, "Device selected, connecting...");

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                BluetoothDevice device = btManager.getAdapter().getRemoteDevice(mDeviceAddress);

                try {
                    mSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                    mSocket.connect();
                } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.e(TAG_OBDII, "Could not connect to device");

                    onObdDeviceDisconnected();

                    return;
                }

                onObdDeviceConnected();
            }
        };

        mThread = new Thread(runnable);
        mThread.start();
    }

    @Override
    public void onObdDeviceConnected() {
        Log.i(TAG_OBDII, "Device connected");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDisconnectedDialog.hide();
            }
        });

        try {
            new EchoOffObdCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.i(TAG_OBDII, "Echo disabled");
            new LineFeedOffObdCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.i(TAG_OBDII, "Linefeed disabled");
            new TimeoutObdCommand(0).run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.i(TAG_OBDII, "Timeout 200");
            new SelectProtocolObdCommand(ObdProtocols.AUTO).run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.i(TAG_OBDII, "Protocol: " + ObdProtocols.AUTO.name());

            onObdDeviceReady();
        } catch (InterruptedException e) {
            Log.d(TAG_OBDII, "Thread interrupted while connected");

            onObdDeviceDisconnected();
        } catch (IOException e) {
            e.printStackTrace();

            onObdDeviceDisconnected();
        }
    }

    @Override
    public void onObdDeviceReady() {
        Log.i(TAG_OBDII, "Device ready");

        mProgressDialog.dismiss();

        ArrayList<ObdCommand> commands = new ArrayList<>();

        commands.add(new EngineRPMObdCommand());
        commands.add(new SpeedObdCommand());
        commands.add(new VehicleIdentificationNumberCommand());

        while (!Thread.currentThread().isInterrupted()) {
            while (mSocket.isConnected()) {
                try {
                    for (ObdCommand command : commands) {
                        command.run(mSocket.getInputStream(), mSocket.getOutputStream());
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG_OBDII, "Thread interrupted while ready");
                } catch (IOException e) {
                    e.printStackTrace();

                    onObdDeviceDisconnected();
                }

                onObdCommandExecuted(commands);
            }

            onObdDeviceDisconnected();

            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onObdCommandExecuted(ArrayList<ObdCommand> commands) {
        for (ObdCommand command : commands) {
            String name = command.getName();

            if (name.equals(AvailableCommandNames.SPEED.getValue())) {
                ((DialView) findViewById(R.id.speed)).setProgress(((SpeedObdCommand) command).getMetricSpeed());
            } else if (name.equals(AvailableCommandNames.ENGINE_RPM.getValue())) {
                ((DialView) findViewById(R.id.rpm)).setProgress(((EngineRPMObdCommand) command).getRPM());
            } else if (name.equals("VIN")) {
                if (((VehicleIdentificationNumberCommand) command).hasData()) {
                    String vin = ((VehicleIdentificationNumberCommand) command).getVin();
                    if (vin.length() == 17) {
                        mVin = vin;
                    }
                }
            }

            try {
                mCar = mDatabaseHandler.getCar(mVin);
                if (mCar == null) {
                    Log.i(TAG_OBDII, "Adding new car with VIN: " + mVin);
                    mCar = new Car(mVin);
                    mDatabaseHandler.addCar(mCar);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onObdDeviceDisconnected() {
        Log.i(TAG_OBDII, "Device disconnected");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDisconnectedDialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        showDeviceChooser();
                        break;
                    case RESULT_CANCELED:
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.dialog_bluetooth_required_message)
                                .setPositiveButton(R.string.okay, null)
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        finish();
                                    }
                                })
                                .show();
                        break;
                }
                break;
        }
    }
}
