package org.ethack.torrific.lib;

import android.content.Context;
import android.util.Log;

import org.ethack.torrific.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cedric on 7/25/14.
 */
public class InstallScripts extends Thread {
    private final Context context;

    public InstallScripts(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        if (!installBinary(context, R.raw.activate_portal, "activate_portal.sh")) {
            Log.d("Init", "Unable to install activate_portal script");
        }
        if (!installBinary(context, R.raw.deactivate_portal, "deactivate_portal.sh")) {
            Log.d("Init", "Unable to install deactivate_portal script");
        }
        if (!installBinary(context, R.raw.userinit, "userinit.sh")) {
            Log.d("Init", "We're fucked… unable to extract userinit.sh script");
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
            Log.e("InstallScripts", "installBinary failed: " + e.getLocalizedMessage());
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
     * @throws java.io.IOException  on error
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
