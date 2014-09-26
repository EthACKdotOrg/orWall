package org.ethack.orwall.lib;

/**
 * Data structure: application NAT rule.
 */
public class AppRule {

    private String pkgName;
    private Long appUID;
    private String onionType;
    private Long onionPort;
    private String portType;

    // Variables dedicated for ListView
    // We need them for persistence across scroll
    private boolean isChecked;
    private String label;
    private String appName;

    public AppRule(String pkgName, Long appUID, String onionType, Long onionPort, String portType) {
        this.pkgName = pkgName;
        this.appUID = appUID;
        this.onionType = onionType;
        this.onionPort = onionPort;
        this.portType = portType;
        // set to a null value - used in AppListAdapter
        this.label = null;
        this.appName = null;
    }

    public AppRule() {
        // Empty constructor in order to use setters.
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

    public void setOnionType(String onionType) {
        this.onionType = onionType;
    }

    public String getPortType() {
        return this.portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }

    public Long getAppUID() {
        return this.appUID;
    }

    public void setAppUID(Long appUID) {
        this.appUID = appUID;
    }

    public Long getOnionPort() {
        return this.onionPort;
    }

    public void setOnionPort(Long onionPort) {
        this.onionPort = onionPort;
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
        return  this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
