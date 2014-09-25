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

    private boolean supportComment;

    public IptRules(boolean supportComment) {
        this.supportComment = supportComment;
    }

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
     */
    public void natApp(Context context, final long appUID, final char action, final String appName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long trans_port = Long.valueOf(preferences.getString(Constants.PREF_TRANS_PORT, String.valueOf(Constants.ORBOT_TRANSPROXY)));
        long dns_port = Long.valueOf(preferences.getString(Constants.PREF_DNS_PORT, String.valueOf(Constants.ORBOT_DNS_PROXY)));
        String[] RULES = {
                String.format(
                        "%s -t nat -%c OUTPUT ! -d 127.0.0.1 -p tcp -m tcp --tcp-flags FIN,SYN,RST,ACK SYN -m owner --uid-owner %d -j REDIRECT --to-ports %d%s",
                        Constants.IPTABLES, action, appUID, trans_port,
                        (this.supportComment ? String.format(" -m comment --comment \"Force %s through TransPort\"", appName) : "")
                ),
                String.format(
                        "%s -t nat -%c OUTPUT ! -d 127.0.0.1 -p udp --dport 53 -m owner --uid-owner %d -j REDIRECT --to-ports %d%s",
                        Constants.IPTABLES, action, appUID, dns_port,
                        (this.supportComment ? String.format(" -m comment --comment \"Force %s through DNSProxy\"", appName) : "")
                ),
                String.format(
                        "%s -%c OUTPUT -d 127.0.0.1 -m conntrack --ctstate NEW,ESTABLISHED -m owner --uid-owner %d -m tcp -p tcp --dport %d -j accounting_OUT%s",
                        Constants.IPTABLES, action, appUID, trans_port,
                        (this.supportComment ? String.format(" -m comment --comment \"Allow %s through TransPort\"", appName) : "")
                ),
                String.format(
                        "%s -%c OUTPUT -d 127.0.0.1 -m conntrack --ctstate NEW,ESTABLISHED -m owner --uid-owner %d -p udp --dport %d -j accounting_OUT%s",
                        Constants.IPTABLES, action, appUID, dns_port,
                        (this.supportComment ? String.format(" -m comment --comment \"Allow %s through DNSProxy\"", appName) : "")
                ),
        };

        for (String rule : RULES) {
            if (!applyRule(rule)) {
                Log.e(InitializeIptables.class.getName(), rule);
            }
        }
    }

    public void LanNoNat(final String lan, final boolean allow) {
        char action = (allow ? 'I' : 'D');

        String[] rules = {
                "%s -%c OUTPUT -d %s -j LAN",
                "%s -%c INPUT -d %s -j LAN",
                "%s -t nat -%c OUTPUT -d %s -j RETURN",
        };

        String formatted;
        for (String rule : rules) {
            formatted = String.format(rule, Constants.IPTABLES, action, lan);
            if (!applyRule(formatted)) {
                Log.e(
                        "LanNoNat",
                        "Unable to add rule: " + formatted
                );
            }
        }
    }

    public boolean genericRule(final String rule) {
        return applyRule(String.format("%s %s", Constants.IPTABLES, rule));
    }

    public void bypass(final long appUID, final String appName, final boolean allow) {
        char action = (allow ? 'I' : 'D');
        String[] rules = {
                String.format(
                        "-t nat -%c OUTPUT -m owner --uid-owner %d -j RETURN%s",
                        action, appUID,
                        (this.supportComment ? String.format(" -m comment --comment \"Allow %s to bypass Proxies\"", appName) : "")
                ),
                String.format(
                        "-%c OUTPUT -m conntrack --ctstate NEW,ESTABLISHED,RELATED -m owner --uid-owner %d -j ACCEPT%s",
                        action, appUID,
                        (this.supportComment ? String.format(" -m comment --comment \"Allow %s to bypass Proxies\"", appName) : "")
                ),
        };

        for (String rule: rules) {
            if (!applyRule(rule)) {
                Log.e(
                        "bypass",
                        "Unable to add rule: " + rule
                );
            }
        }
    }

    public void fenced(final long appUID, final String appName, final boolean allow) {
        char action = (allow ? 'A' : 'D');

        String[] rules = {
                String.format(
                        "-t nat -%c OUTPUT -m owner --uid-owner %d -j RETURN%s",
                        action, appUID,
                        (this.supportComment ? String.format(" -m comment --comment \"Fencing %s\"", appName) : "")
                ),
                String.format(
                        "-%c INPUT -i lo -m conntrack --ctstate NEW,ESTABLISHED.RELATED -m owner --uid-owner %d -j ACCEPT%s",
                        action, appUID,
                        (this.supportComment ? String.format(" -m comment --comment \"Allow %s to connect on localhost\"", appName) : "")
                ),
                String.format(
                        "-%c OUTPUT -o lo -m conntrack --ctstate NEW,ESTABLISHED,RELATED -m owner --uid-owner %d -j ACCEPT%s",
                        action, appUID,
                        (this.supportComment ? String.format(" -m comment --comment \"Allow %s to connect on localhost\"", appName) : "")
                ),
        };

        for (String rule: rules) {
            if (!applyRule(rule)) {
                Log.e(
                        "fenced",
                        "Unable to add rule: " + rule
                );
            }
        }
    }
}
