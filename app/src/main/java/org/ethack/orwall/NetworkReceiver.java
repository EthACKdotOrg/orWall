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
        boolean support_tethering = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_TETHER_ENABLED, false);

        Log.d(TAG, "Got a Network Change event");

        if (support_tethering) {
            Log.d(TAG, "Tethering support is enabled");

            InitializeIptables initializeIptables = new InitializeIptables(context);
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
    }
}
