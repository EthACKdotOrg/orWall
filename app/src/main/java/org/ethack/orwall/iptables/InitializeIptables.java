package org.ethack.orwall.iptables;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.ethack.orwall.R;
import org.ethack.orwall.lib.CheckSum;
import org.ethack.orwall.lib.Constants;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

import java.io.File;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Initialize IPTables. The application has
 * to run at least once before this can be called.
 * This initialization is the second steps needed in order to get
 * Orbot working.
 */
public class InitializeIptables {

    private final IptRules iptRules;
    private final String dir_dst = "/system/etc/init.d";
    private final String dst_file = String.format("%s/91firewall", dir_dst);
    private long trans_proxy;
    private long polipo_port;
    private Context context;

    /**
     * Construtor
     *
     * @param context
     */
    public InitializeIptables(Context context) {
        this.iptRules = new IptRules();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.trans_proxy = Long.valueOf(preferences.getString(Constants.PREF_TRANS_PORT, Long.toString(Constants.ORBOT_TRANSPROXY)));
        this.polipo_port = Long.valueOf(preferences.getString(Constants.PREF_POLIPO_PORT, Long.toString(Constants.ORBOT_POLIPO_PROXY)));
        this.context = context;
    }

    public void boot() {
        boolean authorized;
        Long app_uid;
        PackageManager packageManager = context.getPackageManager();

        try {
            app_uid = Long.valueOf(packageManager.getApplicationInfo("org.torproject.android", 0).uid);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(BroadcastReceiver.class.getName(), "Unable to get Orbot real UID — is it still installed?");
            app_uid = new Long(0); // prevents stupid compiler error… never used.
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        Log.d("Boot", "Deactivate some stuff at boot time in order to prevent crashes");
        this.context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false).apply();


        initOutputs(app_uid);
        initInput(app_uid);

        authorized = this.context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean("enable_lan", false);
        if (authorized) {
            LANPolicy(true);
        }

        authorized = this.context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_SIP_ENABLED, false);
        if (authorized) {
            app_uid = Long.valueOf(this.context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getString(Constants.PREF_KEY_SIP_APP, "0"));
            if (app_uid != 0) {
                Log.d("Boot", "Authorizing SIP");
                manageSip(true, app_uid);
            }
        }

        authorized = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_ADB_ENABLED, false);
        if(authorized) {
            enableADB(authorized);
        }

        authorized = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_POLIPO_ENABLED, false);
        if (authorized) {
            allowPolipo(authorized);
        }
    }

    public boolean iptablesExists() {
        File iptables = new File(Constants.IPTABLES);
        return iptables.exists();
    }

    public boolean isInitialized() {
        String rule = "-C witness -j RETURN";
        return iptRules.genericRule(rule);
    }

    public void LANPolicy(final boolean allow) {
        String[] lans = {
                "10.0.0.0/8",
                "172.16.0.0/12",
                "192.168.0.0/16"
        };
        if (allow) {
            if (iptRules.genericRule("-N LAN")) {
                iptRules.genericRule("-A LAN -p tcp -m tcp --dport 53 -j REJECT --reject-with icmp-port-unreachable");
                iptRules.genericRule("-A LAN -p udp -m udp --dport 53 -j REJECT --reject-with icmp-port-unreachable");
                iptRules.genericRule("-A LAN -j ACCEPT");
            }
        }
        for (String lan : lans) {
            if (!iptRules.LanNoNat(lan, allow)) {
                Log.e(
                        InitializeIptables.class.getName(),
                        String.format("Unable to bypass NAT for %s", lan));
            }
        }
        if (!allow) {
            iptRules.genericRule("-F LAN");
            iptRules.genericRule("-X LAN");
        }
    }

    public void enableADB(final boolean allow) {
        char action;
        if (allow) {
            action = 'I';
        } else {
            action = 'D';
        }

        String[] rules = {
                "-%c INPUT -p tcp --dport 5555 -j ACCEPT",
                "-%c OUTPUT -p tcp --sport 5555 -j ACCEPT",
                "-t nat -%c OUTPUT -p tcp --sport 5555 -j RETURN",
        };

        for (String rule : rules) {
            if (!iptRules.genericRule(String.format(rule, action))) {
                Log.e("enableADB", "Unable to add rule");
                Log.e("enableADB", String.format(rule, action));
            }
        }
    }

    public void initOutputs(final long orbot_uid) {
        String[] rules = {
                // flush all OUTPUT rules
                "-F OUTPUT",
                "-N accounting_OUT",
                "-A accounting_OUT -j bw_OUTPUT",
                "-A accounting_OUT -j ACCEPT",
                String.format(
                        "-A OUTPUT -m owner --uid-owner %d -p tcp --dport 9030 -j accounting_OUT -m comment --comment \"Forward Directory traffic to accounting\"",
                        orbot_uid
                ),
                String.format("-A OUTPUT -m owner --uid-owner %d -m conntrack --ctstate NEW,RELATED,ESTABLISHED -j ACCEPT -m comment --comment \"Allow Orbot outputs\"", orbot_uid),
                "-P OUTPUT DROP",
                // NAT
                String.format("-t nat -I OUTPUT 1 -m owner --uid-owner %d -j RETURN -m comment --comment \"Orbot bypasses itself.\"", orbot_uid),
        };
        for (String rule : rules) {
            if (!iptRules.genericRule(rule)) {
                Log.e(InitializeIptables.class.getName(), "Unable to initialize");
                Log.e(InitializeIptables.class.getName(), rule);
            }
        }
    }

    public void initInput(final long orbot_uid) {
        String[] rules = {
                "-F INPUT",
                "-N accounting_IN",
                "-A accounting_IN -j bw_INPUT",
                "-A accounting_IN -j ACCEPT",
                String.format(
                        "-A INPUT -m owner --uid-owner %d -m conntrack --ctstate NEW,RELATED,ESTABLISHED -j ACCEPT -m comment --comment \"Allow Orbot inputs\"",
                        orbot_uid
                ),
                "-P INPUT DROP",
        };
        for (String rule : rules) {
            if (!iptRules.genericRule(rule)) {
                Log.e(InitializeIptables.class.getName(), "Unable to initialize");
                Log.e(InitializeIptables.class.getName(), rule);
            }
        }
    }

    public void installInitScript(final Context context) {

        final String src_file = new File(context.getDir("bin", 0), "userinit.sh").getAbsolutePath();

        CheckSum check_src = new CheckSum(src_file);
        CheckSum check_dst = new CheckSum(dst_file);

        File dstDir = new File(dir_dst);

        if (dstDir.exists()) {

            if (!check_dst.hash().equals(check_src.hash())) {

                if (check_dst.hash().equals(Constants.E_NO_SUCH_FILE)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle(R.string.alert_install_script_title);
                    alert.setMessage(String.format(context.getString(R.string.alert_install_script_text), dst_file));
                    alert.setNegativeButton(R.string.alert_install_script_refuse, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_ENFOCE_INIT, false).apply();
                        }
                    });

                    alert.setPositiveButton(R.string.alert_install_script_accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            doInstallScripts(src_file, dst_file);
                            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_ENFOCE_INIT, true).apply();
                        }
                    });

                    alert.show();

                } else {
                    doInstallScripts(src_file, dst_file);
                }
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.alert_install_script_title);
            alert.setMessage(String.format(context.getString(R.string.alert_install_no_support), dir_dst));
            alert.setNeutralButton(R.string.main_dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_ENFOCE_INIT, false).apply();
                    context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_DISABLE_INIT, true).apply();
                }
            });
            alert.show();
        }
    }

    private void doInstallScripts(String src_file, String dst_file) {
        Shell shell = null;
        try {
            shell = Shell.startRootShell();
        } catch (IOException e) {
            Log.e("Shell", "Unable to get shell");
            return;
        }

        if (shell != null) {
            String CMD = String.format("cp %s %s", src_file, dst_file);

            SimpleCommand command1 = new SimpleCommand("mount -o remount,rw /system");
            SimpleCommand command2 = new SimpleCommand(CMD);
            CMD = String.format("chmod 0755 %s", dst_file);
            SimpleCommand command3 = new SimpleCommand(CMD);
            SimpleCommand command4 = new SimpleCommand("mount -o remount,ro /system");
            try {
                shell.add(command1).waitForFinish();
                shell.add(command2).waitForFinish();
                shell.add(command3).waitForFinish();
                shell.add(command4).waitForFinish();
            } catch (IOException e) {
                Log.e("Shell", "Unable to run simple command");
            } catch (TimeoutException e) {
                Log.e("Shell", "Error while closing the Shell");
            } finally {
                try {
                    shell.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void removeIniScript() {
        Shell shell = null;
        try {
            shell = Shell.startRootShell();
        } catch (IOException e) {
            Log.e("Shell", "Unable to get shell");
            return;
        }
        if (shell != null) {
            SimpleCommand command1 = new SimpleCommand("mount -o remount,rw /system");
            SimpleCommand command2 = new SimpleCommand("rm -f " + dst_file);
            SimpleCommand command3 = new SimpleCommand("mount -o remount,ro /system");
            try {
                shell.add(command1).waitForFinish();
                shell.add(command2).waitForFinish();
                shell.add(command3).waitForFinish();
            } catch (IOException e) {
                Log.e("Shell", "Unable to run simple command");
            } catch (TimeoutException e) {
                Log.e("Shell", "Error while closing the Shell");
            } finally {
                try {
                    shell.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void manageSip(boolean status, Long uid) {
        String[] rules = {
                "-%c INPUT -m owner --uid-owner %d -m conntrack --ctstate RELATED,ESTABLISHED -p udp -j accounting_IN",
                "-%c OUTPUT -m owner --uid-owner %d -p udp -j accounting_OUT",
                "-t nat -%c OUTPUT -m owner --uid-owner %d -p udp -j RETURN",
        };
        char action;
        if (status) {
            action = 'A';
        } else {
            action = 'D';
        }

        for (String rule : rules) {
            iptRules.genericRule(String.format(rule, action, uid));
        }
    }

    public void manageCaptiveBrowser(boolean status, Long uid) {
        String[] rules = {
                "-%c INPUT -m owner --uid-owner %d -m conntrack --ctstate RELATED,ESTABLISHED -p udp --sport 53 -j ACCEPT",
                "-%c INPUT -m conntrack --ctstate ESTABLISHED -m owner --uid-owner %d -j ACCEPT",
                "-%c OUTPUT -m owner --uid-owner %d -j ACCEPT",
                "-%c OUTPUT -m owner --uid-owner %d -m conntrack --ctstate ESTABLISHED -j ACCEPT",
                "-t nat -%c OUTPUT -m owner --uid-owner %d -j RETURN",
        };
        char action;
        if (status) {
            action = 'A';
        } else {
            action = 'D';
        }

        for (String rule : rules) {
            Log.d("ManageCaptiveBrowser", String.format(rule, action, uid));
            iptRules.genericRule(String.format(rule, action, uid));
        }
    }

    public void enableTethering(boolean status) {
        NetworkInterface wifi = null;
        try {
            wifi = NetworkInterface.getByName("wlan0");
        } catch (SocketException e) {
            Log.e("enableTethering", e.toString());
        }

        if (wifi != null) {
            List<InterfaceAddress> addresses = wifi.getInterfaceAddresses();
            InterfaceAddress address = (InterfaceAddress) addresses.toArray()[1];
            String ipv4 = address.getAddress().getHostAddress();

            String st[] = ipv4.split("\\.");
            String subnet = st[0] + "." + st[1] + "." + st[2] + ".0/24";

            char action;
            if (status) {
                action = 'I';
            } else {
                action = 'D';
            }

            String rules[] = {
                    "-%c INPUT -i wlan0 -j ACCEPT",
                    "-%c OUTPUT -o wlan0 -s %s -j ACCEPT",
                    "-t nat -%c OUTPUT ! -o lo -s %s -p tcp -m tcp --tcp-flags FIN,SYN,RST,ACK SYN -j REDIRECT --to-ports " + this.trans_proxy + " -m comment --comment \"Force Tether through TransPort\"",
                    "-t nat -%c OUTPUT -o wlan0 -j RETURN",
            };
            for (String rule : rules) {
                iptRules.genericRule(String.format(rule, action, subnet));
            }
        } else {
            Log.e("Tethering", "Unable to get Wifi state");
        }
    }

    public void allowPolipo(boolean status) {
        String[] rules = {
                "-%c INPUT -i lo -p tcp --dport %d -j accounting_IN -m conntrack --ctstate NEW,RELATED,ESTABLISHED -m comment --comment \"Allow local polipo inputs\"",
        };
        char action = 'D';
        if (status) {
            action = 'A';
        }

        for (String rule: rules) {
            iptRules.genericRule(String.format(rule, action, polipo_port));
        }
    }

    public void enableCaptiveDetection(boolean status, Context context) {
        // TODO: find a way to disable it on android <4.4
        // TODO: we may want to get some setting writer directly through the API.
        // This seems to be done with a System app only. orWall may become a system app.
        if (Build.VERSION.SDK_INT > 18) {

            String CMD;
            if (status) {
                CMD = new File(context.getDir("bin", 0), "activate_portal.sh").getAbsolutePath();
            } else {
                CMD = new File(context.getDir("bin", 0), "deactivate_portal.sh").getAbsolutePath();
            }
            Shell shell = null;
            try {
                shell = Shell.startRootShell();
            } catch (IOException e) {
                Log.e("Shell", "Unable to get shell");
            }

            if (shell != null) {
                SimpleCommand command = new SimpleCommand(CMD);
                try {
                    shell.add(command).waitForFinish();
                } catch (IOException e) {
                    Log.e("Shell", "IO Error");
                } catch (TimeoutException e) {
                    Log.e("Shell", "Timeout");
                } finally {
                    try {
                        shell.close();
                    } catch (IOException e) {

                    }
                }
            }

        }
    }


}
