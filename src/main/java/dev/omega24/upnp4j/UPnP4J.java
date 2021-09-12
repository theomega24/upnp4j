package dev.omega24.upnp4j;

import dev.omega24.upnp4j.gateway.Gateway;
import dev.omega24.upnp4j.gateway.GatewayFinder;
import dev.omega24.upnp4j.util.Protocol;

/**
 * The class to manage upnp4j from
 */
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

    /**
     * Initialize a connection to the router
     */
    public static void initialize() {
        while (finder.isSearching()) {}
    }

    /**
     * Checks if UPnP is available for the router
     *
     * @return True if UPnP is available
     */
    public static boolean isUPnPAvailable() {
        initialize();
        return gateway != null;
    }

    /**
     * Gets the external IP of the router
     *
     * @return The external IP of the router
     */
    public static String getExternalIP() {
        return gateway.getExternalIP();
    }

    /**
     * Gets the local IP of the router
     *
     * @return The local IP of the router
     */
    public static String getLocalIP() {
        return gateway.getLocalIP();
    }

    /**
     * Opens a port using UPnP
     *
     * @param port A port in the range of 1-65535
     * @param protocol The protocol to open the port with
     * @return True if the port was able to open
     */
    public static boolean open(int port, Protocol protocol) {
        return gateway.openPort(port, protocol);
    }

    /**
     * Checks if a port is open
     *
     * @param port A port in the range of 1-65535
     * @param protocol The protocol to check the port with
     * @return True if the port is open
     */
    public static boolean isOpen(int port, Protocol protocol) {
        return gateway.isOpen(port, protocol);
    }

    /**
     * Closes a port using UPnP
     *
     * @param port A port in the range of 1-65535
     * @param protocol The protocol to close the port with
     * @return True if the port was able to close
     */
    public static boolean close(int port, Protocol protocol) {
        return gateway.closePort(port, protocol);
    }
}
