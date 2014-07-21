package org.ethack.torrific.lib;

import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by cedric on 7/18/14.
 */
public class Shell {
    /**
     * Check if we can access root.
     * @return true if success
     */
    public boolean checkSu() {
        Log.d(Shell.class.getName(), "Starting");
        if(!suExec("echo foo")) {
            Log.e(Shell.class.getName(),"No root access");
            return false;
        }
        return true;
    }

    /**
     * Execute cmd as root
     * @param cmd
     * @return true if success
     */
    public boolean suExec(String cmd) {

        String prepend = "";

        if(!cmd.startsWith("/system/bin/iptables")) {
            switch (Build.VERSION.SDK_INT) {
                case 16:
                    prepend = "/system/bin/busybox ";
                    break;
                default:
                    prepend = "";
            }
        }
        List<String> stderr = Collections.synchronizedList(new ArrayList<String>());
        List<String> stdout = Collections.synchronizedList(new ArrayList<String>());

        try {
            Process process;
            process = Runtime.getRuntime().exec("su");

            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            StreamBuffer STDOUT = new StreamBuffer(process.getInputStream(), stdout);
            StreamBuffer STDERR = new StreamBuffer(process.getErrorStream(), stderr);

            STDERR.start();
            STDOUT.start();

            STDIN.write((prepend + cmd + "\n").getBytes("UTF-8"));
            STDIN.flush();
            STDIN.write(("exit\n").getBytes("UTF-8"));
            STDIN.flush();

            process.waitFor();

            try {
                STDIN.close();
            } catch (IOException e) {
            }
            STDOUT.join();
            STDERR.join();

            process.destroy();

            for (String err: stderr) {
                Log.d("STDERR", err);
            }

            return (process.exitValue() == 0);

        } catch (IOException e) {
            Log.e(Shell.class.getName(), e.getMessage());
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
