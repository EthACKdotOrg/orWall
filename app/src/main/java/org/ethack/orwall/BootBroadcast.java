package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BootBroadcast extends BroadcastReceiver {

    public final static String PREFERENCE = "org.ethack.orwall_preferences";
    public final static String PREF_KEY_SIP_APP = "sip_app";
    public final static String PREF_KEY_SIP_ENABLED = "sip_enabled";
    public final static String PREF_KEY_ADB_ENABLED = "enable_adb";

    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        boolean authorized = false;
        Long app_uid;
        PackageManager packageManager = context.getPackageManager();

        try {
            app_uid = Long.valueOf(packageManager.getApplicationInfo("org.torproject.android", 0).uid);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(BroadcastReceiver.class.getName(), "Unable to get Orbot real UID — is it still installed?");
            app_uid = new Long(0); // prevents stupid compiler error… never used.
            android.os.Process.killProcess(android.os.Process.myPid());
        }


        InitializeIptables initializeIptables = new InitializeIptables(context);
        initializeIptables.initOutputs(app_uid);

        authorized = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE).getBoolean("enable_lan", false);
        if (authorized) {
            initializeIptables.LANPolicy(true);
        }

        authorized = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE).getBoolean(PREF_KEY_SIP_ENABLED, false);
        if (authorized) {
            app_uid = Long.valueOf(context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE).getString(PREF_KEY_SIP_APP, "0"));
            if (app_uid != 0) {
                Log.d("Boot", "Authorizing SIP");
                initializeIptables.manageSip(true, app_uid);
            }
        }

        authorized = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE).getBoolean(PREF_KEY_ADB_ENABLED,false);
        initializeIptables.enableADB(authorized);

        IptRules iptRules = new IptRules();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        Set rules = sharedPreferences.getStringSet("nat_rules", new HashSet());

        for (Object rule : rules.toArray()) {
            HashMap<String, Long> r = (HashMap) rule;
            Long uid = (Long) r.values().toArray()[0];
            String name = (String) r.keySet().toArray()[0];
            iptRules.natApp(context, uid, 'A', name);
        }
    }

}
