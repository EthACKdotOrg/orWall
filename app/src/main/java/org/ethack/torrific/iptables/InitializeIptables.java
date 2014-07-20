package org.ethack.torrific.iptables;

import android.content.Context;
import android.util.Log;

import org.ethack.torrific.R;
import org.ethack.torrific.lib.CheckSum;
import org.ethack.torrific.lib.NATLiteSource;
import org.ethack.torrific.lib.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Initialize IPTables. The application has
 * to run at least once before this can be called.
 * This initialization is the second steps needed in order to get
 * Orbot working.
 */
public class InitializeIptables {

    private final NATLiteSource natLiteSource;
    private final IptRules iptRules;

    /**
     * Construtor
     *
     * @param natLiteSource
     */
    public InitializeIptables(NATLiteSource natLiteSource) {
        this.natLiteSource = natLiteSource;
        this.iptRules = new IptRules();
    }

    public void LANPolicy(boolean allow) {
        String[] lans = {
                "10.0.0.0/8",
                "172.16.0.0/12",
                "192.168.0.0/16"
        };
        if (allow) {
            for (String lan : lans) {
                if (!iptRules.LanNoNat(lan)) {
                    Log.e(
                            InitializeIptables.class.getName(),
                            String.format("Unable to bypass NAT for %s", lan));
                }
            }
        }
    }

    public void initOutputs(final long orbot_uid) {
        String[] rules = {
                "-A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT",
                String.format("-A OUTPUT -m owner --uid-owner %d -j ACCEPT -m comment --comment \"Allow Orbot output\"", orbot_uid),
                "-A OUTPUT -d 127.0.0.1/32 -p udp -m udp --dport 5400 -j ACCEPT -m comment --comment \"DNS Requests on Tor DNSPort\"",
                "-A OUTPUT -d 127.0.0.1/32 -p tcp -m tcp --dport 8118 --tcp-flags FIN,SYN,RST,ACK SYN -j ACCEPT -m comment --comment \"Local traffic to Polipo\"",
                "-A OUTPUT -d 127.0.0.1/32 -p tcp -m tcp --dport 9040 --tcp-flags FIN,SYN,RST,ACK SYN -j ACCEPT -m comment --comment \"Local traffic to TransPort\"",
                "-A OUTPUT -d 127.0.0.1/32 -p tcp -m tcp --dport 9050 --tcp-flags FIN,SYN,RST,ACK SYN -j ACCEPT -m comment --comment \"Local traffic to SOCKSPort\""
        };
        Log.d(InitializeIptables.class.getName(), "Orbot: " + orbot_uid);
        for (String rule : rules) {
            if (!iptRules.genericRule(rule)) {
                Log.e(InitializeIptables.class.getName(), "Unable to initialize");
                Log.e(InitializeIptables.class.getName(), rule);
            }
        }
    }

    public void installInitScript(Context context) {
        final String src_file = new File(context.getDir("bin", 0), "userinit.sh").getAbsolutePath();
        final String dir_dst = "/data/local/userinit.d";
        final String dst_file = String.format("%s/torrific.sh", dir_dst);
        final Shell shell = new Shell();

        if (!installBinary(context, R.raw.userinit, "userinit.sh")) {
            Log.d("Init", "We're fucked… unable to extract userinit.sh script");
        }

        File dst = new File(dir_dst);
        String CMD = String.format("mkdir -p %s", dir_dst);
        boolean dst_exists = (dst.exists() || shell.suExec(CMD));

        if (dst_exists) {

            CheckSum check_src = new CheckSum(src_file);
            CheckSum check_dst = new CheckSum(dst_file);

            if (check_dst.hash().equals(check_src.hash())) {
                Log.d("Init", "Nothing to do with init script");
            } else {

                CMD = String.format("cp %s %s", src_file, dst_file);
                if (shell.suExec(CMD)) {
                    Log.d("Init", "Successfully installed userinit.sh script");
                    CMD = String.format("chmod 0755 %s", dst_file);
                    if (shell.suExec(CMD)) {
                        Log.d("Init", "Successfully chmod file");
                    } else {
                        Log.e("Init", "ERROR while doing chmod on initscript");
                    }
                } else {
                    Log.e("Init", "ERROR while copying file to " + dst_file);
                }
            }
        } else {
            Log.e("Init", "Seems there is NO way to install the init-script in " + dst);
        }
    }

    /**
     * Thanks to AFWall :)
     *
     * @param ctx
     * @param resId
     * @param filename
     * @return
     */
    private static boolean installBinary(Context ctx, int resId, String filename) {
        try {
            File f = new File(ctx.getDir("bin", 0), filename);
            if (f.exists()) {
                f.delete();
            }
            copyRawFile(ctx, resId, f, "0755");
            return true;
        } catch (Exception e) {
            Log.e(InitializeIptables.class.getName(), "installBinary failed: " + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Copies a raw resource file, given its ID to the given location
     *
     * @param ctx   context
     * @param resid resource id
     * @param file  destination file
     * @param mode  file permissions (E.g.: "755")
     * @throws IOException          on error
     * @throws InterruptedException when interrupted
     *
     * Thanks AFWall source code
     */
    private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException {
        final String abspath = file.getAbsolutePath();
        // Write the iptables binary
        final FileOutputStream out = new FileOutputStream(file);
        final InputStream is = ctx.getResources().openRawResource(resid);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }


}
