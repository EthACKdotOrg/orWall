package org.ethack.orwall.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Set;

public final class Preferences {
    public final static String PREFERENCES = "org.ethack.orwall_preferences";
    public final static String PREF_KEY_FIRST_RUN = "first_run";
    public final static String PREF_KEY_SIP_APP = "sip_app";
    public final static String PREF_KEY_SIP_ENABLED = "sip_enabled";
    public final static String PREF_KEY_SPEC_BROWSER = "browser_app";
    public final static String PREF_KEY_BROWSER_ENABLED = "browser_enabled";
    //public final static String PREF_KEY_IS_TETHER_ENABLED = "is_tether_enabled";
    public final static String PREF_KEY_TETHER_INTFS = "tether_interfaces";
    public final static String PREF_TRANS_PORT = "proxy_transport";
    public final static String PREF_DNS_PORT = "proxy_dns";
    public final static String PREF_KEY_ADB_ENABLED = "enable_adb";
    public final static String PREF_KEY_SSH_ENABLED = "enable_ssh";
    public final static String PREF_KEY_ENFORCE_INIT = "enforce_init_script";
    //public final static String PREF_KEY_DISABLE_INIT = "deactivate_init_script";
    public final static String PREF_KEY_BROWSER_GRACETIME = "browser_gracetime";
    //public final static String PREF_KEY_IPT_SUPPORTS_COMMENTS = "ipt_comments";
    public final static String PREF_KEY_ORWALL_ENABLED = "orwall_enabled";
    public final static String PREF_KEY_CURRENT_SUBNET = "current_subnet";
    public final static String PREF_KEY_HIDE_PRESS_HINT = "hide_press_hint";

    public static long ORBOT_TRANSPROXY = 9040;
    public static long ORBOT_DNS_PROXY = 5400;

    public final static long BROWSER_GRACETIME = 5;

    private static boolean getBoolean(Context context, String key, boolean def){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(key, def);
    }

    private static void putBoolean(Context context, String key, boolean value){
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    private static String getString(Context context, String key, String def){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(key, def);
    }

    private static void setString(Context context, String key, String value){
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    private static Set<String> getStringSet(Context context, String key, Set<String> def){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getStringSet(key, def);
    }

    private static void setStringSet(Context context, String key, Set<String> value){
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putStringSet(key, value).apply();
    }

    public static boolean isFirstRun(Context context){
        return getBoolean(context, PREF_KEY_FIRST_RUN, true);
    }

    public static void setFirstRun(Context context, boolean value){
        putBoolean(context, PREF_KEY_FIRST_RUN, value);
    }

    public static String getSIPApp(Context context){
        return getString(context, PREF_KEY_SIP_APP, "0");
    }

    public static boolean isSIPEnabled(Context context){
        return getBoolean(context, PREF_KEY_SIP_ENABLED, false);
    }

    public static void setSIPEnabled(Context context, boolean value){
        putBoolean(context, PREF_KEY_SIP_ENABLED, value);
    }

    public static String getBrowserApp(Context context){
        return getString(context, PREF_KEY_SPEC_BROWSER, "0");
    }

    public static boolean isBrowserEnabled(Context context){
        return getBoolean(context, PREF_KEY_BROWSER_ENABLED, false);
    }

    public static void setBrowserEnabled(Context context, boolean value){
        putBoolean(context, PREF_KEY_BROWSER_ENABLED, value);
    }

    public static Set<String> getTetherInterfaces(Context context){
        return getStringSet(context, PREF_KEY_TETHER_INTFS, null);
    }

    public static void setTetherInterfaces(Context context, @Nullable Set<String> value){
        setStringSet(context, PREF_KEY_TETHER_INTFS, value);
    }

    public static void cleanIptablesPreferences(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(PREF_KEY_CURRENT_SUBNET).apply();
        sharedPreferences.edit().remove(PREF_KEY_TETHER_INTFS).apply();
    }

    public static String getTransPort(Context context){
        return getString(context, PREF_TRANS_PORT, String.valueOf(ORBOT_TRANSPROXY));
    }

    public static String getDNSPort(Context context){
        return getString(context, PREF_DNS_PORT, String.valueOf(ORBOT_DNS_PROXY));
    }

    public static boolean isADBEnabled(Context context){
        return getBoolean(context, PREF_KEY_ADB_ENABLED, false);
    }

    public static boolean isSSHEnabled(Context context){
        return getBoolean(context, PREF_KEY_SSH_ENABLED, false);
    }

    public static boolean isEnforceInitScript(Context context){
        return getBoolean(context, PREF_KEY_ENFORCE_INIT, true);
    }

    public static void setEnforceInitScript(Context context, boolean value){
        putBoolean(context, PREF_KEY_ENFORCE_INIT, value);
    }

    public static String getBrowserGraceTime(Context context){
        return getString(context, PREF_KEY_BROWSER_GRACETIME, String.valueOf(BROWSER_GRACETIME));
    }

    public static boolean isOrwallEnabled(Context context){
        return getBoolean(context, PREF_KEY_ORWALL_ENABLED, true);
    }

    public static void setOrwallEnabled(Context context, boolean value){
        putBoolean(context, PREF_KEY_ORWALL_ENABLED, value);
    }

    public static String getCurrentSubnet(Context context){
        return getString(context, PREF_KEY_CURRENT_SUBNET, null);
    }

    public static void setCurrentSubnet(Context context, String value){
        setString(context, PREF_KEY_CURRENT_SUBNET, value);
    }

    public static boolean isHidePressHint(Context context){
        return getBoolean(context, PREF_KEY_HIDE_PRESS_HINT, false);
    }

    public static void setHidePressHint(Context context, boolean value){
        putBoolean(context, PREF_KEY_HIDE_PRESS_HINT, value);
    }
}
