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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;

/**
 * Created by cedric on 8/21/14.
 */
public class NetworkHelper {

    private NetworkInterface wlan = null;
    private static String TAG = "NetworkHelper";

    public NetworkHelper() {
        try {
            this.wlan = NetworkInterface.getByName("wlan0");
        } catch (SocketException e) {
            Log.e(TAG, e.toString());
        }
    }

    public String getSubnet() {
        List<InterfaceAddress> addresses = this.wlan.getInterfaceAddresses();
        InterfaceAddress address = (InterfaceAddress) addresses.toArray()[1];
        String ipv4 = address.getAddress().getHostAddress();
        String netmask = Short.toString(address.getNetworkPrefixLength());

        String st[] = ipv4.split("\\.");
        return st[0] + "." + st[1] + "." + st[2] + ".0/" + netmask;
    }

    public final NetworkInterface getWlan() {
        return this.wlan;
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
