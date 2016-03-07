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