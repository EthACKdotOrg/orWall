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

    private String getLabel(AppRule appRule){
        if (appRule.getAppName() == null){
            if (appRule.getPkgName().startsWith(Constants.SPECIAL_APPS_PREFIX)) {
                appRule.setAppName(PackageInfoData.specialApps().get(appRule.getPkgName()).getName());
            } else {
                try {
                    PackageInfo pkgInfo1 = packageManager.getPackageInfo(appRule.getPkgName(), PackageManager.GET_PERMISSIONS);
                    appRule.setAppName(packageManager.getApplicationLabel(pkgInfo1.applicationInfo).toString());
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        String label =  appRule.getAppName();
        return label;
    }

    @Override
    public int compare(AppRule appRule1, AppRule appRule2) {
        String label1 = getLabel(appRule1);
        String label2 = getLabel(appRule2);

        if (label1 != null && label2 != null) {
            return label1.compareTo(label2);
        } else {
            return 0;
        }
    }
}
