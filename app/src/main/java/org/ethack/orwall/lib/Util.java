package org.ethack.orwall.lib;

import android.content.Context;
import android.content.pm.PackageManager;

public class Util {
    public static boolean isOrbotInstalled(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(Constants.ORBOT_APP_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
