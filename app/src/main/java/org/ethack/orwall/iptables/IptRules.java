package org.ethack.orwall.iptables;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.ethack.orwall.lib.Constants;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper for IPTables calls.
 */
public class IptRules {

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
                return (cmd.getExitCode() == 0);
            } catch (IOException e) {
                Log.e("Shell", "Unable to run simple command");
                Log.e("Shell", rule);
            } catch (TimeoutException e) {
                Log.e("Shell", "A timeout was reached");
                Log.e("Shell", e.getMessage());
            } finally {
                try {
                    shell.close();
                } catch (IOException e) {
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
    public void natApp(Context context, final long appUID, final char action, final String appName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long trans_port = Long.valueOf(preferences.getString(Constants.PREF_TRANS_PORT, "9040"));
        long dns_port = Long.valueOf(preferences.getString(Constants.PREF_DNS_PORT, "5400"));
        String[] RULES = {
                String.format(
                        "%s -t nat -%c OUTPUT ! -o lo -p tcp -m tcp --tcp-flags FIN,SYN,RST,ACK SYN -m owner --uid-owner %d -j REDIRECT --to-ports %d -m comment --comment \"Force %s through TransPort\"",
                        Constants.IPTABLES, action, appUID, trans_port, appName
                ),
                String.format(
                        "%s -%c OUTPUT -d 127.0.0.1/32 -m owner --uid-owner %d -p tcp --dport %d -j accounting_OUT -m comment --comment \"Allow %s through TransPort\"",
                        Constants.IPTABLES, action, appUID, trans_port, appName
                ),
                String.format(
                        "%s -%c INPUT -i lo -m conntrack --ctstate RELATED,ESTABLISHED -m owner --uid-owner %d -j ACCEPT -m comment --comment \"Allow local inputs for %s\"",
                        Constants.IPTABLES, action, appUID, appName
                ),
                String.format(
                        "%s -%c INPUT -i lo -m owner --uid-owner %d -p tcp --dport %d -j ACCEPT -m comment --comment \"Allow %s through TransPort\"",
                        Constants.IPTABLES, action, appUID, trans_port, appName
                ),
                String.format(
                        "%s -t nat -%c OUTPUT ! -o lo -m owner --uid-owner %d -p udp -m udp --dport 53 -j REDIRECT --to-ports %d -m comment --comment \"Redirect DNS queries for %s\"",
                        Constants.IPTABLES, action, appUID, dns_port, appName
                ),
                String.format(
                        "%s -%c OUTPUT -d 127.0.0.1/32 -m owner --uid-owner %d -p udp --dport %d -j accounting_OUT -m comment --comment \"DNS Requests for %s on Tor DNSPort\"",
                        Constants.IPTABLES, action, appUID, dns_port, appName
                ),
        };

        for (String rule : RULES) {
            if (!applyRule(rule)) {
                Log.e(InitializeIptables.class.getName(), rule);
            }
        }
    }

    public boolean LanNoNat(final String lan, final boolean allow) {
        if (allow) {
            RULE = "%s -I OUTPUT 1 -d %s -j LAN";
        } else {
            RULE = "%s -D OUTPUT -d %s -j LAN";
        }
        return (applyRule(String.format(RULE, Constants.IPTABLES, lan)));
    }

    public boolean genericRule(final String rule) {
        return applyRule(String.format("%s %s", Constants.IPTABLES, rule));
    }
}
