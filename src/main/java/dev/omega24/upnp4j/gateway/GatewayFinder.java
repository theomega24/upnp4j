package dev.omega24.upnp4j.gateway;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Searches for a gateway to connect to
 */
public abstract class GatewayFinder {
    private static final String[] SEARCH_MESSAGES;

    static {
        LinkedList<String> list = new LinkedList<>();
        for (String type : new String[]{"urn:schemas-upnp-org:device:InternetGatewayDevice:1",
                "urn:schemas-upnp-org:service:WANIPConnection:1",
                "urn:schemas-upnp-org:service:WANPPPConnection:1"}) {
            list.add("M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nST: " + type + "\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\n\r\n");
        }
        SEARCH_MESSAGES = list.toArray(String[]::new);
    }

    private static Inet4Address[] getLocalIPs() {
        LinkedList<Inet4Address> ips = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual() || networkInterface.isPointToPoint()) {
                        continue;
                    }

                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (address instanceof Inet4Address) {
                            ips.add((Inet4Address) address);
                        }
                    }
                } catch (SocketException ignore) {
                }
            }
        } catch (SocketException ignore) {
        }
        return ips.toArray(Inet4Address[]::new);
    }

    private final LinkedList<GatewayListener> listeners = new LinkedList<>();

    /**
     * Create a new {@link GatewayFinder}
     */
    public GatewayFinder() {
        for (Inet4Address ip : getLocalIPs()) {
            for (String request : SEARCH_MESSAGES) {
                GatewayListener listener = new GatewayListener(ip, request);
                listener.start();
                listeners.add(listener);
            }
        }
    }

    /**
     * Checks if we're still searching for a gateway
     *
     * @return True if we're still searching for a gateway
     */
    public boolean isSearching() {
        for (GatewayListener listener : listeners) {
            if (listener.isAlive()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called when a gateway is found
     *
     * @param gateway The gateway that was found
     */
    public abstract void onGatewayFind(Gateway gateway);

    private class GatewayListener extends Thread {
        private final Inet4Address ip;
        private final String request;

        public GatewayListener(Inet4Address ip, String request) {
            this.setName("UPnP4J - Gateway Listener");
            this.ip = ip;
            this.request = request;
        }

        @Override
        public void run() {
            boolean found = false;
            Gateway gateway = null;

            try {
                byte[] request = this.request.getBytes();
                DatagramSocket socket = new DatagramSocket(new InetSocketAddress(ip, 0));
                socket.send(new DatagramPacket(request, request.length, new InetSocketAddress("239.255.255.250", 1900)));
                socket.setSoTimeout(3000);

                for (;;) {
                    try {
                        DatagramPacket packet = new DatagramPacket(new byte[1536], 1536);
                        socket.receive(packet);

                        gateway = new Gateway(packet.getData(), ip);
                        String externalIp = gateway.getExternalIP();
                        if (externalIp != null && !externalIp.equalsIgnoreCase("0.0.0.0")) {
                            onGatewayFind(gateway);
                            found = true;
                        }
                    } catch (SocketTimeoutException ignore) {
                        break;
                    } catch (Exception ignore) {
                    }
                }
            } catch (IOException ignore) {
            }

            if (!found && gateway != null) {
                onGatewayFind(gateway);
            }
        }
    }
}
