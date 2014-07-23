package org.ethack.torrific;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.app.Activity;


import org.ethack.torrific.iptables.InitializeIptables;
import org.ethack.torrific.iptables.IptRules;
import org.ethack.torrific.lib.NATLite;
import org.ethack.torrific.lib.NATLiteSource;

import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

public class BootBroadcast extends BroadcastReceiver {
    public BootBroadcast() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        NATLiteSource natLiteSource = new NATLiteSource(context);

        PackageManager packageManager = context.getPackageManager();

        long orbot_real_id = 0;
        try {
            orbot_real_id = packageManager.getApplicationInfo("org.torproject.android", 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(BroadcastReceiver.class.getName(), "Unable to get Orbot real UID — is it still installed?");
            android.os.Process.killProcess(android.os.Process.myPid());
        }


        try {
            natLiteSource.open();
        } catch (SQLException e) {

            Log.e(BootBroadcast.class.getName(), "Unable to open database");

            android.os.Process.killProcess(android.os.Process.myPid());
        }

        InitializeIptables initializeIptables = new InitializeIptables(natLiteSource);
        initializeIptables.initOutputs(orbot_real_id);



        IptRules iptRules = new IptRules();
        List<NATLite> natLites = natLiteSource.getAllNats();
        for (NATLite nat : natLites) {
            Log.d(
                    BootBroadcast.class.getName(),
                    String.format("Applying NAT for %s (%s)",nat.getAppUID(), nat.getAppName())
            );
            iptRules.natApp(nat.getAppUID(), 'A', nat.getAppName());
        }

        Notification.Builder notification = new Notification.Builder(context)
                .setContentTitle("NAT has been applied")
                .setContentText(
                        String.format("You have %d application forced through Orbot", natLites.size())
                );
    }

}
