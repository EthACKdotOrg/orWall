package org.ethack.orwall.fragments;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import org.ethack.orwall.R;
import org.ethack.orwall.lib.Constants;

/**
 * Manage "home" tab fragment.
 * @link org.ethack.orwall.TabbedMain
 */
public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tabbed_home, container, false);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(Constants.PREFERENCES, this.getActivity().MODE_PRIVATE);

        Switch orwallStatus = (Switch)view.findViewById(R.id.orwall_status);
        Switch browserStatus = (Switch)view.findViewById(R.id.browser_status);
        Switch sipStatus = (Switch)view.findViewById(R.id.sip_status);
        Switch lanStatus = (Switch)view.findViewById(R.id.lan_status);

        orwallStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true));
        browserStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false));
        sipStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_SIP_ENABLED, false));
        lanStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_LAN_ENABLED, false));

        return view;
    }
}
