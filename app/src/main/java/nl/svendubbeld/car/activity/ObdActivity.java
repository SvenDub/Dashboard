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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import nl.svendubbeld.car.Log;
import nl.svendubbeld.car.R;
import nl.svendubbeld.car.obd.ObdListener;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;

public class ObdActivity extends Activity implements ObdListener {

    private final static String TAG_OBDII = "OBDII";

    String mDeviceAddress;
    BluetoothSocket mSocket;
    Thread mThread = null;

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

        showDeviceChooser();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSocket != null && !mSocket.isConnected()) {
            onObdDeviceSelected();
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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

                BluetoothDevice device = btAdapter.getRemoteDevice(mDeviceAddress);

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                try {
                    mSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                    mSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();

                    // TODO Show error

                    return;
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                onObdDeviceConnected();
            }
        };

        mThread = new Thread(runnable);
        mThread.start();
    }

    @Override
    public void onObdDeviceConnected() {
        Log.d(TAG_OBDII, "Device connected");

        try {
            new EchoOffObdCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.d(TAG_OBDII, "Echo disabled");
            new LineFeedOffObdCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.d(TAG_OBDII, "Linefeed disabled");
            new TimeoutObdCommand(0).run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.d(TAG_OBDII, "Timeout set");
            new SelectProtocolObdCommand(ObdProtocols.AUTO).run(mSocket.getInputStream(), mSocket.getOutputStream());
            Log.d(TAG_OBDII, "Protocol: " + ObdProtocols.AUTO.name());

            onObdDeviceReady();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onObdDeviceReady() {
        Log.d(TAG_OBDII, "Device ready");

        ArrayList<ObdCommand> commands = new ArrayList<>();

        commands.add(new EngineRPMObdCommand());
        commands.add(new SpeedObdCommand());

        while (!Thread.currentThread().isInterrupted()) {
            while (mSocket.isConnected()) {
                try {
                    for (ObdCommand command : commands) {
                        command.run(mSocket.getInputStream(), mSocket.getOutputStream());
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
                // TODO handle commands result
                onObdCommandExecuted(commands);
            }
        }
    }

    @Override
    public void onObdCommandExecuted(ArrayList<ObdCommand> commands) {
        for (ObdCommand command : commands) {
            Log.d(TAG_OBDII, command.getName() + ": " + command.getFormattedResult());
        }
    }
}
