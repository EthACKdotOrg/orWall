package org.ethack.orwall.iptables;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper for IPTables calls.
 */
public class IptRules {

    private final static String IPTABLES = "/system/bin/iptables";
    private final static String PREF_TRANS_PORT = "proxy_transport";
    private String RULE;

    /**
     * Apply an IPTables rule
     *
     * @param rule
     * @return true if success
     */
    private boolean applyRule(final String rule) {
        Shell shell = null;
        try {
            shell = Shell.startRootShell();
        } catch (IOException e) {
            Log.e("Shell", "NO shell !");
        }

        if (shell != null) {
            SimpleCommand cmd = new SimpleCommand(rule);
            try {
                shell.add(cmd).waitForFinish();
                return true;
            } catch (IOException e) {
                Log.e("Shell", "Unable to run simple command");
                Log.e("Shell", rule);
            } catch (TimeoutException e) {
                Log.e("Shell", "A timeout was reached");
                Log.e("Shell", e.getMessage());
            } finally {
                try {
                    shell.close();
                }catch (IOException e) {
                    Log.e("Shell", "Error while closing the Shell");
                }
            }
        }
        return false;
    }

    /**
     * Build an iptables call in order to either create or remove NAT rule
     *
     * @param context
     * @param appUID
     * @param action
     * @param appName
     * @return true if success
     */
    public boolean natApp(Context context, final long appUID, final char action, final String appName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long trans_port = Long.valueOf(preferences.getString(PREF_TRANS_PORT, "9040"));
        RULE = "%s -t nat -%c OUTPUT ! -o lo -p tcp -m tcp --tcp-flags FIN,SYN,RST,ACK SYN -m owner --uid-owner %d -j REDIRECT --to-ports %d -m comment --comment \"Force %s through TransPort\"";

        return applyRule(String.format(RULE, IPTABLES, action, appUID, trans_port, appName));
    }

    public boolean LanNoNat(final String lan, final boolean allow) {
        if (allow) {
            RULE = "%s -I OUTPUT 1 -d %s -j LAN";
        } else {
            RULE = "%s -D OUTPUT -d %s -j LAN";
        }
        return (applyRule(String.format(RULE, IPTABLES, lan)));
    }

    public boolean genericRule(final String rule) {
        return applyRule(String.format("%s %s", IPTABLES, rule));
    }
}
