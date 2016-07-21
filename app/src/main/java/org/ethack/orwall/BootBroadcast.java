package org.ethack.orwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.ethack.orwall.lib.Iptables;
import org.ethack.orwall.lib.Preferences;

/**
 * Do think at startup.
 */
public class BootBroadcast extends BroadcastReceiver {

    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Iptables iptables = new Iptables(context);

        // Enforce init-script if sharedpreference says it
        // We want to do it the earlier.
        // Also, we want to get a fresh status regarding the init-script support: this can be
        // a reboot after a ROM upgrade or change.
        boolean enforceInit = Preferences.isEnforceInitScript(context);
        if (Iptables.initSupported() && enforceInit) {
            Iptables.installInitScript(context);
        }
        // Apply boot-up rules in order to enable traffic for orbot and other things.

        if (Preferences.isOrwallEnabled(context)){
            iptables.boot();
        }
    }
}
