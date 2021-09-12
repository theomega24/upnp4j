# upnp4j
A simple UPnP library for Java


## Maven/Gradle
You can download upnp4j from maven central
under the group `dev.omega24`, artifact `upnp4j` and version `1.0`.

## Usage
To open port 25565 with TCP use:
```java
UPnP4J.open(25565, Protocol.TCP);
```

## Inspiration
upnp4j is a continuation of [WaifUPnP](https://github.com/adolfintel/WaifUPnP),
which is licensed under LGPL 2.1.
