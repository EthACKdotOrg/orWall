package org.ethack.orwall.fragments;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.InstallScripts;
import org.sufficientlysecure.rootcommands.RootCommands;

import java.util.concurrent.TimeUnit;

import info.guardianproject.onionkit.ui.OrbotHelper;

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
        this.initializeIptables = new InitializeIptables(getActivity());
        boolean initSupported = initializeIptables.initSupported();

        Switch orwallStatus = (Switch) view.findViewById(R.id.orwall_status);
        Switch browserStatus = (Switch) view.findViewById(R.id.browser_status);
        Switch sipStatus = (Switch) view.findViewById(R.id.sip_status);
        Switch lanStatus = (Switch) view.findViewById(R.id.lan_status);
        Switch tetherStatus = (Switch) view.findViewById(R.id.tethering_status);

        // Status switches — most of them are read-only, as they just displays devices capabilities.
        Switch status_initscript = (Switch) view.findViewById(R.id.status_initscript);
        Switch status_root = (Switch) view.findViewById(R.id.status_root);
        Switch status_iptables = (Switch) view.findViewById(R.id.status_iptables);
        Switch status_ipt_comments = (Switch) view.findViewById(R.id.status_ipt_comments);
        Switch status_orbot = (Switch) view.findViewById(R.id.status_orbot);

        // Buttons
        Button settings = (Button) view.findViewById(R.id.id_settings);
        Button about = (Button) view.findViewById(R.id.id_about);
        Button wizard = (Button) view.findViewById(R.id.id_wizard);

        // Display a big fat warning if IPTables wasn't initialized properly
        // This warning should be shown only if we aren't expected this situation
        // If we know there is no init-script support, then don't show it.
        if (initSupported && !initializeIptables.isInitialized()) {
            view.findViewById(R.id.warn_init).setVisibility(View.VISIBLE);
        }


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

        tetherStatus.setChecked(sharedPreferences.getBoolean(Constants.PREF_KEY_TETHER_ENABLED, false));
        tetherStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((Switch) view).isChecked();
                Intent bgpProcess = new Intent(getActivity(), BackgroundProcess.class);
                bgpProcess.putExtra(Constants.PARAM_TETHER_STATUS, checked);
                bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_TETHER);
                getActivity().startService(bgpProcess);
                sharedPreferences.edit().putBoolean(Constants.PREF_KEY_TETHER_ENABLED, checked).apply();
            }
        });

        // Set status switches in order to show the user what's working. Or not working.

        // Init script: try to install it and so on
        InstallScripts installScripts = new InstallScripts(getActivity());
        installScripts.run();
        boolean enforceInit = sharedPreferences.getBoolean(Constants.PREF_KEY_ENFOCE_INIT, true);
        status_initscript.setChecked( (enforceInit && initSupported) );
        status_initscript.setEnabled(initSupported);
        // If init script cannot be enabled, display why
        if (!initSupported) {
            TextView explain = (TextView) view.findViewById(R.id.status_initscript_description);
            explain.setText(
                    String.format(
                            getString(R.string.explain_no_initscript),
                            InitializeIptables.dst_file
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
                    initializeIptables.installInitScript();
                } else {
                    initializeIptables.removeIniScript();
                }
            }
        });
        // Do we have root access ?
        if (RootCommands.rootAccessGiven()) {
            status_root.setChecked(true);
            view.findViewById(R.id.warn_root).setVisibility(View.GONE);
        } else {
            status_root.setChecked(false);
            view.findViewById(R.id.warn_root).setVisibility(View.VISIBLE);
        }
        // Hopefully there IS iptables on this device…
        if (initializeIptables.iptablesExists()) {
            status_iptables.setChecked(true);
            view.findViewById(R.id.warn_iptables).setVisibility(View.GONE);
            view.findViewById(R.id.status_iptables_description).setVisibility(View.GONE);
        } else {
            status_iptables.setChecked(false);
            view.findViewById(R.id.warn_iptables).setVisibility(View.VISIBLE);
            view.findViewById(R.id.status_iptables_description).setVisibility(View.VISIBLE);
        }

        // Does current kernel supports comments in iptables?
        initializeIptables.supportComments();
        status_ipt_comments.setChecked(sharedPreferences.getBoolean(Constants.CONFIG_IPT_SUPPORTS_COMMENTS, false));
        // Is orbot installed?
        OrbotHelper orbotHelper = new OrbotHelper(getActivity());
        status_orbot.setChecked(orbotHelper.isOrbotInstalled());

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
            sharedPreferences.edit().putBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true).apply();
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
                    sharedPreferences.edit().putBoolean(Constants.PREF_KEY_ORWALL_ENABLED, false).apply();
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
}
