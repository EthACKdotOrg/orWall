package org.ethack.orwall.vpn;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.DebugPacket;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Attempt to provide a root-less usage for orWall.
 * This will use VpnService stuff instead of iptables.
 *
 * Rooted device will use iptables, non-rooted this VpnService.
 *
 * The following examples and documentations where used in order to get
 * it more or less working:
 * http://www.thegeekstuff.com/2014/06/android-vpn-service/
 * https://github.com/guardianproject/orbotvpn (Thanks @n8fr8)
 */
public class Service extends VpnService implements Runnable{

    private final static String TAG = "orWallVpnService";

    private final static String orbotAddress = "127.0.0.1";
    private final static int orbotTransProxy = (int)Constants.ORBOT_TRANSPROXY;
    private final static int orbotDNS = (int)Constants.ORBOT_DNS_PROXY;
    private PendingIntent configIntent;

    private Thread thread;
    private ParcelFileDescriptor netInterface;
    private DatagramChannel tunnel = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // cleanup previous connection if any
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(this, TAG);

        thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cleanup thread in order to prevent leaks and bad stuff
        if (thread != null) {
            thread.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public synchronized void run() {
        try {
            Log.i(TAG, "Starting");
            InetSocketAddress server = new InetSocketAddress(orbotAddress, orbotTransProxy);

            run(server);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            try {
                netInterface.close();
            } catch (Exception e1) {
                // ignore that
            }
        }
    }

    private boolean run(InetSocketAddress inetSocketAddress) throws Exception{

        tunnel = DatagramChannel.open();
        DatagramSocket socket = tunnel.socket();

        // Ensure socket is protected
        if (!protect(socket)) {
            throw new IllegalStateException("Cannot protect tunnel");
        }

        tunnel.connect(inetSocketAddress);

        // First attempt: non-blocking mode, though we might require the other one
        // in order to be able to filter per-apps rules
        tunnel.configureBlocking(false);

        // connect
        handshake();

        // Start a new thread
        new Thread() {

            public void run() {

                // Allocate a buffer for one packet
                ByteBuffer packet = ByteBuffer.allocate(32757);

                // Outgoing packets are put in this input stream
                FileInputStream in = new FileInputStream(netInterface.getFileDescriptor());

                // Incoming packets are put in this output stream
                FileOutputStream out = new FileOutputStream(netInterface.getFileDescriptor());

                // set timer in order to get information on the tunnel state
                int timer = 0;

                // loop for ever. At least until something bad happens.
                while (true) {
                    try {
                        // describe state
                        boolean idle = true;

                        // read outgoing packets
                        int length = in.read(packet.array());
                        if (length>0) {
                            // write the outgoing packet
                            packet.limit(length);
                            tunnel.write(packet);
                            packet.clear();

                            // maybe some other packets will go out?
                            idle = false;

                            // if we were receiving, switch to send
                            if (timer < 1) {
                                timer = 1;
                            }
                        }

                        // read incoming packets
                        length = tunnel.read(packet);
                        if (length > 0) {
                            out.write(packet.array(), 0, length);
                            packet.clear();

                            idle = false;

                            // if we were sending, switch to receiving
                            if (timer > 0) {
                                timer = 0;
                            }
                        }

                        // If idle or waiting
                        if(idle) {
                            Thread.sleep(100);

                            // increase timer in order to allow the thing to switch from send to receive
                            timer += (timer > 0) ? 100 : -100;

                            // we received a lot, without sending anything, thus switch back to sending
                            if (timer < -15000) {
                                timer = 1;
                            }

                            // we sent a lot, without receiving anything, thus switch back to receiving
                            if (timer > 20000) {
                                throw new IllegalStateException("Timed out");
                            }
                        }

                    } catch (Exception e) {
                        Log.d(TAG, e.toString(), e);
                    }
                }
            }
        }.start();

        return true;
    }

    /**
     * Build the VpnService connection
     */
    public void handshake() {
        if (netInterface == null) {
            Builder builder = new Builder();

            builder.setMtu(1500);
            // Tor internal subnet
            builder.addAddress("10.0.2.0", 24);
            builder.setSession(TAG);
            builder.addRoute("0.0.0.0", 0);
            // returns "Not a numeric address: 127.0.0.1:5400" — maybe using some other format.
            // builder.addDnsServer(orbotAddress + ":" + orbotDNS);

            // ensure we do not already have an opened interface
            try {
                netInterface.close();
            } catch (Exception e) {
                // ignore
            }

            // create the new network interface with vpn
            netInterface = builder.setSession(TAG)
                    .setConfigureIntent(configIntent)
                    .establish();
        }
    }

}
