package me.notom3ga.upnp4j.gateway;

import me.notom3ga.upnp4j.util.Protocol;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Gateway {
    private final Inet4Address ip;
    private String serviceType;
    private String controlUrl;

    public Gateway(byte[] data, Inet4Address ip) throws Exception {
        this.ip = ip;
        String location = null;
        StringTokenizer tokenizer = new StringTokenizer(new String(data), "\n");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.isEmpty() || token.startsWith("HTTP/1.") || token.startsWith("NOTIFY *")) {
                continue;
            }

            String name = token.substring(0, token.indexOf(':'));
            String value = token.length() >= name.length() ? token.substring(name.length() + 1).trim() : null;
            if (name.equalsIgnoreCase("location")) {
                location = value;
            }
        }

        if (location == null) {
            throw new Exception("Unsupported gateway, does not support UPnP");
        }

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(location);
        NodeList services = document.getElementsByTagName("service");
        for (int i = 0; i < services.getLength(); i++) {
            Node service = services.item(i);
            NodeList children = service.getChildNodes();

            String serviceType = null;
            String controlUrl = null;
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeName().trim().equalsIgnoreCase("serviceType")) {
                    serviceType = child.getFirstChild().getNodeValue();
                } else if (child.getNodeName().trim().equalsIgnoreCase("controlURL")) {
                    controlUrl = child.getFirstChild().getNodeValue();
                }
            }

            if (serviceType == null || controlUrl == null) {
                continue;
            }

            if (serviceType.trim().toLowerCase().contains(":wanipconnection:") || serviceType.trim().toLowerCase().contains(":wanpppconnection:")) {
                this.serviceType = serviceType.trim();
                this.controlUrl = controlUrl.trim();
            }
        }

        if (controlUrl == null) {
            throw new Exception("Unsupported gateway, does not support UPnP");
        }

        int slash = location.indexOf("/", 7);
        if (slash == -1) {
            throw new Exception("Unsupported gateway, does not support UPnP");
        }
        location = location.substring(0, slash);

        if (!controlUrl.startsWith("/")) {
            controlUrl = "/" + controlUrl;
        }
        controlUrl = location + controlUrl;
    }

    private Map<String, String> command(String action, Map<String, String> params) throws Exception {
        Map<String, String> ret = new HashMap<String, String>();
        String soap = "<?xml version=\"1.0\"?>\r\n" + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<SOAP-ENV:Body>"
                + "<m:" + action + " xmlns:m=\"" + serviceType + "\">";
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                soap += "<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">";
            }
        }
        soap += "</m:" + action + "></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        byte[] req = soap.getBytes();
        HttpURLConnection conn = (HttpURLConnection) new URL(controlUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setRequestProperty("SOAPAction", "\"" + serviceType + "#" + action + "\"");
        conn.setRequestProperty("Connection", "Close");
        conn.setRequestProperty("Content-Length", "" + req.length);
        conn.getOutputStream().write(req);
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conn.getInputStream());
        NodeIterator iter = ((DocumentTraversal) d).createNodeIterator(d.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        Node n;
        while ((n = iter.nextNode()) != null) {
            try {
                if (n.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    ret.put(n.getNodeName(), n.getTextContent());
                }
            } catch (Throwable t) {
            }
        }
        conn.disconnect();
        return ret;
    }

    public String getLocalIP() {
        return ip.getHostAddress();
    }

    public String getExternalIP() {
        try {
            Map<String, String> r = command("GetExternalIPAddress", null);
            return r.get("NewExternalIPAddress");
        } catch (Throwable t) {
            return null;
        }
    }

    public boolean openPort(int port, Protocol protocol) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", protocol.raw());
        params.put("NewInternalClient", ip.getHostAddress());
        params.put("NewExternalPort", "" + port);
        params.put("NewInternalPort", "" + port);
        params.put("NewEnabled", "1");
        params.put("NewPortMappingDescription", "UPnP4J");
        params.put("NewLeaseDuration", "0");
        try {
            Map<String, String> r = command("AddPortMapping", params);
            return r.get("errorCode") == null;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean closePort(int port, Protocol protocol) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", protocol.raw());
        params.put("NewExternalPort", "" + port);
        try {
            command("DeletePortMapping", params);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isOpen(int port, Protocol protocol) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", protocol.raw());
        params.put("NewExternalPort", "" + port);
        try {
            Map<String, String> r = command("GetSpecificPortMappingEntry", params);
            if (r.get("errorCode") != null) {
                throw new Exception();
            }
            return r.get("NewInternalPort") != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
