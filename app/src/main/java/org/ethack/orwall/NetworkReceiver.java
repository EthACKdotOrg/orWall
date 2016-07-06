package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NetworkHelper;

public class NetworkReceiver extends BroadcastReceiver {
    private static String TAG = "NetworkReceiver";

    public NetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.d(TAG, "Got a Network Change event: " + action);

        InitializeIptables initializeIptables = new InitializeIptables(context);

        boolean support_tethering = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_TETHER_ENABLED, false);
        if (support_tethering) {
            Log.d(TAG, "Tethering support is enabled");


            if (NetworkHelper.isTether(context)) {
                Log.d(TAG, "Enable Tethering");
                Toast.makeText(context, R.string.tether_activated_in_orwall, Toast.LENGTH_LONG).show();
                initializeIptables.enableTethering(true);
            } else {
                if (initializeIptables.isTetherEnabled()) {
                    Log.d(TAG, "Disable Tethering");
                    Toast.makeText(context, R.string.tether_deactivated_in_orwall, Toast.LENGTH_LONG).show();
                    initializeIptables.enableTethering(false);
                } else {
                    Log.d(TAG, "Nothing to do");
                }
            }
        } else {
            Log.d(TAG, "Tethering support is disabled");
        }

        if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") || action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Log.d(TAG, "Will do some LAN stuff");

            //boolean lan_bypass = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_LAN_ENABLED, false);
            initializeIptables.LANPolicy();
        }
    }
}
