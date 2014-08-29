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

public class DialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeIptables initializeIptables = new InitializeIptables(this);
        Intent in = new Intent();

        if (!initializeIptables.iptablesExists()) {
            Log.e("DialogActivity", "No iptables found at " + Constants.IPTABLES);
            setResult(2, in);
            finish();
        } else  if (!RootCommands.rootAccessGiven()) {
            Log.e("DialogActivity", "No root access");
            setResult(3, in);
            finish();
        }
        initializeIptables.supportComments();
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
