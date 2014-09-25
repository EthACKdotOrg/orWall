package org.ethack.orwall.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    /**
     * Get device IP, using WifiManager
     * Using this object let us access the IP without requiring INTERNET right.
     * @param context Context in order to get WifiManager
     * @return the IP as a String
     */
    public String getIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address.");
            ipAddressString = null;
        }
        Log.d(TAG, "IPaddress: " + ipAddressString);
        return ipAddressString;
    }

    /**
     * Provide a simple way to get subnet
     * Though it might be a bit stronger, as it fixes /24.
     * @param context Context in order to call getIp()
     * @return subnet as a String
     */
    public String getSubnet(Context context) {
        String ipAddress = getIp(context);
        if (ipAddress != null) {
            String[] st = ipAddress.split("\\.");
            return st[0] + "." + st[1] + "." + st[2] + ".1/24";
        } else {
            return null;
        }
    }
}
