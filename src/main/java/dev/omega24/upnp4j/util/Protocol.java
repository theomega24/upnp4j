package dev.omega24.upnp4j.util;

/**
 * The protocols upnp4j supports
 */
public enum Protocol {
    /**
     * For the TCP protocol
     */
    TCP("TCP"),
    /**
     * For the UDP protocol
     */
    UDP("UDP");

    private final String raw;

    Protocol(String raw) {
        this.raw = raw;
    }

    /**
     * Gets the raw name for the protocol
     *
     * @return The raw name for the protocol
     */
    public String raw() {
        return this.raw;
    }
}
