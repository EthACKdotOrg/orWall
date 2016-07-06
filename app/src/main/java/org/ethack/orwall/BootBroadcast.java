package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;

/**
 * Do think at startup.
 */
public class BootBroadcast extends BroadcastReceiver {

    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).
                getBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true))
            return;

        InitializeIptables initializeIptables = new InitializeIptables(context);

        // We want to ensure we support comments — reboot may be due to
        // some ROM change, hence kernel may have changed as well.
        initializeIptables.supportComments();

        // Enforce init-script if sharedpreference says it
        // We want to do it the earlier.
        // Also, we want to get a fresh status regarding the init-script support: this can be
        // a reboot after a ROM upgrade or change.
        boolean enforceInit = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_ENFOCE_INIT, true);
        if (initializeIptables.initSupported() && enforceInit) {
            initializeIptables.installInitScript();
        }
        // Apply boot-up rules in order to enable traffic for orbot and other things.
        initializeIptables.boot();
    }
}
