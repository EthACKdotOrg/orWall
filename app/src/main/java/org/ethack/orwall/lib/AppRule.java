package org.ethack.orwall.lib;

import android.content.Context;
import android.content.Intent;

import org.ethack.orwall.BackgroundProcess;

import java.util.ArrayList;

/**
 * Data structure: application NAT rule.
 */
public class AppRule {
    private Boolean stored;
    private String pkgName;
    private Long appUID;
    private String onionType;
    private Boolean localHost;
    private Boolean localNetwork;

    // Variables dedicated for ListView
    // We need them for persistence across scroll
    private boolean isChecked;
    private String label;
    private String appName;

    public AppRule(String pkgName, Long appUID, String onionType, Boolean localHost, Boolean localNetwork) {
        this.stored = true;
        this.pkgName = pkgName;
        this.appUID = appUID;
        this.onionType = onionType;
        this.localHost = localHost;
        this.localNetwork = localNetwork;
        // set to a null value - used in AppListAdapter
        this.label = null;
        this.appName = null;
    }

    public AppRule() {
        // Empty constructor in order to use setters.
        this.stored = false;
        this.pkgName = null;
        this.appUID = null;
        this.onionType = Constants.DB_ONION_TYPE_NONE;
        this.localHost = false;
        this.localNetwork = false;
        // set to a null value - used in AppListAdapter
        this.label = null;
        this.appName = null;
    }

    public Boolean isStored(){
        return this.stored;
    }

    public Boolean isEmpty(){
        return !this.localHost && !this.localNetwork && this.onionType.equals(Constants.DB_ONION_TYPE_NONE);
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getOnionType() {
        return this.onionType;
    }

    public String getDisplay(){
        String ret = this.appName;
        ArrayList<String> flags = new ArrayList<>();
        switch (this.onionType) {
            case Constants.DB_ONION_TYPE_NONE:
                break;
            case Constants.DB_ONION_TYPE_BYPASS:
                flags.add("Bypass");
                break;
            case Constants.DB_ONION_TYPE_TOR:
                flags.add("Tor");
                break;
        }
        if (this.localHost) {
            flags.add("Localhost");
        }
        if (this.localNetwork) {
            flags.add("LocalNetwork");
        }

        if (!flags.isEmpty()){
            ret += " (" + flags.get(0);
            for(int i = 1; i < flags.size(); i++){
                ret += " - " + flags.get(i);
            }
            ret += ")";
        }
        return ret;
    }

    public void setOnionType(String onionType) {
        this.onionType = onionType;
    }

    public Boolean getLocalHost() {
        return this.localHost;
    }

    public void setLocalHost(Boolean localHost) {
        this.localHost = localHost;
    }

    public Boolean getLocalNetwork() {
        return this.localNetwork;
    }

    public void setLocalNetwork(Boolean localNetwork) {
        this.localNetwork = localNetwork;
    }

    public Long getAppUID() {
        return this.appUID;
    }

    public void setAppUID(Long appUID) {
        this.appUID = appUID;
    }

    /**
     * Setters and getters dedicated for View — mainly used in AppListAdapter
     * They don't have any incidence on DB content.
     */
    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    private Intent newBackground(Context context, Intent intent){
        Intent bg = (intent == null? new Intent(context, BackgroundProcess.class): intent);
        bg.putExtra(Constants.PARAM_APPUID, getAppUID());
        bg.putExtra(Constants.PARAM_APPNAME, getPkgName());
        bg.putExtra(Constants.PARAM_ONIONTYPE, getOnionType());
        bg.putExtra(Constants.PARAM_LOCALHOST, getLocalHost());
        bg.putExtra(Constants.PARAM_LOCALNETWORK, getLocalNetwork());
        return bg;
    }

    public void install(Context context, Intent intent){
        Intent bg = newBackground(context, intent);
        bg.putExtra(Constants.ACTION, Constants.ACTION_ADD_RULE);
        context.startService(bg);
    }

    public void uninstall(Context context, Intent intent){
        Intent bg = newBackground(context, intent);
        bg.putExtra(Constants.ACTION, Constants.ACTION_RM_RULE);
        context.startService(bg);
    }

    public void install(Context context){
        install(context, null);
    }

    public void uninstall(Context context){
        uninstall(context, null);
    }

}
