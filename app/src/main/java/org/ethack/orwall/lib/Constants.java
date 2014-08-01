package org.ethack.orwall.lib;

/**
 * Created by cedric on 8/1/14.
 */
public class Constants {
    public final static String PREFERENCES = "org.ethack.orwall_preferences";
    public final static String PREF_KEY_SIP_APP = "sip_app";
    public final static String PREF_KEY_SIP_ENABLED = "sip_enabled";
    public final static String PREF_KEY_SPEC_BROWSER = "browser_app";
    public final static String PREF_KEY_BROWSER_ENABLED = "browser_enabled";
    public final static String PREF_KEY_TETHER_ENABLED = "enable_tethering";
    public final static String PREF_KEY_IS_TETHER_ENAVLED = "is_tether_enabled";
    public final static String PREF_TRANS_PORT = "proxy_transport";
    public final static String PREF_DNS_PORT = "proxy_dns";
    public final static String PREF_SOCKS = "proxy_socks";
    public final static String PREF_KEY_ADB_ENABLED = "enable_adb";


    public final static String IPTABLES = "/system/bin/iptables";

    public static final String ACTION = "org.ethack.orwall.backgroundProcess.action";
    public static final String ACTION_PORTAL = "org.ethack.orwall.backgroundProcess.action.portal";
    public static final String PARAM_ACTIVATE = "org.ethack.orwall.captive.activate";
}
