package org.ethack.orwall;

import android.app.IntentService;
import android.content.Intent;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.Constants;

/**
 * Created by cedric on 7/31/14.
 */
public class BackgroundProcess extends IntentService {

    public BackgroundProcess() {
        super("BackroundProcess");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String action = workIntent.getStringExtra(Constants.ACTION);

        if (action.equals(Constants.ACTION_PORTAL)) {
            boolean activate = workIntent.getBooleanExtra(Constants.PARAM_ACTIVATE, false);
            managePortal(activate);
        } else if (action.equals(Constants.ACTION_ADD_RULE)) {
            long appUID = workIntent.getLongExtra(Constants.PARAM_APPUID, 0);
            String appName = workIntent.getStringExtra(Constants.PARAM_APPNAME);
            addRule(appUID, appName);
        } else if (action.equals(Constants.ACTION_RM_RULE)) {
            long appUID = workIntent.getLongExtra(Constants.PARAM_APPUID, 0);
            String appName = workIntent.getStringExtra(Constants.PARAM_APPNAME);
            rmRule(appUID, appName);
        } else if (action.equals(Constants.ACTION_TETHER)) {
            boolean activate = workIntent.getBooleanExtra(Constants.PARAM_TETHER_STATUS, false);
            manageTether(activate);
        }
    }

    private void managePortal(boolean activate) {
        InitializeIptables initializeIptables = new InitializeIptables(this);
        initializeIptables.enableCaptiveDetection(activate, this);
    }

    private void addRule(Long appUID, String appName) {
        IptRules iptRules = new IptRules();
        iptRules.natApp(this, appUID, 'A', appName);
    }

    private void rmRule(Long appUID, String appName) {
        IptRules iptRules = new IptRules();
        iptRules.natApp(this, appUID, 'D', appName);
    }

    private void manageTether(boolean activate) {
        InitializeIptables initializeIptables = new InitializeIptables(this);
        initializeIptables.enableTethering(activate);
    }
}
