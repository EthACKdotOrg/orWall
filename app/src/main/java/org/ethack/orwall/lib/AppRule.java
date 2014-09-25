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

    public AppRule() {
        // Empty constructor in order to use setters.
    }


    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

}
