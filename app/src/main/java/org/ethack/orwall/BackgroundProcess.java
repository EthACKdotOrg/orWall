package org.ethack.orwall;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.ethack.orwall.iptables.InitializeIptables;

/**
 * Created by cedric on 7/31/14.
 */
public class BackgroundProcess extends IntentService {

    public static final String ACTION = "org.ethack.orwall.backgroundProcess.action";
    public static final String ACTION_PORTAL = "org.ethack.orwall.backgroundProcess.action.portal";
    public static final String PARAM_ACTIVATE = "org.ethack.orwall.captive.activate";

    public BackgroundProcess() {
        super("BackroundProcess");
    }
    @Override
    protected void onHandleIntent(Intent workIntent) {
        String action = workIntent.getStringExtra(ACTION);

        if (action.equals(ACTION_PORTAL)) {
            boolean activate = workIntent.getBooleanExtra(PARAM_ACTIVATE, false);
            managePortal(activate);
        }
    }

    private void managePortal(boolean activate) {
        InitializeIptables initializeIptables = new InitializeIptables(this);
        initializeIptables.enableCaptiveDetection(activate, this);
    }
}
