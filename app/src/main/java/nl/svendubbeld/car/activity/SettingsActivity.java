package nl.svendubbeld.car.activity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import nl.svendubbeld.car.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        // Load SettingsFragment
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            fragmentTransaction.replace(R.id.container, fragment).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        /**
         * Adds all preferences.
         *
         * @param savedInstanceState If the activity is being re-initialized after previously being
         *                           shut down then this Bundle contains the data it most recently
         *                           supplied in {@link #onSaveInstanceState(Bundle)}.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            findPreference("pref_key_licenses").setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.pref_title_licenses)
                        // TODO .setView(R.layout.dialog_licenses)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();

                return true;
            });
            findPreference("pref_key_version").setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SvenDub/Dashboard/releases/tag/" + getString(R.string.app_version_name)));
                startActivity(intent);
                return true;
            });
        }
    }
}
