package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.NatRules;

import java.util.ArrayList;

public class BootBroadcast extends BroadcastReceiver {

    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        InitializeIptables initializeIptables = new InitializeIptables(context);
        initializeIptables.boot();

        IptRules iptRules = new IptRules();

        NatRules natRules = new NatRules(context);
        Log.d("BootBroadcast: ", "Get NAT rules...");
        ArrayList<AppRule> rules = natRules.getAllRules();
        Log.d("BootBroadcast: ", "Length received: " + String.valueOf(rules.size()));

        for (AppRule rule : rules) {
            long uid = rule.getAppUID();
            String name = rule.getAppName();
            // TODO: take care of other rule content (port, proxytype and so on)
            iptRules.natApp(context, uid, 'A', name);
        }
    }
}
