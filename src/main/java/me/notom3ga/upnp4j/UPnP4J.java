package me.notom3ga.upnp4j;

import me.notom3ga.upnp4j.gateway.Gateway;
import me.notom3ga.upnp4j.gateway.GatewayFinder;
import me.notom3ga.upnp4j.util.Protocol;

public class UPnP4J {
    private static Gateway gateway = null;
    private static final GatewayFinder finder = new GatewayFinder() {
        @Override
        public void onGatewayFind(Gateway gateway) {
            synchronized (finder) {
                if (gateway != null) {
                    UPnP4J.gateway = gateway;
                }
            }
        }
    };

    public static void initialize() {
        while (finder.isSearching()) {}
    }

    public static boolean isUPnPAvailable() {
        initialize();
        return gateway != null;
    }

    public static String getExternalIP() {
        return gateway.getExternalIP();
    }

    public static String getLocalIP() {
        return gateway.getLocalIP();
    }

    public static boolean open(int port, Protocol protocol) {
        return gateway.openPort(port, protocol);
    }

    public static boolean isOpen(int port, Protocol protocol) {
        return gateway.isOpen(port, protocol);
    }

    public static boolean close(int port, Protocol protocol) {
        return gateway.closePort(port, protocol);
    }
}
