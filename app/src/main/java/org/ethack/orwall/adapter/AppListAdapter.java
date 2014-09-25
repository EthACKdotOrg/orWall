package org.ethack.orwall.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
 *
 * Enabled application will get the following information/characteristics:
 * - name of proxy being used (default: Tor/Orbot)
 * - if there's a pass-through, it will be shown in here
 *
 * A long press on the activated app will show up a dedicated dialog providing:
 * - choice for another proxy app (if i2p is installed for example)
 * - choice between "forcing" or "native" connection to the proxy
 * - native will create a fenced way: the app may not connect to anything else than the proxy port
 * - forcing will apply the -j REDIRECT we already use now
 * - choice to allow the app to go to the Net without using any proxy (with a timer)
 *
 * Once we touch either a disabled or enabled app, it should go to the opposite list, and rules should
 * be removed or added. This still has to be done.
 */
public class AppListAdapter extends ArrayAdapter {

    private final static String TAG = "AppListAdapter";
    private final Context context;
    private final Object[] apps;
    private final PackageManager packageManager;
    private final NatRules natRules;

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
        } catch (ArrayIndexOutOfBoundsException e) {}

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
                CharSequence label = packageManager.getApplicationLabel(applicationInfo);

                if (appRule.getOnionType().equals("None")) {
                    holder.checkBox.setText(label);
                } else {
                    Log.d(TAG, "Treating as ENABLED: " + label);
                    holder.checkBox.setText(label + " (via " + appRule.getOnionType() + ")");
                    holder.checkBox.setChecked(true);
                    holder.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            showAdvanced(view);
                            return true;
                        }
                    });
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


            // TODO: find a way to force a complet refresh of the tab

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
        String appName = view.getTag(R.id.id_appTag).toString();
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.packageManager.getPackageInfo(appName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
        }

        if (packageInfo != null) {

            // get current application rule
            AppRule appRule = this.natRules.getAppRule((long) packageInfo.applicationInfo.uid);

            // Add Proxy providers if available
            // Is orbot installed ?
            OrbotHelper orbotHelper = new OrbotHelper(this.context);
            if (orbotHelper.isOrbotInstalled()) {
                RadioButton radioTor = new RadioButton(this.context);
                radioTor.setText("Tor");

                if (appRule.getOnionType().equals(Constants.DB_ONION_TYPE_TOR)) {
                    radioTor.setChecked(true);
                }

                ((ViewGroup) l_view.findViewById(R.id.radio_connection_provider)).addView(radioTor);
            }
            // is i2p present? No helper for that now
            PackageInfo i2p = null;
            try {
                i2p = this.packageManager.getPackageInfo(Constants.I2P_APP_NAME, PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
            }

            if (i2p != null) {
                RadioButton radioI2p = new RadioButton(this.context);
                radioI2p.setText("i2p");

                if (appRule.getOnionType().equals(Constants.DB_ONION_TYPE_I2P)) {
                    radioI2p.setChecked(true);
                }
                ((ViewGroup) l_view.findViewById(R.id.radio_connection_provider)).addView(radioI2p);
            }

            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            // Will save state and apply rules
            alert.setPositiveButton(R.string.alert_accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            // Will do nothing
            alert.setNegativeButton(R.string.main_dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            // Display alert
            alert.setTitle("Advanced settings")
                    .setView(l_view)
                    .show();
        }
    }
}
