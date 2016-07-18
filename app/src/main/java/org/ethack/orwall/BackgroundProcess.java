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
                String onionType = workIntent.getStringExtra(Constants.PARAM_ONIONTYPE);
                Boolean localHost = workIntent.getBooleanExtra(Constants.PARAM_LOCALHOST, false);
                Boolean localNetwork = workIntent.getBooleanExtra(Constants.PARAM_LOCALNETWORK, false);
                addRule(appUID, appName, onionType, localHost, localNetwork);

            } else if (action.equals(Constants.ACTION_RM_RULE)) {
                long appUID = workIntent.getLongExtra(Constants.PARAM_APPUID, 0);
                String appName = workIntent.getStringExtra(Constants.PARAM_APPNAME);
                String onionType = workIntent.getStringExtra(Constants.PARAM_ONIONTYPE);
                Boolean localHost = workIntent.getBooleanExtra(Constants.PARAM_LOCALHOST, false);
                Boolean localNetwork = workIntent.getBooleanExtra(Constants.PARAM_LOCALNETWORK, false);
                rmRule(appUID, appName, onionType, localHost, localNetwork);

            } else if (action.equals(Constants.ACTION_DISABLE_ORWALL)) {
                this.initializeIptables.deactivate();
                this.initializeIptables.deactivateV6();
                
            } else if (action.equals(Constants.ACTION_ENABLE_ORWALL)) {
                this.initializeIptables.boot();
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

    private void addRule(Long appUID, String appName, String onionType, Boolean localHost, Boolean localNetwork) {

        if (onionType.equals(Constants.DB_ONION_TYPE_TOR)) {
            this.iptRules.natApp(this, appUID, 'A', appName);
        } else
        if (onionType.equals(Constants.DB_ONION_TYPE_BYPASS)) {
            iptRules.bypass(appUID, appName, true);
        }

        if (localHost) {
            iptRules.localHost(appUID, appName, true);
        }

        if (localNetwork) {
            iptRules.localNetwork(appUID, appName, true);
        }
    }

    private void rmRule(Long appUID, String appName, String onionType, Boolean localHost, Boolean localNetwork) {
        if (onionType.equals(Constants.DB_ONION_TYPE_TOR)) {
            this.iptRules.natApp(this, appUID, 'D', appName);
        } else
        if (onionType.equals(Constants.DB_ONION_TYPE_BYPASS)) {
            iptRules.bypass(appUID, appName, false);
        }

        if (localHost) {
            iptRules.localHost(appUID, appName, false);
        }

        if (localNetwork) {
            iptRules.localNetwork(appUID, appName, false);
        }
    }
}
