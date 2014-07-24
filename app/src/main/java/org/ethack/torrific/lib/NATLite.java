package org.ethack.torrific.lib;

/**
 * Tuple composed by appUID and appName
 */
public class NATLite {
    private long appUID;
    private String appName;

    /**
     * Getter for appUID
     *
     * @return long appUID
     */
    public long getAppUID() {
        return appUID;
    }

    /**
     * Setter for appUID
     *
     * @param appUID
     */
    public void setAppUID(long appUID) {
        this.appUID = appUID;
    }

    /**
     * Getter for appName
     *
     * @return String appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Setter for appName
     *
     * @param appName
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Override toString() method
     *
     * @return String appName
     */
    @Override
    public String toString() {
        return appName;
    }
}
