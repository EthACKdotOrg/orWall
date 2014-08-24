package org.ethack.orwall.lib;

/**
 * Data structure: application NAT rule.
 */
public class AppRule {

    private String appName;
    private Long appUID;
    private String onionType;
    private Long onionPort;
    private String portType;

    public AppRule(String appName, Long appUID, String onionType, Long onionPort, String portType) {
        this.appName = appName;
        this.appUID = appUID;
        this.onionType = onionType;
        this.onionPort = onionPort;
        this.portType = portType;
    }


    public String getAppName() {
        return this.appName;
    }

    public String getOnionType() {
        return this.onionType;
    }

    public String getPortType() {
        return this.portType;
    }

    public Long getAppUID() {
        return this.appUID;
    }

    public Long getOnionPort() {
        return this.onionPort;
    }

}
