package org.ethack.orwall.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Switch;
import android.widget.Toast;

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.R;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;

/**
 * Manage "home" tab fragment.
 * @link org.ethack.orwall.TabbedMain
 */
public class HomeFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tabbed_home, container, false);

        this.sharedPreferences = this.getActivity().getSharedPreferences(Constants.PREFERENCES, this.getActivity().MODE_PRIVATE);

        Switch orwallStatus = (Switch)view.findViewById(R.id.orwall_status);
        Switch browserStatus = (Switch)view.findViewById(R.id.browser_status);
        Switch sipStatus = (Switch)view.findViewById(R.id.sip_status);
        Switch lanStatus = (Switch)view.findViewById(R.id.lan_status);

        orwallStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true));
        orwallStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleOrwall(view);
            }
        });
        browserStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false));
        sipStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_SIP_ENABLED, false));
        lanStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_LAN_ENABLED, false));

        return view;
    }

    public void toggleOrwall(final View view) {
        boolean checked = ((Switch) view).isChecked();

        final Intent bgpProcess = new Intent(this.getActivity(), BackgroundProcess.class);

        if (checked) {
            bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_ENABLE_ORWALL);
            getActivity().startService(bgpProcess);
            sharedPreferences.edit().putBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true).apply();
            Toast.makeText(this.getActivity(), getString(R.string.enabling_orwall), Toast.LENGTH_LONG).show();

        } else {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

            alertDialog.setTitle(getString(R.string.disable_orwall_title));
            alertDialog.setMessage(getString(R.string.disable_orwall_msg));
            alertDialog.setPositiveButton(getString(R.string.alert_accept), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_DISABLE_ORWALL);
                    getActivity().startService(bgpProcess);
                    sharedPreferences.edit().putBoolean(Constants.PREF_KEY_ORWALL_ENABLED, false).apply();
                }
            });

            alertDialog.setNegativeButton(getString(R.string.main_dismiss), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Switch) view).setChecked(true);
                }
            });
            alertDialog.show();
        }
    }
}
