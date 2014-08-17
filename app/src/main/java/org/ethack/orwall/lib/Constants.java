package org.ethack.orwall.lib;

/**
 * Constants used across the code
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
    public final static String PREF_KEY_ENFOCE_INIT = "enforce_init_script";
    public final static String PREF_KEY_DISABLE_INIT = "deactivate_init_script";
    public final static String PREF_POLIPO_PORT = "proxy_polipo";

    public final static String IPTABLES = "/system/bin/iptables";

    public final static String ACTION = "org.ethack.orwall.backgroundProcess.action";
    public final static String ACTION_PORTAL = "org.ethack.orwall.backgroundProcess.action.portal";
    public final static String PARAM_ACTIVATE = "org.ethack.orwall.captive.activate";

    public final static String ACTION_ADD_RULE = "org.ethack.orwall.backgroundProcess.action.addRule";
    public final static String PARAM_APPUID = "org.ethack.orwall.backgroundProcess.action.addRule.appUid";
    public final static String PARAM_APPNAME = "org.ethack.orwall.backgroundProcess.action.addRule.appName";

    public final static String ACTION_TETHER = "org.ethack.orwall.backgroundProcess.action.tethering";
    public final static String PARAM_TETHER_STATUS = "org.ethack.orwall.backgroundProcess.action.tethering.status";

    public final static long ORBOT_SOCKS_PROXY = 9050;
    public final static long ORBOT_TRANSPROXY = 9040;
    public final static long ORBOT_DNS_PROXY = 5400;
    public final static long ORBOT_POLIPO_PROXY = 8118;

    public final static String E_NO_SUCH_FILE = "E_NO_SUCH_FILE";
    public final static String E_NO_SUCH_ALGO = "E_NO_SUCH_ALGO";
}
