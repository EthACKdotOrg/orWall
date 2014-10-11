package org.ethack.orwall.lib;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Provide a simple way to debug packet received or sent through VPN Interface.
 * Thanks a lot to @n8fr8 for his code on orbotVPN ;)
 */
public class DebugPacket {

    private final static String TAG = "DebugPacket";
    private String status = "";
    private int version;
    private int headerLength;

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
        Log.d(TAG, "Total Length:" + buffer);

        buffer = packet.getChar(); //Identification
        buffer = packet.getChar(); //Flags + Fragment Offset
        buffer = packet.get(); //Time to Live
        buffer = packet.get(); //Protocol
        Log.d(TAG, "Protocol:"+buffer);
        status += " Protocol:"+buffer;

        buffer = packet.getChar(); //Header checksum

        String sourceIP = "";
        buffer = packet.get(); //Source IP 1st Octet
        sourceIP += buffer;
        sourceIP += ".";
        buffer = packet.get(); //Source IP 2nd Octet
        sourceIP += buffer;
        sourceIP += ".";
        buffer = packet.get(); //Source IP 3rd Octet
        sourceIP += buffer;
        sourceIP += ".";
        buffer = packet.get(); //Source IP 4th Octet
        sourceIP += buffer;
        Log.d(TAG, "Source IP:"+sourceIP);
        status += " Source IP:"+sourceIP;

        String destIP = "";
        buffer = packet.get(); //Destination IP 1st Octet
        destIP += buffer;
        destIP += ".";
        buffer = packet.get(); //Destination IP 2nd Octet
        destIP += buffer;
        destIP += ".";
        buffer = packet.get(); //Destination IP 3rd Octet
        destIP += buffer;
        destIP += ".";
        buffer = packet.get(); //Destination IP 4th Octet
        destIP += buffer;
        Log.d(TAG, "Destination IP:"+destIP);
        status += " Destination IP:"+destIP;
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
}
