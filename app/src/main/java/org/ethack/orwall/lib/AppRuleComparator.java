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
        PackageInfo pkgInfo1 = null;
        try {
            pkgInfo1 = packageManager.getPackageInfo(appRule1.getPkgName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        PackageInfo pkgInfo2 = null;
        try {
            pkgInfo2 = packageManager.getPackageInfo(appRule2.getPkgName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        if (pkgInfo1 != null && pkgInfo2 != null) {
            String label1 = packageManager.getApplicationLabel(pkgInfo1.applicationInfo).toString();
            String label2 = packageManager.getApplicationLabel(pkgInfo2.applicationInfo).toString();
            return label1.compareTo(label2);
        } else {
            return 0;
        }
    }
}
