package org.ethack.orwall.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;

/**
 * Created by cedric on 8/21/14.
 */
public class NetworkHelper {

    private NetworkInterface wlan = null;
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_TETHER = 3;
    public static int TYPE_OTHER = 0;

    public NetworkHelper() {
        try {
            this.wlan = NetworkInterface.getByName("wlan0");
        } catch (SocketException e) {
            Log.e("enableTethering", e.toString());
        }
    }

    public String getSubnet() {
        List<InterfaceAddress> addresses = this.wlan.getInterfaceAddresses();
        InterfaceAddress address = (InterfaceAddress) addresses.toArray()[1];
        String ipv4 = address.getAddress().getHostAddress();
        String netmask = Short.toString(address.getNetworkPrefixLength());

        String st[] = ipv4.split("\\.");
        String subnet = st[0] + "." + st[1] + "." + st[2] + ".0/" + netmask;
        return subnet;
    }

    public final NetworkInterface getWlan() {
        return this.wlan;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_DUN) {
                return TYPE_TETHER;
            }
        }
        return TYPE_OTHER;
    }
}
