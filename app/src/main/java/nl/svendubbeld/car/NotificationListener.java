package nl.svendubbeld.car;

import android.app.Notification;
import android.app.UiModeManager;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;

public class NotificationListener extends NotificationListenerService
        implements TextToSpeech.OnInitListener, SharedPreferences.OnSharedPreferenceChangeListener {

    Log mLog = new Log();
    boolean mPrefSpeakNotifications = true;
    SharedPreferences mSharedPref;
    TextToSpeech mTextToSpeech;
    boolean mTextToSpeechInitialized = false;
    Bundle mTextToSpeechOptions;


    public void onCreate() {
        super.onCreate();

        mTextToSpeech = new TextToSpeech(this, this);

        mTextToSpeechOptions = new Bundle();
        mTextToSpeechOptions.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION);
        mTextToSpeechOptions.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.2f);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPref.registerOnSharedPreferenceChangeListener(this);

        mPrefSpeakNotifications = mSharedPref.getBoolean("pref_key_speak_notifications", true);
        mLog.i("TextToSpeech", "Speak notifications: " + mPrefSpeakNotifications);
    }

    public void onDestroy() {
        super.onDestroy();

        mTextToSpeech.shutdown();
        mLog.i("TextToSpeech", "Stopped");

        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTextToSpeechInitialized = true;
            mLog.i("TextToSpeech", "Ready");
        }
    }

    public void onNotificationPosted(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);

        if (mTextToSpeechInitialized && mPrefSpeakNotifications && (((UiModeManager) getSystemService(UI_MODE_SERVICE)).getCurrentModeType() == 3)) {
            Notification notification = sbn.getNotification();
            if (notification.tickerText != null) {
                mTextToSpeech.playSilentUtterance(1500l, TextToSpeech.QUEUE_ADD, sbn.getId() + "_delay");
                mTextToSpeech.speak(notification.tickerText, TextToSpeech.QUEUE_ADD, mTextToSpeechOptions, sbn.getId() + "_content");
                mLog.d("TextToSpeech", "Speak: " + notification.tickerText);
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_key_speak_notifications")) {
            mPrefSpeakNotifications = sharedPreferences.getBoolean("pref_key_speak_notifications", true);
            mLog.i("TextToSpeech", "Spoken notifications: " + mPrefSpeakNotifications);
        }
    }
}