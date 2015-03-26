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

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;

import nl.svendubbeld.car.R;

/**
 * AccessibilityService for Google Now that listens for specific commands.
 */
public class VoiceAccessibilityService extends AccessibilityService {

    private static final long timeOut = 500;
    private long lastCommand = 0;

    /**
     * Called when a command is entered.
     *
     * @param event An event.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getText().size() > 0) {
            String command = event.getText().get(0).toString();
            if (interpretCommand(command) && (lastCommand + timeOut) < event.getEventTime()) {
                lastCommand = event.getEventTime();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInterrupt() {

    }

    /**
     * Checks a String to see if it matches a command and executes it.
     *
     * @param command The String to check
     * @return true if a command was executed, false otherwise
     */
    private boolean interpretCommand(String command) {
        if (command.toLowerCase().equals(getString(R.string.command_enable_spoken_notifications))) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putBoolean("pref_key_speak_notifications", true).apply();
            return true;
        } else if (command.toLowerCase().equals(getString(R.string.command_disable_spoken_notifications))) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putBoolean("pref_key_speak_notifications", false).apply();
            return true;
        }
        return false;
    }
}
