package org.ethack.orwall.lib;

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
}
