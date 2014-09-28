package org.ethack.orwall.lib;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Comparator;

/**
 * Comparator: allows to sort appRule collection using application name.
 */
public class AppRuleComparator implements Comparator<AppRule> {
    private static final String TAG = "AppRuleComparator";

    private PackageManager packageManager;

    public AppRuleComparator(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public int compare(AppRule appRule1, AppRule appRule2) {
        String label1 = null;
        if (appRule1.getPkgName().startsWith(Constants.SPECIAL_APPS_PREFIX)) {
            label1 = PackageInfoData.specialApps().get(appRule1.getPkgName()).getName();
        } else {
            try {
                PackageInfo pkgInfo1 = packageManager.getPackageInfo(appRule1.getPkgName(), PackageManager.GET_PERMISSIONS);
                label1 = packageManager.getApplicationLabel(pkgInfo1.applicationInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        String label2 = null;
        if (appRule2.getPkgName().startsWith(Constants.SPECIAL_APPS_PREFIX)) {
            label2 = PackageInfoData.specialApps().get(appRule2.getPkgName()).getName();
        } else {
            try {
                PackageInfo pkgInfo2 = packageManager.getPackageInfo(appRule2.getPkgName(), PackageManager.GET_PERMISSIONS);
                label2 = packageManager.getApplicationLabel(pkgInfo2.applicationInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        if (label1 != null && label2 != null) {
            return label1.compareTo(label2);
        } else {
            return 0;
        }
    }
}
