package org.ethack.torrific;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.ethack.torrific.iptables.InitializeIptables;

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
                prepend + "ScriptPrefs",
                prepend + "SpecialApps",
                prepend + "NetworkPrefs",
        };

        return Arrays.asList(fragments).contains(fragmentName);
    }

    public static class ScriptPrefs extends PreferenceFragment {

        private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                InitializeIptables iptables = new InitializeIptables();
                if (sharedPreferences.getBoolean(s, true) && s.equals("enforce_init_script")) {
                    iptables.installInitScript(getActivity());
                }
                if (sharedPreferences.getBoolean(s, true) && s.equals("deactivate_init_script") && !sharedPreferences.getBoolean("enforce_init_script", true)) {
                    iptables.removeIniScript();
                }
            }
        };
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.network_preference, true);
            addPreferencesFromResource(R.xml.fragment_init_pref);
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    public static class SpecialApps extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                InitializeIptables iptables = new InitializeIptables();

            }
        };
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //PreferenceManager.setDefaultValues(getActivity(), R.xml.fragment_apps_prefs, true);
            addPreferencesFromResource(R.xml.fragment_apps_prefs);
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    public static class NetworkPrefs extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                InitializeIptables iptables = new InitializeIptables();
                if (s.equals("enable_lan")) {
                    iptables.LANPolicy(sharedPreferences.getBoolean(s, false));
                }

                if (s.equals("enable_tethering")) {
                    iptables.enableTethering(sharedPreferences.getBoolean(s, false));
                }
                if (s.equals("enable_captive_portal")) {
                    iptables.enableCaptiveDetection(sharedPreferences.getBoolean(s, false), getActivity());
                }
            }
        };
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.other_preferences, true);
            addPreferencesFromResource(R.xml.fragment_network_prefs);
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

}
