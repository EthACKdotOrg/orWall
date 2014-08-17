package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BootBroadcast extends BroadcastReceiver {

    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        InitializeIptables initializeIptables = new InitializeIptables(context);
        initializeIptables.boot();

        IptRules iptRules = new IptRules();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        Set rules = sharedPreferences.getStringSet("nat_rules", new HashSet());

        for (Object rule : rules.toArray()) {
            HashMap<String, Long> r = (HashMap) rule;
            Long uid = (Long) r.values().toArray()[0];
            String name = (String) r.keySet().toArray()[0];
            iptRules.natApp(context, uid, 'A', name);
        }
    }

}
