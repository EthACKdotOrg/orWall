package org.ethack.orwall.adapter;

import android.content.Context;
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
import android.widget.TextView;

import org.ethack.orwall.R;
import org.ethack.orwall.lib.AppRule;
import org.sufficientlysecure.rootcommands.util.Log;


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

    /**
     * Constructor.
     *
     * @param context - application context
     * @param pkgs - list of all installed packages
     */
    public AppListAdapter(Context context, List<AppRule> pkgs) {
        super(context, R.layout.rowlayout, context.getPackageManager().getInstalledApplications(PackageManager.GET_PERMISSIONS));
        this.context = context;
        this.apps = pkgs.toArray();

        Log.d(TAG, "Pkgs: "+ this.apps.length);
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
            holder.textView = (TextView) convertView.findViewById(R.id.id_application);
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

                holder.textView.setCompoundDrawables(appIcon, null, null, null);
                if (appRule.getOnionType().equals("None")) {
                    holder.textView.setText(packageManager.getApplicationLabel(applicationInfo));
                    holder.textView.setTextColor(Color.GRAY);
                } else {
                    holder.textView.setTextColor(Color.WHITE);
                    holder.textView.setText(appRule.getAppName() + "(" + appRule.getOnionType() + ")");
                }
            }
        }
        return convertView;
    }

    static class ViewHolder {
        private TextView textView;
    }
}
