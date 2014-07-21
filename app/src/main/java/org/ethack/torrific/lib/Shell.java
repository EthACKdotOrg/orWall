package org.ethack.torrific.lib;

import android.os.Build;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

        try {
            Process process;
            process = Runtime.getRuntime().exec("su");

            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            DataInputStream STDOUT = new DataInputStream(process.getInputStream());
            DataInputStream STDERR = new DataInputStream(process.getErrorStream());

            STDIN.writeChars(prepend + cmd + "\nexit\n");
            STDIN.flush();

            if (STDERR.available() != 0) {
                Log.e(Shell.class.getName(), STDERR.readLine());
                STDERR.close();
                STDOUT.close();
                STDIN.close();
                return false;
            }
            STDERR.close();
            STDOUT.close();
            STDIN.close();
            return true;


        } catch (IOException e) {
            Log.e(Shell.class.getName(), e.getMessage());
            return false;
        }
    }
}
