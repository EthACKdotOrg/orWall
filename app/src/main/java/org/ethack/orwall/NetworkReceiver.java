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
    public NetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean support_tethering = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_IS_TETHER_ENABLED, false);

        if (support_tethering) {
            int status = NetworkHelper.getConnectivityStatus(context);

            InitializeIptables initializeIptables = new InitializeIptables(context);
            if (status == 3) {
                Toast.makeText(context, R.string.tether_activated_in_orwall, Toast.LENGTH_LONG).show();
                initializeIptables.enableTethering(true);
            } else {
                if (initializeIptables.isTetherEnabled()) {
                    Toast.makeText(context, R.string.tether_deactivated_in_orwall, Toast.LENGTH_LONG).show();
                    initializeIptables.enableTethering(false);
                } else {
                    Log.d("NetworkReceiver", "Nothing to do");
                }
            }
        }
    }
}
