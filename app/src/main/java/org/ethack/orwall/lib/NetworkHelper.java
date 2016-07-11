package org.ethack.orwall.lib;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

/**
 * Small helper in order to get some network information
 */
public class NetworkHelper {

    private static String TAG = "NetworkHelper";

    public NetworkHelper() {
    }

    /**
     * Tries to detect if we're sharing the connection or not.
     * It's not that easy, as it seems there is no simple API to call for that :(.
     *
     * @param context Context in order to get ConnectivityManager
     * @return boolean (true if connection is shared)
     */
    public static boolean isTether(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Method[] methods = connectivityManager.getClass().getDeclaredMethods();
        String[] tethered = {};
        for (Method method : methods) {
            if (method.getName().equals("getTetheredIfaces")) {
                try {
                    tethered = (String[]) method.invoke(connectivityManager);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IllegalAccessException e) {
                    Log.e(TAG, e.getMessage());
                } catch (InvocationTargetException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return (tethered.length != 0);
    }

    private int netmaskToCIDR(int netmask){
        switch (netmask){
            case 0x80000000: return 1;
            case 0xC0000000: return 2;
            case 0xE0000000: return 3;
            case 0xF0000000: return 4;
            case 0xF8000000: return 5;
            case 0xFC000000: return 6;
            case 0xFE000000: return 7;
            case 0xFF000000: return 8;
            case 0xFF800000: return 9;
            case 0xFFC00000: return 10;
            case 0xFFE00000: return 11;
            case 0xFFF00000: return 12;
            case 0xFFF80000: return 13;
            case 0xFFFC0000: return 14;
            case 0xFFFE0000: return 15;
            case 0xFFFF0000: return 16;
            case 0xFFFF8000: return 17;
            case 0xFFFFC000: return 18;
            case 0xFFFFE000: return 19;
            case 0xFFFFF000: return 20;
            case 0xFFFFF800: return 21;
            case 0xFFFFFC00: return 22;
            case 0xFFFFFE00: return 23;
            case 0xFFFFFF00: return 24;
            case 0xFFFFFF80: return 25;
            case 0xFFFFFFC0: return 26;
            case 0xFFFFFFE0: return 27;
            case 0xFFFFFFF0: return 28;
            case 0xFFFFFFF8: return 29;
            case 0xFFFFFFFC: return 30;
            case 0xFFFFFFFE: return 31;
            case 0xFFFFFFFF: return 32;
            default:
                return 0;
        }
    }

    private String getNetwork(DhcpInfo dhcp){
        int ip = dhcp.ipAddress;
        int mask = dhcp.netmask;
        if (ip == 0 || mask == 0) return null;

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
            mask = Integer.reverseBytes(mask);
        }

        ip &= mask;
        mask = netmaskToCIDR(mask);
        if (mask == 0) return null;

        int a = (ip >> 24) & 0xFF;
        int b = (ip >> 16) & 0xFF;
        int c = (ip >>  8) & 0xFF;
        int d = ip & 0xFF;

        return String.format("%d.%d.%d.%d/%d", a, b, c, d, mask);
    }

    /**
     * Provide a simple way to get subnet
     *
     * @param context  Context in order to get WifiManager
     * @return subnet as a String
     */

    public String getSubnet(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return getNetwork(wifiManager.getDhcpInfo());
    }
}
