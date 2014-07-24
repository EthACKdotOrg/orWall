package org.ethack.torrific.adapter;

import android.content.Context;
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

import org.ethack.torrific.R;
import org.ethack.torrific.iptables.IptRules;
import org.ethack.torrific.lib.NATLiteSource;

import java.sql.SQLException;
import java.util.List;

/**
 * New adapter in order to create the grid for applications
 */
public class RowAdapter extends ArrayAdapter<PackageInfo> {
    private final Context context;
    private final Object[] pkgs;
    private final PackageManager packageManager;
    private NATLiteSource natLiteSource;

    /**
     * Class builder
     *
     * @param context
     * @param pkgs
     * @param packageManager
     * @param natLiteSource
     */
    public RowAdapter(Context context, List<PackageInfo> pkgs, PackageManager packageManager, NATLiteSource natLiteSource) {
        super(context, R.layout.rowlayout, pkgs);
        this.context = context;
        this.pkgs = pkgs.toArray();
        this.packageManager = packageManager;
        this.natLiteSource = natLiteSource;
        try {
            natLiteSource.open();
        } catch (SQLException e) {
            Log.d("FUCKING STUFF", "Why you NO working?");
        }
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
            holder.text_view = (TextView) convertView.findViewById(R.id.label);
            holder.check_box = (CheckBox) convertView.findViewById(R.id.appCheckbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String label = packageManager.getApplicationLabel(pkg.applicationInfo).toString();
        Drawable appIcon = packageManager.getApplicationIcon(pkg.applicationInfo);
        appIcon.setBounds(0, 0, 40, 40);

        holder.text_view.setText(label);
        if (isSystemPackage(pkg)) {
            holder.text_view.setTextColor(Color.RED);
        } else {
            holder.text_view.setTextColor(Color.WHITE);
        }
        holder.text_view.setCompoundDrawables(appIcon, null, null, null);
        holder.text_view.setCompoundDrawablePadding(15);

        holder.check_box.setTag(R.id.checkTag, pkg.packageName);
        holder.check_box.setChecked(natLiteSource.natExists(pkg.applicationInfo.uid));
        holder.check_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                    IptRules iptRules = new IptRules();
                    long appUID = apk.applicationInfo.uid;
                    if (checked) {
                        iptRules.natApp(appUID, 'A', appName);
                        natLiteSource.createNAT(appUID, appName);
                    } else {
                        iptRules.natApp(appUID, 'D', appName);
                        natLiteSource.deleteNAT(appUID);
                    }
                }
            }
        });

        return convertView;
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

    /**
     * View holder, used in order to optimize speed and display
     */
    static class ViewHolder {
        private TextView text_view;
        private CheckBox check_box;
    }
}
