package me.notom3ga.upnp4j.util;

public enum Protocol {
    TCP("TCP"),
    UDP("UDP");

    private final String raw;

    Protocol(String raw) {
        this.raw = raw;
    }

    public String raw() {
        return this.raw;
    }
}
