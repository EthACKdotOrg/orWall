package org.ethack.orwall.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.PreferencesActivity;
import org.ethack.orwall.R;
import org.ethack.orwall.TabbedMain;
import org.ethack.orwall.WizardActivity;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.InstallScripts;
import org.ethack.orwall.lib.Iptables;
import org.ethack.orwall.lib.Preferences;
import org.ethack.orwall.lib.Util;
import org.sufficientlysecure.rootcommands.RootCommands;

import java.util.concurrent.TimeUnit;

/**
 * Manage "home" tab fragment.
 *
 * @link org.ethack.orwall.TabbedMain
 */
public class HomeFragment extends Fragment {

    private CountDownTimer timer;
    private Long browser_uid;
    private Long sip_uid;
    private Iptables iptables;
    private View home;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        home = inflater.inflate(R.layout.fragment_tabbed_home, container, false);

        iptables = new Iptables(getActivity());
        boolean initSupported = Iptables.initSupported();

        Switch orwallStatus = (Switch) home.findViewById(R.id.orwall_status);

        // Status switches — most of them are read-only, as they just displays devices capabilities.
        Switch status_initscript = (Switch) home.findViewById(R.id.status_initscript);
        Switch status_root = (Switch) home.findViewById(R.id.status_root);
        Switch status_iptables = (Switch) home.findViewById(R.id.status_iptables);
        Switch status_ipt_comments = (Switch) home.findViewById(R.id.status_ipt_comments);
        Switch status_orbot = (Switch) home.findViewById(R.id.status_orbot);

        // Buttons
        Button settings = (Button) home.findViewById(R.id.id_settings);
        Button about = (Button) home.findViewById(R.id.id_about);
        Button wizard = (Button) home.findViewById(R.id.id_wizard);

        // Display a big fat warning if IPTables wasn't initialized properly
        // This warning should be shown only if we aren't expected this situation
        // If we know there is no init-script support, then don't show it.
        if (initSupported && !iptables.isInitialized()) {
            home.findViewById(R.id.warn_init).setVisibility(View.VISIBLE);
        }


        orwallStatus.setChecked(Preferences.isOrwallEnabled(getActivity()));
        // orWall might be deactivated. Let's test it!
        orwallStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleOrwall(view);
            }
        });

        // Set status switches in order to show the user what's working. Or not working.

        // Init script: try to install it and so on
        InstallScripts installScripts = new InstallScripts(getActivity());
        installScripts.run();
        boolean enforceInit = Preferences.isEnforceInitScript(getActivity());
        status_initscript.setChecked( (enforceInit && initSupported) );
        status_initscript.setEnabled(initSupported);
        // If init script cannot be enabled, display why
        if (!initSupported) {
            TextView explain = (TextView) home.findViewById(R.id.status_initscript_description);
            explain.setText(
                    String.format(
                            getString(R.string.explain_no_initscript),
                            Iptables.DST_FILE
                    )
            );
            explain.setVisibility(View.VISIBLE);
        }
        // add a listener to this switch
        status_initscript.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean checked = compoundButton.isChecked();
                if (checked) {
                    Iptables.installInitScript(getActivity());
                } else {
                    Iptables.removeIniScript(getActivity());
                }
            }
        });
        // Do we have root access ?
        if (RootCommands.rootAccessGiven()) {
            status_root.setChecked(true);
            home.findViewById(R.id.warn_root).setVisibility(View.GONE);
        } else {
            status_root.setChecked(false);
            home.findViewById(R.id.warn_root).setVisibility(View.VISIBLE);
        }
        // Hopefully there IS iptables on this device…
        if (Iptables.iptablesExists()) {
            status_iptables.setChecked(true);
            home.findViewById(R.id.warn_iptables).setVisibility(View.GONE);
            home.findViewById(R.id.status_iptables_description).setVisibility(View.GONE);
        } else {
            status_iptables.setChecked(false);
            home.findViewById(R.id.warn_iptables).setVisibility(View.VISIBLE);
            home.findViewById(R.id.status_iptables_description).setVisibility(View.VISIBLE);
        }

        status_ipt_comments.setChecked(iptables.supportComment);
        // Is orbot installed?
        status_orbot.setChecked(Util.isOrbotInstalled(getActivity()));

        // Shows settings
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                startActivity(intent);
            }
        });

        // Shows alert dialog with "about" stuff
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAbout();
            }
        });

        // Start wizard
        wizard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wizard = new Intent(getActivity()
                        , WizardActivity.class);
                startActivity(wizard);
            }
        });

        return home;
    }

    private void updateOptions() {
        Switch browserStatus = (Switch) home.findViewById(R.id.browser_status);
        Switch sipStatus = (Switch) home.findViewById(R.id.sip_status);

        // We want to ensure we can access this setting if and only if there's a selected browser.
        this.browser_uid = Long.valueOf(Preferences.getBrowserApp(getActivity()));
        if (this.browser_uid != 0  && Preferences.isOrwallEnabled(getActivity())) {
            browserStatus.setClickable(true);
            browserStatus.setTextColor(Color.BLACK);

            browserStatus.setChecked(Preferences.isBrowserEnabled(getActivity()));
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
        this.sip_uid = Long.valueOf(Preferences.getSIPApp(getActivity()));
        if (this.sip_uid != 0  && Preferences.isOrwallEnabled(getActivity())) {
            sipStatus.setClickable(false);
            sipStatus.setTextColor(Color.BLACK);

            sipStatus.setChecked(Preferences.isSIPEnabled(getActivity()));
            sipStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = ((Switch) view).isChecked();
                    iptables.manageSip(checked, sip_uid);
                    Preferences.setSIPEnabled(getActivity(), checked);
                }
            });
        } else {
            // No selected SIP app, meaning we want to deactivate this option.
            sipStatus.setClickable(false);
            sipStatus.setTextColor(Color.GRAY);
        }
    }

    /**
     * Activate or deactivate Browser bypass
     *
     * @param view View passed by onClick
     */
    public void toggleBrowser(final View view) {
        boolean checked = ((Switch) view).isChecked();

        iptables.manageCaptiveBrowser(checked, this.browser_uid);

        if (checked) {

            long gracetime = Long.valueOf(Preferences.getBrowserGraceTime(getActivity()));

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
                    iptables.manageCaptiveBrowser(false, browser_uid);
                    Preferences.setBrowserEnabled(getActivity(), false);
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

        Intent intent = new Intent(this.getActivity(), TabbedMain.class);
        final PendingIntent pintent = PendingIntent.getActivity(this.getActivity(), 0, intent, 0);

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(getActivity())
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pintent)
                .setContentTitle(getString(R.string.notification_deactivated_title))
                .setContentText(getString(R.string.notification_deactivated_text))
                .setSmallIcon(R.drawable.v2);

        final NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (checked) {
            bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_ENABLE_ORWALL);
            getActivity().startService(bgpProcess);
            Preferences.setOrwallEnabled(getActivity(), true);
            updateOptions();
            Toast.makeText(this.getActivity(), getString(R.string.enabling_orwall), Toast.LENGTH_LONG).show();
            notificationManager.cancel(1);

        } else {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

            alertDialog.setTitle(getString(R.string.disable_orwall_title));
            alertDialog.setMessage(getString(R.string.disable_orwall_msg));
            alertDialog.setPositiveButton(getString(R.string.alert_accept), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_DISABLE_ORWALL);
                    getActivity().startService(bgpProcess);
                    Preferences.setOrwallEnabled(getActivity(), false);
                    updateOptions();
                    notificationManager.notify(1, notification.build());
                }
            });

            alertDialog.setNegativeButton(getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Switch) view).setChecked(true);
                }
            });
            alertDialog.show();
        }
    }

    public void showAbout() {

        LayoutInflater li = LayoutInflater.from(getActivity());
        View v_about = li.inflate(R.layout.about, null);

        PackageManager packageManager = getActivity().getPackageManager();
        String versionName = "UNKNOWN";
        try {
            versionName = packageManager.getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("About: ", "Unable to get application version name");
        }
        TextView version = (TextView) v_about.findViewById(R.id.about_version);
        version.setText(versionName);
        new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT)
                .setTitle(getString(R.string.button_about))
                .setView(v_about)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        Switch orwallSwitch = (Switch) home.findViewById(R.id.orwall_status);
        // checking true orwall status
        if (Preferences.isOrwallEnabled(getActivity()) &&
                !iptables.haveBooted()){
            Preferences.setOrwallEnabled(getActivity(), false);
            orwallSwitch.setChecked(false);
        }

        updateOptions();
    }
}
