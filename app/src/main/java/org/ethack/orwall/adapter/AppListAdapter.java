package org.ethack.orwall.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.R;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.sufficientlysecure.rootcommands.util.Log;

import java.util.List;

import info.guardianproject.onionkit.ui.OrbotHelper;


/**
 * New adapter class
 * This will create the new application list, for the new tabbed layout.
 * It will provide, in the end, two lists:
 * - enabled application
 * - disabled application
 * <p/>
 * Enabled application will get the following information/characteristics:
 * - name of proxy being used (default: Tor/Orbot)
 * - if there's a pass-through, it will be shown in here
 * <p/>
 * A long press on the activated app will show up a dedicated dialog providing:
 * - choice for another proxy app (if i2p is installed for example)
 * - choice between "forcing" or "native" connection to the proxy
 * - native will create a fenced way: the app may not connect to anything else than the proxy port
 * - forcing will apply the -j REDIRECT we already use now
 * - choice to allow the app to go to the Net without using any proxy (with a timer)
 * <p/>
 * Once we touch either a disabled or enabled app, it should go to the opposite list, and rules should
 * be removed or added. This still has to be done.
 */
public class AppListAdapter extends ArrayAdapter {

    private final static String TAG = "AppListAdapter";
    private final Context context;
    private final Object[] apps;
    private final PackageManager packageManager;
    private final NatRules natRules;
    private final SharedPreferences sharedPreferences;
    private RadioButton radioTor;
    private RadioButton radioForced;
    private RadioButton radioFenced;
    private RadioButton radioI2p;
    private CheckBox check_bypass;

    /**
     * Constructor.
     *
     * @param context - application context
     * @param pkgs    - list of all installed packages
     */
    public AppListAdapter(Context context, List<AppRule> pkgs) {
        super(context, R.layout.app_row, pkgs);
        this.context = context;
        this.apps = pkgs.toArray();
        this.packageManager = context.getPackageManager();
        this.natRules = new NatRules(context);
        this.sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Creates the view with both lists.
     *
     * @param position    - position in list
     * @param convertView - conversion view
     * @param parent      - parent view group
     * @return - the formatted view
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.app_row, parent, false);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.id_application);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppRule appRule = null;
        try {
            appRule = (AppRule) this.apps[position];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        if (appRule != null) {

            PackageManager packageManager = this.context.getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(appRule.getAppName(), PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Application not found: " + appRule.getAppName());
            }

            if (applicationInfo != null) {

                Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
                appIcon.setBounds(0, 0, 40, 40);

                holder.checkBox.setCompoundDrawables(appIcon, null, null, null);
                holder.checkBox.setTag(R.id.id_appTag, appRule.getAppName());
                holder.checkBox.setTextColor(Color.BLACK);
                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleApp(view);
                    }
                });
                holder.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showAdvanced(view);
                        return true;
                    }
                });
                CharSequence label = packageManager.getApplicationLabel(applicationInfo);

                if (appRule.getOnionType().equals("None")) {
                    holder.checkBox.setText(label);
                    holder.checkBox.setChecked(false);
                } else {
                    Log.d(TAG, "Treating as ENABLED: " + label);
                    holder.checkBox.setText(label + " (via " + appRule.getOnionType() + ")");
                    holder.checkBox.setChecked(true);
                }

            }
        }
        return convertView;
    }

    /**
     * Simple holder — allows a faster view
     */
    static class ViewHolder {
        private CheckBox checkBox;
    }

    /**
     * Function called when we touch an app in the "app" tab
     *
     * @param view - View object transmitted via onClick argument in app_row.xml
     */
    public void toggleApp(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        String appName = view.getTag(R.id.id_appTag).toString();

        PackageInfo apk;
        try {
            apk = packageManager.getPackageInfo(appName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            android.util.Log.e(TAG, "Application " + appName + " not found");
            apk = null;
        }
        if (apk != null) {

            long appUID = apk.applicationInfo.uid;

            Intent bgpProcess = new Intent(context, BackgroundProcess.class);
            bgpProcess.putExtra(Constants.PARAM_APPNAME, appName);
            bgpProcess.putExtra(Constants.PARAM_APPUID, appUID);


            // TODO: find a way to force a complete refresh of the tab

            if (checked) {
                bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_ADD_RULE);
                this.natRules.addAppToRules(
                        appUID, appName,
                        Constants.DB_ONION_TYPE_TOR,
                        Constants.ORBOT_TRANSPROXY,
                        Constants.DB_PORT_TYPE_TRANS
                );
            } else {
                bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_RM_RULE);
                this.natRules.removeAppFromRules(appUID);
            }
            context.startService(bgpProcess);

        }
    }

    public void showAdvanced(View view) {
        LayoutInflater li = LayoutInflater.from(this.context);
        View l_view = li.inflate(R.layout.advanced_connection, null);

        // Get application information
        final String appName = view.getTag(R.id.id_appTag).toString();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = this.packageManager.getApplicationInfo(appName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
        }

        if (applicationInfo != null) {

            // get current application rule
            final AppRule appRule = this.natRules.getAppRule((long) applicationInfo.uid);

            // is it a known app? If not, populate some information about it anyway.
            if (appRule.getAppName() == null) {
                appRule.setAppUID((long) applicationInfo.uid);
                appRule.setAppName(applicationInfo.packageName);
            }

            // Add Proxy providers if available
            // Is orbot installed ?
            OrbotHelper orbotHelper = new OrbotHelper(this.context);

            this.radioTor = new RadioButton(this.context);
            if (orbotHelper.isOrbotInstalled()) {
                radioTor.setText("Tor");

                // TODO: handle this a bit better once we get a better support for i2p
                if (appRule.getOnionType() == null || appRule.getOnionType().equals(Constants.DB_ONION_TYPE_TOR)) {
                    radioTor.setChecked(true);
                }

                ((ViewGroup) l_view.findViewById(R.id.radio_connection_providers)).addView(radioTor);
            }
            // is i2p present? No helper for that now
            PackageInfo i2p = null;
            try {
                i2p = this.packageManager.getPackageInfo(Constants.I2P_APP_NAME, PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
            }

            this.radioI2p = new RadioButton(this.context);
            if (i2p != null) {
                radioI2p.setText("i2p");
                // For now we do not have support for i2p. Just teasing ;)
                radioI2p.setEnabled(false);

                if (appRule.getOnionType() != null && appRule.getOnionType().equals(Constants.DB_ONION_TYPE_I2P)) {
                    radioI2p.setChecked(true);
                }
                ((ViewGroup) l_view.findViewById(R.id.radio_connection_providers)).addView(radioI2p);
            }

            this.radioForced = (RadioButton) l_view.findViewById(R.id.id_radio_force);
            this.radioFenced = (RadioButton) l_view.findViewById(R.id.id_radio_fenced);
            // Is it a Fenced or a Forced app?
            if (appRule.getPortType() != null && appRule.getPortType().equals(Constants.DB_PORT_TYPE_FENCED)) {
                radioFenced.setChecked(true);
            } else {
                radioForced.setChecked(true);
            }

            // Maybe it's a Bypass, if so, we will need to deactivate all other Radios…
            this.check_bypass = (CheckBox) l_view.findViewById(R.id.id_checkbox_bypass);
            if (appRule.getOnionType() != null && appRule.getOnionType().equals(Constants.DB_ONION_TYPE_BYPASS)) {
                toggleAdvancedPrefs(true);
            }

            // just set an onClick action for the checkBox, as we don't have access to the dependencies
            check_bypass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = ((CheckBox) view.findViewById(R.id.id_checkbox_bypass)).isChecked();
                    toggleAdvancedPrefs(checked);
                }
            });

            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            // Will save state and apply rules
            alert.setPositiveButton(R.string.alert_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    saveAdvanced(appRule);
                }
            });

            // Will do nothing
            alert.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            // Display alert
            alert.setTitle(String.format(
                            this.context.getString(R.string.advanced_connection_settings_title),
                            this.packageManager.getApplicationLabel(applicationInfo))
            ).setView(l_view).show();
        }
    }

    public void toggleAdvancedPrefs(boolean status) {
        radioFenced.setEnabled(!status);
        radioForced.setEnabled(!status);
        radioI2p.setEnabled(!status);
        radioTor.setEnabled(!status);
        check_bypass.setChecked(status);
    }

    private void saveAdvanced(AppRule appRule) {

        // Update DB content
        AppRule updated = new AppRule();
        updated.setAppName(appRule.getAppName());
        updated.setAppUID(appRule.getAppUID());

        if (check_bypass.isChecked()) {
            updated.setOnionType(Constants.DB_ONION_TYPE_BYPASS);
            updated.setPortType(Constants.DB_ONION_TYPE_BYPASS);
        } else {
            // Anonymity provider
            if (radioTor.isChecked()) {
                updated.setOnionType(Constants.DB_ONION_TYPE_TOR);
            }
            if (radioI2p.isChecked()) {
                updated.setOnionType(Constants.DB_ONION_TYPE_I2P);
            }
            // We might fallback to the default type.
            if (updated.getOnionType() == null || updated.getOnionType().isEmpty()) {
                updated.setOnionType(Constants.DB_ONION_TYPE_TOR);
            }
        }
        // Anonymity connectivity
        if (radioFenced.isChecked()) {
            updated.setPortType(Constants.DB_PORT_TYPE_FENCED);
        }
        if (radioForced.isChecked()) {
            updated.setPortType(Constants.DB_PORT_TYPE_TRANS);
        }

        // We might fallback to the default type: forced
        if (updated.getPortType() == null || updated.getPortType().isEmpty()) {
            updated.setPortType(Constants.DB_PORT_TYPE_TRANS);
        }

        // By the way, is it a new object? If so, we're wanting to create it instead of update it!
        boolean db_status;
        if (appRule.getOnionType() == null) {
            db_status = natRules.addAppToRules(updated);
        } else {
            db_status = natRules.update(updated);
        }

        // If and ONLY IF the DB update/creation is OK, we will push the new rules
        if (db_status) {

            // We want to use a background process in order to free the main thread and associated
            Intent bgCleanup = new Intent(this.context, BackgroundProcess.class);
            bgCleanup.putExtra(Constants.PARAM_APPUID, appRule.getAppUID());
            bgCleanup.putExtra(Constants.PARAM_APPNAME, appRule.getAppName());

            // first: clean existing rules IF app already exists
            if (appRule.getOnionType() != null) {
                IptRules iptRules = new IptRules(this.sharedPreferences.getBoolean(Constants.CONFIG_IPT_SUPPORTS_COMMENTS, false));
                if (appRule.getPortType().equals(Constants.DB_PORT_TYPE_FENCED)) {
                    bgCleanup.putExtra(Constants.ACTION, Constants.ACTION_RM_FENCED);

                } else if (appRule.getOnionType().equals(Constants.DB_ONION_TYPE_BYPASS)) {
                    bgCleanup.putExtra(Constants.ACTION, Constants.ACTION_RM_BYPASS);

                } else {
                    bgCleanup.putExtra(Constants.ACTION, Constants.ACTION_RM_RULE);
                }
                this.context.startService(bgCleanup);
            }

            // Now we can add the new rules
            Intent bgNewRules = new Intent(this.context, BackgroundProcess.class);
            bgNewRules.putExtra(Constants.PARAM_APPUID, appRule.getAppUID());
            bgNewRules.putExtra(Constants.PARAM_APPNAME, appRule.getAppName());

            if (updated.getPortType().equals(Constants.DB_PORT_TYPE_FENCED)) {
                bgNewRules.putExtra(Constants.ACTION, Constants.ACTION_ADD_FENCED);

            } else if (updated.getOnionType().equals(Constants.DB_ONION_TYPE_BYPASS)) {
                bgNewRules.putExtra(Constants.ACTION, Constants.ACTION_ADD_BYPASS);

            } else {
                bgNewRules.putExtra(Constants.ACTION, Constants.ACTION_ADD_RULE);
            }
            this.context.startService(bgNewRules);
        } else {
            Log.e(TAG, "ERROR while updating object in DB!");
        }

    }
}
