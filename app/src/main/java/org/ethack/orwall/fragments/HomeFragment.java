package org.ethack.orwall.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.R;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;

import java.util.concurrent.TimeUnit;

/**
 * Manage "home" tab fragment.
 *
 * @link org.ethack.orwall.TabbedMain
 */
public class HomeFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private CountDownTimer timer;
    private Long browser_uid;
    private Long sip_uid;
    private InitializeIptables initializeIptables;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tabbed_home, container, false);

        this.sharedPreferences = this.getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        this.initializeIptables  = new InitializeIptables(getActivity());

        Switch orwallStatus = (Switch) view.findViewById(R.id.orwall_status);
        Switch browserStatus = (Switch) view.findViewById(R.id.browser_status);
        Switch sipStatus = (Switch) view.findViewById(R.id.sip_status);
        Switch lanStatus = (Switch) view.findViewById(R.id.lan_status);

        orwallStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true));
        // orWall might be deactivated. Let's test it!
        orwallStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleOrwall(view);
            }
        });

        // We want to ensure we can access this setting if and only if there's a selected browser.
        this.browser_uid = Long.valueOf(sharedPreferences.getString(Constants.PREF_KEY_SPEC_BROWSER, "0"));
        if (this.browser_uid != 0) {
            browserStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false));
            browserStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleBrowser(view);
                }
            });
        } else {
            // No selected browser, meaning we want to deactivate this option.
            browserStatus.setClickable(false);
            browserStatus.setTextColor(Color.GRAY);
        }

        // We want to ensure we can access this setting if and only if there's a selected SIP app
        this.sip_uid = Long.valueOf(sharedPreferences.getString(Constants.PREF_KEY_SIP_APP, "0"));
        if (this.sip_uid != 0) {
            sipStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_SIP_ENABLED, false));
            sipStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = ((Switch) view).isChecked();
                    initializeIptables.manageSip(checked, sip_uid);
                    sharedPreferences.edit().putBoolean(Constants.PREF_KEY_SIP_ENABLED, checked).apply();
                }
            });
        } else {
            // No selected SIP app, meaning we want to deactivate this option.
            sipStatus.setClickable(false);
            sipStatus.setTextColor(Color.GRAY);
        }

        lanStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_LAN_ENABLED, false));
        lanStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((Switch) view).isChecked();
                initializeIptables.LANPolicy(checked);
                sharedPreferences.edit().putBoolean(Constants.PREF_KEY_LAN_ENABLED, checked).apply();
            }
        });

        return view;
    }

    /**
     * Activate or deactivate Browser bypass
     *
     * @param view View passed by onClick
     */
    public void toggleBrowser(final View view) {
        boolean checked = ((Switch) view).isChecked();

        initializeIptables.manageCaptiveBrowser(checked, this.browser_uid);

        if (checked) {
            long gracetime = Long.valueOf(sharedPreferences.getString(Constants.PREF_KEY_BROWSER_GRACETIME, String.valueOf(Constants.BROWSER_GRACETIME)));

            this.timer = new CountDownTimer(TimeUnit.MINUTES.toMillis(gracetime), TimeUnit.SECONDS.toMillis(30)) {
                @Override
                public void onTick(long untilFinished) {
                    final long minutes = TimeUnit.MILLISECONDS.toMinutes(untilFinished);
                    final long seconds = TimeUnit.MILLISECONDS.toSeconds(untilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(untilFinished));
                    CharSequence text = String.format(getResources().getString(R.string.main_counter), minutes, seconds);
                    Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                }


                @Override
                public void onFinish() {
                    initializeIptables.manageCaptiveBrowser(false, browser_uid);
                    sharedPreferences.edit().putBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false).apply();
                    CharSequence text = getResources().getString(R.string.main_end_of_browser);
                    Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                }
            }.start();
        } else {
            this.timer.cancel();
        }
    }

    /**
     * Toggle orWall status
     *
     * @param view View passed by onClick
     */
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
