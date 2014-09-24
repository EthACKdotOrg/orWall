package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;

import java.util.ArrayList;

public class BootBroadcast extends BroadcastReceiver {

    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        InitializeIptables initializeIptables = new InitializeIptables(context);
        initializeIptables.supportComments();
        initializeIptables.boot();
    }
}
