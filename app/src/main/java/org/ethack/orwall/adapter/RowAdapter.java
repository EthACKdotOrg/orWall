package org.ethack.orwall.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.R;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;

import java.util.List;


/**
 * New adapter in order to create the grid for applications
 */
public class RowAdapter extends ArrayAdapter<PackageInfo> {
    private final Context context;
    private final Object[] pkgs;
    private final PackageManager packageManager;
    private NatRules natRules;

    /**
     * Class builder
     *  @param context
     * @param pkgs
     * @param packageManager
     */
    public RowAdapter(Context context, List<PackageInfo> pkgs, PackageManager packageManager) {
        super(context, R.layout.rowlayout, pkgs);
        this.context = context;
        this.pkgs = pkgs.toArray();
        this.packageManager = packageManager;
        this.natRules = new NatRules(context);
    }

    /**
     * Override getView
     *
     * @param position
     * @param convertView
     * @param parent
     * @return View
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        PackageInfo pkg = (PackageInfo) pkgs[position];

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.rowlayout, parent, false);
            holder = new ViewHolder();
            holder.check_box = (CheckBox) convertView.findViewById(R.id.appCheckbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String label = packageManager.getApplicationLabel(pkg.applicationInfo).toString();
        Drawable appIcon = packageManager.getApplicationIcon(pkg.applicationInfo);
        appIcon.setBounds(0, 0, 40, 40);

        holder.check_box.setText(label);
        if (isSystemPackage(pkg)) {
            holder.check_box.setTextColor(Color.rgb(135, 206, 250));
        } else {
            holder.check_box.setTextColor(Color.WHITE);
        }
        holder.check_box.setCompoundDrawables(appIcon, null, null, null);
        holder.check_box.setCompoundDrawablePadding(15);

        holder.check_box.setTag(R.id.checkTag, pkg.packageName);
        holder.check_box.setChecked(isAppChecked(pkg));

        holder.check_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myOnClick(view);
            }
        });

        return convertView;
    }

    public void myOnClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        String appName = view.getTag(R.id.checkTag).toString();
        PackageInfo apk;
        try {
            apk = packageManager.getPackageInfo(appName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("onClick", "Application " + appName + " not found");
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

        }
    }

    /**
     * Just check if package is System or not.
     *
     * @param pkgInfo
     * @return true if package is a system app
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
    }

    private boolean isAppChecked(PackageInfo packageInfo) {
        return this.natRules.isAppInRules(Long.valueOf(packageInfo.applicationInfo.uid));
    }

    /**
     * View holder, used in order to optimize speed and display
     */
    static class ViewHolder {
        private CheckBox check_box;
    }
}
