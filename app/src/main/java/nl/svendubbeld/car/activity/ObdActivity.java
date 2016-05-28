package nl.svendubbeld.car.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.preference.Preferences;
import nl.svendubbeld.car.service.obd.ObdService;
import nl.svendubbeld.car.service.obd.OnObdDataReceivedListener;
import nl.svendubbeld.car.service.obd.OnObdStatusChangeListener;
import nl.svendubbeld.car.unit.speed.KilometerPerHour;
import nl.svendubbeld.car.widget.DialView;
import nl.svendubbeld.car.widget.SpeedView;

public class ObdActivity extends AppCompatActivity implements OnObdDataReceivedListener, OnObdStatusChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * "pref_key_unit_speed"
     */
    private int mPrefSpeedUnit = 1;
    /**
     * "pref_key_obd_enabled"
     */
    private boolean mPrefObdEnabled = true;

    private SharedPreferences mSharedPref;

    private SpeedView mSpeedView;
    private DialView mRpmView;
    private DialView mEngineTempView;
    private DialView mFuelLevelView;
    private DialView mFuelRateView;

    private ObdService mObdService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_obd);

        mSpeedView = (SpeedView) findViewById(R.id.speed);
        mRpmView = (DialView) findViewById(R.id.rpm);
        mEngineTempView = (DialView) findViewById(R.id.engine_temp);
        mFuelLevelView = (DialView) findViewById(R.id.fuel_level);
        mFuelRateView = (DialView) findViewById(R.id.fuel_rate);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSharedPref.registerOnSharedPreferenceChangeListener(this);
        mPrefSpeedUnit = Integer.parseInt(mSharedPref.getString(Preferences.PREF_KEY_UNIT_SPEED, "1"));
        mPrefObdEnabled = mSharedPref.getBoolean(Preferences.PREF_KEY_OBD_ENABLED, true);

        mSpeedView.setUnit(mPrefSpeedUnit);

        if (mPrefObdEnabled) {
            Intent obdIntent = new Intent(this, ObdService.class);
            bindService(obdIntent, mObdConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);

        if (mObdService != null) {
            mObdService.removeOnObdDataReceivedListener(this);
            mObdService.removeOnObdStatusChangeListener(this);
        }

        unbindService(mObdConnection);
    }

    //region OBD

    private ServiceConnection mObdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mObdService = ((ObdService.ObdBinder) service).getService();
            mObdService.addOnObdStatusChangeListener(ObdActivity.this);
            mObdService.addOnObdDataReceivedListener(ObdActivity.this);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(mObdService.getStatus().getString(ObdActivity.this));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mObdService = null;
        }
    };

    @Override
    public void onObdDataReceived(ObdService.Data dataType, Object data) {
        switch (dataType) {
            case RPM:
                if (mRpmView != null) {
                    mRpmView.setProgress((int) data);
                }
                break;
            case SPEED:
                if (mSpeedView != null) {
                    mSpeedView.setSpeed(new KilometerPerHour((int) data));
                }
                break;
            case ENGINE_TEMP:
                if (mEngineTempView != null) {
                    mEngineTempView.setProgress((float) data);
                }
                break;
            case VIN:
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle((String) data);
                }
                break;
            case FUEL_LEVEL:
                if (mFuelLevelView != null) {
                    mFuelLevelView.setProgress((float) data);
                }
                break;
            case FUEL_RATE:
                if (mFuelRateView != null) {
                    mFuelRateView.setProgress((float) data);
                }
        }
    }

    @Override
    public void onObdStatusChanged(ObdService.Status status) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(status.getString(this));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Preferences.PREF_KEY_UNIT_SPEED:
                mPrefSpeedUnit = sharedPreferences.getInt(Preferences.PREF_KEY_UNIT_SPEED, 1);
                mSpeedView.setUnit(mPrefSpeedUnit);
                break;
            case Preferences.PREF_KEY_OBD_ENABLED:
                mPrefObdEnabled = sharedPreferences.getBoolean(Preferences.PREF_KEY_OBD_ENABLED, true);
                break;
        }
    }

    //endregion
}
