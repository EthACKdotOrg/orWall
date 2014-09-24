package org.ethack.orwall.adapter;

import android.content.Context;
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

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.R;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.sufficientlysecure.rootcommands.util.Log;


import java.util.ArrayList;
import java.util.List;


/**
 * New adapter class
 * This will create the new application list, for the new tabbed layout.
 * It will provide, in the end, two lists:
 * - enabled application
 * - disabled application
 *
 * Enabled application will get the following information/characteristics:
 * - white color
 * - name of proxy being used (default: Tor/Orbot)
 *  - if there's a pass-through, it will be shown in here
 * - an arrow in order to get a special panel with dedicated options for this app
 * - The dedicate panel will provide:
 *  - choice for another proxy app (if i2p is installed for example)
 *  - choice between "forcing" or "native" connection to the proxy
 *      - native will create a fenced way: the app may not connect to anything else than the proxy port
 *      - forcing will apply the -j REDIRECT we already use now
 *  - choice to allow the app to go to the Net without using any proxy (with a timer)
 *
 * Disabled application will get the following information/characteristics:
 * - grey name (of something showing up "hey, disabled")
 *
 * Once we touch either a disabled or enabled app, it should go to the opposite list, and rules should
 * be removed or added.
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
     * @param pkgs - list of all installed packages
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
     * @param position - position in list
     * @param convertView - conversion view
     * @param parent - parent view group
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
            //Log.e(TAG, "Out of bound: "+ String.valueOf(position));
            //Log.e(TAG, "Array size: "+ String.valueOf(this.apps.length));
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
                CharSequence label = packageManager.getApplicationLabel(applicationInfo);

                if (appRule.getOnionType().equals("None")) {
                    holder.checkBox.setText(label);
                } else {
                    Log.d(TAG, "Treating as ENABLED: "+label);
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
            view.invalidate();
        }
    }
}
