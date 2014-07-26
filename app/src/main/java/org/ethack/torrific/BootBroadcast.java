package org.ethack.torrific;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import org.ethack.torrific.iptables.InitializeIptables;
import org.ethack.torrific.iptables.IptRules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BootBroadcast extends BroadcastReceiver {
    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        PackageManager packageManager = context.getPackageManager();

        long orbot_real_id = 0;
        try {
            orbot_real_id = packageManager.getApplicationInfo("org.torproject.android", 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(BroadcastReceiver.class.getName(), "Unable to get Orbot real UID — is it still installed?");
            android.os.Process.killProcess(android.os.Process.myPid());
        }


        InitializeIptables initializeIptables = new InitializeIptables();
        initializeIptables.initOutputs(orbot_real_id);

        boolean authorizeLAN = context.getSharedPreferences("org.ethack.torrific_preferences", Context.MODE_PRIVATE).getBoolean("enable_lan", false);
        if (authorizeLAN) {
            initializeIptables.LANPolicy(authorizeLAN);
        }

        IptRules iptRules = new IptRules();
        SharedPreferences sharedPreferences = context.getSharedPreferences("org.ethack.torrific_preferences", Context.MODE_PRIVATE);
        Set rules = sharedPreferences.getStringSet("nat_rules", new HashSet());

        for (Object rule : rules.toArray()) {
            HashMap<String, Long> r = (HashMap) rule;
            Long uid = (Long) r.values().toArray()[0];
            String name = (String) r.keySet().toArray()[0];
            iptRules.natApp(uid, 'A', name);
        }
    }

}
