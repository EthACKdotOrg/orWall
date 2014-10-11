package org.ethack.orwall.lib;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Provide a simple way to debug packet received or sent through VPN Interface.
 * Thanks a lot to @n8fr8 for his code on orbotVPN ;)
 *
 * Also, some documentations in here:
 * https://stackoverflow.com/questions/20237743/android-firewall-with-vpnservice
 * https://stackoverflow.com/questions/17766405/android-vpnservice-to-capture-packets-wont-capture-packets
 */
public class DebugPacket {

    private final static String TAG = "DebugPacket";
    private String status = "";
    private int version;
    private int headerLength;
    private int protocol_id;
    private int totalLength;
    private String srcIP;
    private String dstIP;

    public DebugPacket(ByteBuffer packet) {
        int buffer = packet.get();
        version = buffer >> 4;
        headerLength = buffer & 0x0F;
        headerLength *= 4;
        Log.d(TAG, "IP Version:"+version);
        Log.d(TAG, "Header Length:"+headerLength);
        status += "Header Length:"+headerLength;

        buffer = packet.get(); //DSCP + EN
        buffer = packet.getChar(); //Total Length
        totalLength = buffer;
        Log.d(TAG, "Total Length:" + totalLength);

        buffer = packet.getChar(); //Identification
        buffer = packet.getChar(); //Flags + Fragment Offset
        buffer = packet.get(); //Time to Live
        buffer = packet.get(); //Protocol
        protocol_id = buffer;
        Log.d(TAG, "Protocol:"+ protocol_id);
        status += " Protocol:"+ protocol_id;

        buffer = packet.getChar(); //Header checksum

        buffer = packet.get(); //Source IP 1st Octet
        srcIP += buffer;
        srcIP += ".";
        buffer = packet.get(); //Source IP 2nd Octet
        srcIP += buffer;
        srcIP += ".";
        buffer = packet.get(); //Source IP 3rd Octet
        srcIP += buffer;
        srcIP += ".";
        buffer = packet.get(); //Source IP 4th Octet
        srcIP += buffer;
        Log.d(TAG, "Source IP:"+ srcIP);
        status += " Source IP:"+ srcIP;

        buffer = packet.get(); //Destination IP 1st Octet
        dstIP += buffer;
        dstIP += ".";
        buffer = packet.get(); //Destination IP 2nd Octet
        dstIP += buffer;
        dstIP += ".";
        buffer = packet.get(); //Destination IP 3rd Octet
        dstIP += buffer;
        dstIP += ".";
        buffer = packet.get(); //Destination IP 4th Octet
        dstIP += buffer;
        Log.d(TAG, "Destination IP:"+ dstIP);
        status += " Destination IP:"+ dstIP;
    }

    /**
     * Returns some human-readable string in order to describe protocol.
     * Source for listing:
     * https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
     * @param index integer: protocol int value
     * @return either protocol name as a string, or UNKNOWN with protocol number
     */
    public String getProtocolString(int index) {

        HashMap<Integer, String> protos = new HashMap<Integer, String>();
        protos.put(0, "HOPOPT");
        protos.put(1, "ICMP");
        protos.put(6, "TCP");
        protos.put(15, "XNET");
        protos.put(16, "CHAOS");
        protos.put(17, "UDP");
        protos.put(27, "RDP");
        protos.put(41, "IPv6");
        protos.put(43, "IPv6-Route");
        protos.put(44, "IPv6-Frag");
        protos.put(58, "IPv6-ICMP");
        protos.put(59, "IPv6-NoNxt");
        protos.put(60, "IPv6-Opts");
        protos.put(136, "UDPLite");

        if (protos.containsKey(index)) {
            return protos.get(index);
        } else {
            return "UNKNOWN: "+index;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public int getVersion() {
        return this.version;
    }

    public int getHeaderLength() {
        return this.headerLength;
    }

    public int getProtocol_id() {
        return this.protocol_id;
    }

    public int getTotalLength() {
        return this.totalLength;
    }

    public String getSrcIP() {
        return this.srcIP;
    }

    public String getDstIP() {
        return this.dstIP;
    }
}
