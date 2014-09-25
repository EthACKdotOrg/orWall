package org.ethack.orwall.lib;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Comparator;

/**
 * Package comparator in order to sort a collection of application.
 */
public class PackageComparator implements Comparator<PackageInfo> {
    private PackageManager packageManager;

    public PackageComparator(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public int compare(PackageInfo packageInfo, PackageInfo packageInfo2) {
        String label1 = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        String label2 = packageManager.getApplicationLabel(packageInfo2.applicationInfo).toString();
        return label1.compareTo(label2);
    }
}
