package org.ethack.orwall;

import android.app.IntentService;
import android.content.Intent;

import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.iptables.IptRules;
import org.ethack.orwall.lib.Constants;
import org.sufficientlysecure.rootcommands.util.Log;

/**
 * Allows to run background commands in order to avoid any blocking stuff in main thread.
 */
public class BackgroundProcess extends IntentService {

    private InitializeIptables initializeIptables;
    private IptRules iptRules;

    public BackgroundProcess() {
        super("BackgroundProcess");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        boolean supportComment = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.CONFIG_IPT_SUPPORTS_COMMENTS, false);
        this.initializeIptables = new InitializeIptables(this);
        this.iptRules = new IptRules(supportComment);

        String action = workIntent.getStringExtra(Constants.ACTION);

        if (action != null) {
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

            } else if (action.equals(Constants.ACTION_DISABLE_ORWALL)) {
                this.initializeIptables.deactivate();

            } else if (action.equals(Constants.ACTION_ENABLE_ORWALL)) {
                this.initializeIptables.boot();

            } else if (action.equals(Constants.ACTION_RM_BYPASS) || action.equals(Constants.ACTION_ADD_BYPASS)) {
                String appName = workIntent.getStringExtra(Constants.PARAM_APPNAME);
                long appUID = workIntent.getLongExtra(Constants.PARAM_APPUID, 0);
                iptRules.bypass(appUID, appName, action.equals(Constants.ACTION_ADD_BYPASS));

            } else if (action.equals(Constants.ACTION_RM_FENCED) || action.equals(Constants.ACTION_ADD_FENCED)) {
                String appName = workIntent.getStringExtra(Constants.PARAM_APPNAME);
                long appUID = workIntent.getLongExtra(Constants.PARAM_APPUID, 0);
                iptRules.fenced(appUID, appName, action.equals(Constants.ACTION_ADD_FENCED));
            } else {
                Log.e("BackgroundProcess", "Just got an unknown action!");
            }
        } else {
            Log.e("BackgroundProcess", "Just got an undefined action!");
        }
    }

    private void managePortal(boolean activate) {
        this.initializeIptables.enableCaptiveDetection(activate, this);
    }

    private void addRule(Long appUID, String appName) {
        this.iptRules.natApp(this, appUID, 'A', appName);
    }

    private void rmRule(Long appUID, String appName) {
        this.iptRules.natApp(this, appUID, 'D', appName);
    }

    private void manageTether(boolean activate) {
        this.initializeIptables.enableTethering(activate);
    }
}
