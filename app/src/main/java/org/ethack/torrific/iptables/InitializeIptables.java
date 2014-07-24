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
    private final String dir_dst = "/data/local";
    private final String dst_file = String.format("%s/userinit.sh", dir_dst);
    private final Shell shell = new Shell();

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
            if (iptRules.genericRule("-N LAN")) {
                iptRules.genericRule("-A LAN -p tcp -m tcp --dport 53 -j REJECT --reject-with icmp-port-unreachable");
                iptRules.genericRule("-A LAN -p udp -m udp --dport 53 -j REJECT --reject-with icmp-port-unreachable");
                iptRules.genericRule("-A LAN -j ACCEPT");
            }
        } else {
            iptRules.genericRule("-X LAN");
        }
        for (String lan : lans) {
            if (!iptRules.LanNoNat(lan, allow)) {
                Log.e(
                        InitializeIptables.class.getName(),
                        String.format("Unable to bypass NAT for %s", lan));
            }
        }
    }

    public void initOutputs(final long orbot_uid) {
        String[] rules = {
                "-I INPUT 1 -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT -m comment --comment \"Allow established and related connections\"",
                String.format("-t nat -I OUTPUT 1 -m owner --uid-owner %d -j RETURN -m comment --comment \"Orbot bypasses itself.\"", orbot_uid),
                "-t nat -I OUTPUT 2 ! -o lo -p udp -m udp --dport 53 -j REDIRECT --to-ports 5400",
                "-I OUTPUT 1 -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT",
                String.format("-I OUTPUT 2 -m owner --uid-owner %d -j ACCEPT -m comment --comment \"Allow Orbot output\"", orbot_uid),
                "-I OUTPUT 3 -d 127.0.0.1/32 -p udp -m udp --dport 5400 -j ACCEPT -m comment --comment \"DNS Requests on Tor DNSPort\"",
                "-I OUTPUT 3 -d 127.0.0.1/32 -p tcp -m tcp --dport 8118 --tcp-flags FIN,SYN,RST,ACK SYN -j ACCEPT -m comment --comment \"Local traffic to Polipo\"",
                "-I OUTPUT 3 -d 127.0.0.1/32 -p tcp -m tcp --dport 9040 --tcp-flags FIN,SYN,RST,ACK SYN -j ACCEPT -m comment --comment \"Local traffic to TransPort\"",
                "-I OUTPUT 3 -d 127.0.0.1/32 -p tcp -m tcp --dport 9050 --tcp-flags FIN,SYN,RST,ACK SYN -j ACCEPT -m comment --comment \"Local traffic to SOCKSPort\"",
                // Remove the first reject we installed with the init-script
                "-D OUTPUT -j REJECT",
                // This will *break* quota management. But we have no choice, the POLICY is bypassed by quota chains :(.
                "-I OUTPUT 7 -j REJECT",
        };
        for (String rule : rules) {
            if (!iptRules.genericRule(rule)) {
                Log.e(InitializeIptables.class.getName(), "Unable to initialize");
                Log.e(InitializeIptables.class.getName(), rule);
            }
        }
    }

    public void installInitScript(Context context) {

        if (!installBinary(context, R.raw.userinit, "userinit.sh")) {
            Log.d("Init", "We're fucked… unable to extract userinit.sh script");
        }

        final String src_file = new File(context.getDir("bin", 0), "userinit.sh").getAbsolutePath();

        CheckSum check_src = new CheckSum(src_file);
        CheckSum check_dst = new CheckSum(dst_file);

        if (!check_dst.hash().equals(check_src.hash())) {

            String CMD = String.format("cp %s %s", src_file, dst_file);
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
    }

    public void removeIniScript(Context context) {
        String CMD = String.format("rm -f %s", dst_file);
        if (shell.suExec(CMD)) {
            Log.d("Init", "file removed");
        } else {
            Log.e("Init", "ERROR while removing file");
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
     *                              <p/>
     *                              Thanks AFWall source code
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
