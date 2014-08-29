package org.ethack.orwall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import org.ethack.orwall.R;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;
import org.sufficientlysecure.rootcommands.RootCommands;

import info.guardianproject.onionkit.ui.OrbotHelper;

public class DialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeIptables initializeIptables = new InitializeIptables(this);
        Intent in = new Intent();
        String TAG = "DialogActivity";

        OrbotHelper orbotHelper = new OrbotHelper(this);

        if (!initializeIptables.iptablesExists()) {
            Log.e(TAG, "No iptables found at " + Constants.IPTABLES);
            setResult(2, in);
            finish();
        } else  if (!RootCommands.rootAccessGiven()) {
            Log.e(TAG, "No root access");
            setResult(3, in);
            finish();
        }
        initializeIptables.supportComments();
        if (!orbotHelper.isOrbotInstalled()) {
            Log.e(TAG, "No orbot installed");
            setResult(4, in);
            finish();
        } else if (orbotHelper.isOrbotInstalled() && !orbotHelper.isOrbotRunning()) {
            Log.e(TAG, "Orbot is down");
            setResult(5, in);
            finish();
        }
        setResult(1, in);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
}
