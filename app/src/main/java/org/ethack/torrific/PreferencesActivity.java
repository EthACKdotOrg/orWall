package org.ethack.torrific;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.ethack.torrific.R;

import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.List;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_header, target);
    }

    public static class NetworkPrefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.network_preference, true);
            addPreferencesFromResource(R.xml.fragment_net_pref);
        }
    }

    public static class OtherPrefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.other_preferences, true);
            addPreferencesFromResource(R.xml.fragment_other_prefs);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        String prepend = "org.ethack.torrific.PreferencesActivity$";
        String[] fragments = {
                prepend +"OtherPrefs",
                prepend +"NetworkPrefs",
        };

        return Arrays.asList(fragments).contains(fragmentName);
    }
}
