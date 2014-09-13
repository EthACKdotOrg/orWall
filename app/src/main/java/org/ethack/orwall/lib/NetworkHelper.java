package org.ethack.orwall.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Formatter;
import java.util.List;

/**
 * Created by cedric on 8/21/14.
 */
public class NetworkHelper {

    private static String TAG = "NetworkHelper";

    public NetworkHelper() {
    }

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
        Log.d(TAG, "IPaddress: " +ipAddressString);
        return ipAddressString;
    }


    public String getSubnet(Context context) {
        String ipAddress = getIp(context);
        String[] st = ipAddress.split("\\.");
        return st[0] + "." + st[1] + "." + st[2] + ".1/24";
    }

    public static boolean isTether(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Method[] methods = connectivityManager.getClass().getDeclaredMethods();
        String[] tethered = {};
        for (Method method: methods) {
            if (method.getName().equals("getTetheredIfaces")) {
                try {
                    tethered = (String[])method.invoke(connectivityManager);
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
}
