package org.ethack.torrific;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

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

    @Override
    protected boolean isValidFragment(String fragmentName) {
        String prepend = "org.ethack.torrific.PreferencesActivity$";
        String[] fragments = {
                prepend + "OtherPrefs",
                prepend + "NetworkPrefs",
        };

        return Arrays.asList(fragments).contains(fragmentName);
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
}
