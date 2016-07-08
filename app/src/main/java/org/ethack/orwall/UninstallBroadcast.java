package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.NatRules;

public class UninstallBroadcast extends BroadcastReceiver {
    private final static String TAG = "UninstallBroadcast";

    public UninstallBroadcast() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri data = intent.getData();

        if (!data.getScheme().equals("package")) {
            Log.d(TAG, "Intent scheme was not 'package'");
            return;
        }

        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) && !replacing) {
            final long uid = intent.getIntExtra(Intent.EXTRA_UID, -123);
            final String appName = intent.getData().getSchemeSpecificPart();
            Log.d("UninstallBroadcast", "AppName: " + appName + ", AppUID: " + uid);

            // is the app present in rules?
            NatRules natRules = new NatRules(context);

            AppRule rule = natRules.getAppRule(uid);
            if (rule.isStored()) {

                // First: remove rule from firewall if any
                rule.uninstall(context);

                // Second: remove app from NatRules if present
                natRules.removeAppFromRules(uid);
            }
        }
    }
}
