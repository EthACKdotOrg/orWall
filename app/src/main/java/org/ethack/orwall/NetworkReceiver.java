package org.ethack.orwall;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.ethack.orwall.lib.Iptables;
import org.ethack.orwall.lib.NetworkHelper;
import org.ethack.orwall.lib.Preferences;

public class NetworkReceiver extends BroadcastReceiver {
    private static String TAG = "NetworkReceiver";

    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    public static final String EXTRA_ACTIVE_TETHER = "activeArray";

    public NetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Preferences.isOrwallEnabled(context)){
            return;
        }

        String action = intent.getAction();

        Log.d(TAG, "Got a Network Change event: " + action);

        Iptables iptables = new Iptables(context);

        if (action.equals(ACTION_TETHER_STATE_CHANGED)){
            // try the faster way
            Set<String> set = new HashSet<>(0);
            ArrayList<String> active = intent.getStringArrayListExtra(EXTRA_ACTIVE_TETHER);
            if (active != null){
                for(String intf: active) set.add(intf);
            } else {
                // hum, try the old fashioned way
                NetworkHelper.getTetheredInterfaces(context, set);
            }

            Set<String> oldIntfs = Preferences.getTetherInterfaces(context);

            if (!set.equals(oldIntfs))
                iptables.tetherUpdate(context, oldIntfs, set);
        }
        else
        if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") || action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Log.d(TAG, "Will do some LAN stuff");

            iptables.LANPolicy();
        }
    }
}
