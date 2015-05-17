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

import android.app.Notification;
import android.app.UiModeManager;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;

import nl.svendubbeld.car.Log;
import nl.svendubbeld.car.preference.Preferences;

/**
 * NotificationListener that speaks notifications as they come in.
 */
public class NotificationListenerService extends android.service.notification.NotificationListenerService
        implements TextToSpeech.OnInitListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static NotificationListenerService mService = null;

    /**
     * "pref_key_speak_notifications"
     */
    private boolean mPrefSpeakNotifications = true;

    /**
     * "pref_key_speak_notifications_volume"
     */
    private float mPrefSpeakNotificationsVolume = 0.5f;

    private SharedPreferences mSharedPref;
    private TextToSpeech mTextToSpeech;
    private boolean mTextToSpeechInitialized = false;
    private Bundle mTextToSpeechOptions;

    public static NotificationListenerService getService() {
        return mService;
    }

    /**
     * Initializes the TTS engine and gets the preferences.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mService = this;

        // Initialize TTS engine
        mTextToSpeech = new TextToSpeech(this, this);

        // Create TTS options
        mTextToSpeechOptions = new Bundle();
        mTextToSpeechOptions.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION);
        mTextToSpeechOptions.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mPrefSpeakNotificationsVolume);

        // Get preferences
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPref.registerOnSharedPreferenceChangeListener(this);

        mPrefSpeakNotifications = mSharedPref.getBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, true);
        mPrefSpeakNotificationsVolume = mSharedPref.getFloat(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS_VOLUME, 0.5f);

        Log.i("TextToSpeech", "Speak notifications: " + mPrefSpeakNotifications);
    }

    /**
     * Stops the TTS engine and detaches listeners.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        mTextToSpeech.shutdown();
        Log.i("TextToSpeech", "Stopped");

        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTextToSpeechInitialized = true;
            Log.i("TextToSpeech", "Ready");
        }
    }

    /**
     * Called when a new notification is posted. Speaks the notification if Car Mode is active and
     * the user has set {@link #mPrefSpeakNotifications}.
     *
     * @param sbn A data structure encapsulating the original {@link Notification} object as well as
     *            its identifying information (tag and id) and source (package name).
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (mTextToSpeechInitialized
                && mPrefSpeakNotifications
                && (((UiModeManager) getSystemService(UI_MODE_SERVICE)).getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR)
                && (((AudioManager) getSystemService(AUDIO_SERVICE)).getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
                ) {
            Notification notification = sbn.getNotification();
            if (notification.tickerText != null) {
                CharSequence text = notification.tickerText;

                try {
                    PackageManager pm = getPackageManager();
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(sbn.getPackageName(), 0);
                    text = pm.getApplicationLabel(applicationInfo) + ": " + text;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                mTextToSpeech.playSilentUtterance(1500l, TextToSpeech.QUEUE_ADD, sbn.getId() + "_delay");
                mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, mTextToSpeechOptions, sbn.getId() + "_content");
                Log.d("TextToSpeech", "Speak: " + text);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS)) {
            mPrefSpeakNotifications = sharedPreferences.getBoolean(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS, true);
            Log.i("TextToSpeech", "Spoken notifications: " + mPrefSpeakNotifications);
        } else if (key.equals(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS_VOLUME)) {
            mPrefSpeakNotificationsVolume = sharedPreferences.getFloat(Preferences.PREF_KEY_SPEAK_NOTIFICATIONS_VOLUME, 0.5f);
            mTextToSpeechOptions.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mPrefSpeakNotificationsVolume);
        }
    }

    public boolean isMapsRunning() {
        for (StatusBarNotification notification : getActiveNotifications()) {
            if (notification.getPackageName().equals("com.google.android.apps.maps")) {
                return true;
            }
        }

        return false;
    }
}