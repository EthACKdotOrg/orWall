package org.ethack.orwall.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import org.ethack.orwall.BackgroundProcess;
import org.ethack.orwall.R;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * New adapter in order to create the grid for applications
 */
public class RowAdapter extends ArrayAdapter<PackageInfo> {
    private final Context context;
    private final Object[] pkgs;
    private final PackageManager packageManager;
    private SharedPreferences.Editor editor;
    private Set nat_rules;
    private boolean check_all = false;
    private IptRules iptRules = new IptRules();

    /**
     * Class builder
     *
     * @param context
     * @param pkgs
     * @param packageManager
     * @param check_all
     */
    public RowAdapter(Context context, List<PackageInfo> pkgs, PackageManager packageManager, boolean check_all) {
        super(context, R.layout.rowlayout, pkgs);
        this.context = context;
        this.pkgs = pkgs.toArray();
        this.packageManager = packageManager;
        SharedPreferences sharedPreferences = context.getSharedPreferences("org.ethack.orwall_preferences", Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.nat_rules = sharedPreferences.getStringSet("nat_rules", new HashSet());
        this.check_all = check_all;
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
            holder.check_box.setTextColor(Color.rgb(135,206,250));
        } else {
            holder.check_box.setTextColor(Color.WHITE);
        }
        holder.check_box.setCompoundDrawables(appIcon, null, null, null);
        holder.check_box.setCompoundDrawablePadding(15);

        holder.check_box.setTag(R.id.checkTag, pkg.packageName);
        if (!nat_rules.isEmpty()) {
            holder.check_box.setChecked(isAppChecked(pkg, nat_rules));
            if (this.check_all && !isAppChecked(pkg, nat_rules)) {
                holder.check_box.setChecked(this.check_all);
                HashMap rule = new HashMap<String, Long>();
                rule.put(pkg.packageName, Long.valueOf(pkg.applicationInfo.uid));
                nat_rules.add(rule);
                editor.remove("nat_rules").commit();
                editor.putStringSet("nat_rules", nat_rules).apply();

                Intent bgpProcess = new Intent(context, BackgroundProcess.class);
                bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_ADD_RULE);
                bgpProcess.putExtra(Constants.PARAM_APPNAME, pkg.packageName);
                bgpProcess.putExtra(Constants.PARAM_APPUID, Long.valueOf(pkg.applicationInfo.uid));
                context.startService(bgpProcess);
            }
        }

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
            Set current_rules = nat_rules;
            HashMap rule = new HashMap<String, Long>();
            rule.put(appName, appUID);
            if (checked) {
                iptRules.natApp(context, appUID, 'A', appName);
                current_rules.add(rule);
            } else {
                iptRules.natApp(context, appUID, 'D', appName);
                current_rules.remove(rule);
            }
            nat_rules = current_rules;
            editor.remove("nat_rules");
            editor.commit();
            editor.putStringSet("nat_rules", nat_rules);
            if (!editor.commit()) {
                Log.e("Rulset", "Unable to save new ruleset!");
            }
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

    private boolean isAppChecked(PackageInfo packageInfo, Set set) {
        for (Object row : set) {
            HashMap<String, Long> r = (HashMap) row;
            if ((Long) r.values().toArray()[0] == packageInfo.applicationInfo.uid) {
                return true;
            }
        }
        return false;
    }

    /**
     * View holder, used in order to optimize speed and display
     */
    static class ViewHolder {
        private TextView text_view;
        private CheckBox check_box;
    }
}
