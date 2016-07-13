package org.ethack.orwall.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import org.ethack.orwall.R;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.ethack.orwall.lib.PackageInfoData;
import org.sufficientlysecure.rootcommands.util.Log;

import java.util.List;
import java.util.Map;

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
    private final List<AppRule> apps;
    private final PackageManager packageManager;
    private final NatRules natRules;
    private final SharedPreferences sharedPreferences;
    private CheckBox checkboxInternet;
    private RadioButton radioTor;
    private CheckBox checkLocalHost;
    private CheckBox checkLocalNetwork;
    //private RadioButton radioI2p;
    private RadioButton radioBypass;
    private Map<String, PackageInfoData> specialApps;

    /**
     * Constructor.
     *
     * @param context - application context
     * @param pkgs    - list of all installed packages
     */
    public AppListAdapter(Context context, List<AppRule> pkgs) {
        super(context, R.layout.app_row, pkgs);
        this.context = context;
        this.apps = pkgs;
        this.packageManager = context.getPackageManager();
        this.natRules = new NatRules(context);
        this.sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        this.specialApps = PackageInfoData.specialApps();
    }

    private Boolean isOrWallEnabled(){
        return sharedPreferences.getBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true);
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
        holder.checkBox.setTag(position);

        AppRule appRule = this.apps.get(position);

        ApplicationInfo applicationInfo = null;
        PackageInfoData packageInfoData = null;
        Drawable appIcon = null;

        if (appRule.getPkgName().startsWith(Constants.SPECIAL_APPS_PREFIX)) {
            packageInfoData = specialApps.get(appRule.getPkgName());
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_unknown_app);
            appIcon = new BitmapDrawable(context.getResources(), b);
        } else {
            PackageManager packageManager = this.context.getPackageManager();
            try {
                applicationInfo = packageManager.getApplicationInfo(appRule.getPkgName(), PackageManager.GET_PERMISSIONS);
                appIcon = packageManager.getApplicationIcon(applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Application not found: " + appRule.getPkgName());
            }
        }

        if (applicationInfo != null || packageInfoData != null) {

            appIcon.setBounds(0, 0, 40, 40);

            holder.checkBox.setCompoundDrawables(appIcon, null, null, null);
            holder.checkBox.setTag(R.id.id_appTag, appRule);

            if (appRule.getLabel() == null) {

                if (appRule.getAppName()==null){
                    String appName;

                    if (packageInfoData != null) {
                        appName = (specialApps.get(appRule.getPkgName())).getName();
                    } else {
                        appName = (String) packageManager.getApplicationLabel(applicationInfo);
                    }
                    appRule.setAppName(appName);
                }
                if (appRule.isStored()) {
                    String label = appRule.getDisplay();
                    holder.checkBox.setText(label);
                    appRule.setLabel(label);
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setText(appRule.getAppName());
                    appRule.setLabel(appRule.getAppName());
                    holder.checkBox.setChecked(false);
                }
            } else {
                holder.checkBox.setText(appRule.getLabel());
                holder.checkBox.setChecked(appRule.isStored());
            }

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Click caught!");
                    boolean checked = ((CheckBox) view).isChecked();
                    int getPosition = (Integer) view.getTag();
                    toggleApp(checked, getPosition);
                    AppRule rule = apps.get(getPosition);
                    CheckBox checkBox = (CheckBox) view;
                    checkBox.setChecked(rule.isStored());
                    checkBox.setText(rule.getLabel());
                }
            });
            holder.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showAdvanced(view);
                    return true;
                }
            });

        }
        return convertView;
    }

    /**
     * Function called when we touch an app in the "app" tab
     *
     * @param checked boolean: is checkbox checked?
     * @param position integer: position in apps list
     */
    public void toggleApp(boolean checked, int position) {
        AppRule appRule = apps.get(position);

        if (checked) {
            appRule.setOnionType(Constants.DB_ONION_TYPE_TOR);
            appRule.setLocalHost(false);
            appRule.setLocalNetwork(false);
            boolean success = this.natRules.addAppToRules(appRule);
            if (success) {
                Toast.makeText(context, context.getString(R.string.toast_new_rule), Toast.LENGTH_SHORT).show();
                appRule.setStored(true);
                appRule.setLabel(appRule.getDisplay());
                if (isOrWallEnabled())
                    appRule.install(context);
            } else {
                appRule.setOnionType(Constants.DB_ONION_TYPE_NONE);
                Toast.makeText(context,
                        String.format(context.getString(R.string.toast_error), 1),
                        Toast.LENGTH_SHORT
                ).show();
            }
        } else {
            String oldType = appRule.getOnionType();
            Boolean oldLocalhost = appRule.getLocalHost();
            Boolean oldLocalNetwork = appRule.getLocalNetwork();
            boolean success = this.natRules.removeAppFromRules(appRule.getAppUID());
            if (success) {
                if (isOrWallEnabled())
                    appRule.uninstall(context);
                appRule.setStored(false);
                appRule.setOnionType(Constants.DB_ONION_TYPE_NONE);
                appRule.setLocalHost(false);
                appRule.setLocalNetwork(false);
                appRule.setLabel(appRule.getAppName());
                Toast.makeText(context, context.getString(R.string.toast_remove_rule), Toast.LENGTH_SHORT).show();
            } else {
                appRule.setOnionType(oldType);
                appRule.setLocalHost(oldLocalhost);
                appRule.setLocalNetwork(oldLocalNetwork);
                Toast.makeText(context,
                        String.format(context.getString(R.string.toast_error), 2),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    public void showAdvanced(final View view) {
        LayoutInflater li = LayoutInflater.from(this.context);
        View l_view = li.inflate(R.layout.advanced_connection, null);

        // Get application information
        final AppRule appRule = (AppRule) view.getTag(R.id.id_appTag);

        // Add Proxy providers if available
        // Is orbot installed ?
        OrbotHelper orbotHelper = new OrbotHelper(this.context);

        this.checkboxInternet = (CheckBox) l_view.findViewById(R.id.id_check_internet);
        if (appRule.getOnionType() != null && !appRule.getOnionType().equals(Constants.DB_ONION_TYPE_NONE)) {
            this.checkboxInternet.setChecked(true);
        }
        this.checkboxInternet.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioBypass.setEnabled(checkboxInternet.isChecked());
                        OrbotHelper orbotHelper = new OrbotHelper(context);
                        radioTor.setEnabled(checkboxInternet.isChecked() && orbotHelper.isOrbotInstalled());

                        if (!radioBypass.isChecked() && !radioTor.isChecked()) {
                            if (radioTor.isEnabled()) {
                                radioTor.setChecked(true);
                            } else {
                                radioBypass.setChecked(true);
                            }
                        }
                    }
                }
        );

        this.radioBypass = (RadioButton) l_view.findViewById(R.id.id_radio_bypass);
        if (appRule.getOnionType() != null && appRule.getOnionType().equals(Constants.DB_ONION_TYPE_BYPASS)) {
            this.radioBypass.setChecked(true);
        }
        this.radioBypass.setEnabled(this.checkboxInternet.isChecked());

        this.radioTor = (RadioButton) l_view.findViewById(R.id.id_radio_tor);
        if (!orbotHelper.isOrbotInstalled()) {
            radioTor.setEnabled(false);
        } else {
            if (appRule.getOnionType() != null && appRule.getOnionType().equals(Constants.DB_ONION_TYPE_TOR)) {
                radioTor.setChecked(true);
            }
            this.radioTor.setEnabled(this.checkboxInternet.isChecked());
        }
/*
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
*/
        this.checkLocalHost = (CheckBox) l_view.findViewById(R.id.id_check_localhost);
        this.checkLocalHost.setChecked(appRule.getLocalHost());

        this.checkLocalNetwork = (CheckBox) l_view.findViewById(R.id.id_check_localnetwork);
        this.checkLocalNetwork.setChecked(appRule.getLocalNetwork());

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // Will save state and apply rules
        alert.setPositiveButton(R.string.alert_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveAdvanced(appRule, view);
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
                appRule.getAppName()
                )
        ).setView(l_view).show();

    }

    private void saveAdvanced(AppRule appRule, View view) {

        // Update DB content
        AppRule updated = new AppRule();
        updated.setAppName(appRule.getAppName());
        updated.setPkgName(appRule.getPkgName());
        updated.setAppUID(appRule.getAppUID());
        final int position = (Integer) view.getTag();

        // none
        if (!checkboxInternet.isChecked()) {
            updated.setOnionType(Constants.DB_ONION_TYPE_NONE);
        } else
        // Anonymity provider
        if (radioTor.isChecked()) {
            updated.setOnionType(Constants.DB_ONION_TYPE_TOR);
        } else
        if (radioBypass.isChecked()) {
            updated.setOnionType(Constants.DB_ONION_TYPE_BYPASS);
        }
/*
        else
        if (radioI2p.isChecked()) {
            updated.setOnionType(Constants.DB_ONION_TYPE_I2P);
        }
*/
        updated.setLocalHost(this.checkLocalHost.isChecked());
        updated.setLocalNetwork(this.checkLocalNetwork.isChecked());

        boolean done = false;
        boolean error = false;

        // CREATE
        if (!appRule.isStored() && !updated.isEmpty()){
            done = natRules.addAppToRules(updated);
            if (done){
                updated.install(this.context);
                Toast.makeText(context, context.getString(R.string.toast_new_rule), Toast.LENGTH_SHORT).show();
            }
        } else
        // UPDATE
        if (appRule.isStored() && !updated.isEmpty()){
            done = natRules.update(updated);
            if (done){
                appRule.uninstall(this.context);
                updated.install(this.context);
                Toast.makeText(context, context.getString(R.string.toast_update_rule), Toast.LENGTH_SHORT).show();
            }
        } else
        //DELETE
        if (appRule.isStored() && updated.isEmpty()){
            done = natRules.removeAppFromRules(updated.getAppUID());
            if (done) {
                appRule.uninstall(this.context);
                Toast.makeText(context, context.getString(R.string.toast_remove_rule), Toast.LENGTH_SHORT).show();
            }
        } else {
            // nothing to do
            return;
        }

        if (done){
            appRule.setOnionType(updated.getOnionType());
            appRule.setLocalHost(updated.getLocalHost());
            appRule.setLocalNetwork(updated.getLocalNetwork());

            if (appRule.isEmpty()){
                appRule.setStored(false);
                appRule.setLabel(appRule.getAppName());
            } else {
                appRule.setStored(true);
                appRule.setLabel(appRule.getDisplay());
            }
            CheckBox checkBox = (CheckBox) view;
            checkBox.setText(appRule.getLabel());
            checkBox.setChecked(appRule.isStored());
        } else {
            // error updating database
            appRule.setOnionType(Constants.DB_ONION_TYPE_NONE);
            Toast.makeText(context,
                    String.format(context.getString(R.string.toast_error), 3),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Simple holder — allows a faster view
     */
    static class ViewHolder {
        protected CheckBox checkBox;
    }
}
