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

package nl.svendubbeld.car.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A {@link Preference} that allows for string input. <p> It is a subclass of {@link
 * DialogPreference} and shows the {@link EditText} in a dialog. This {@link EditText} can be
 * modified either programmatically via {@link #getEditText()}, or through XML by setting any
 * EditText attributes on the EditTextPreference. </p> <p> This preference will store a string into
 * the SharedPreferences. </p>
 */
public class EditTextPreference extends android.preference.EditTextPreference {

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        setSummary(getSummary());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getSummary() {
        return this.getText();
    }
}