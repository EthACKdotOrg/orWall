package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;

import java.util.ArrayList;
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

        NatRules natRules = new NatRules(context);
        ArrayList<AppRule> rules = natRules.getAllRules();

        for (AppRule rule : rules) {
            long uid = rule.getAppUID();
            String name = rule.getAppName();
            // TODO: take care of other rule content (port, proxytype and so on)
            iptRules.natApp(context, uid, 'A', name);
        }
    }

}
