package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.ethack.orwall.iptables.InitializeIptables;

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
