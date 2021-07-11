# upnp4j
A simple UPnP library for Java


## Maven/Gradle
You can download upnp4j from my maven repo: `https://notom3ga.me/repo`
under the group `me.notom3ga`, artifact `upnp4j` and version `1.0`.

## Usage
To open port 25565 with TCP use:
```java
UPnP4J.open(25565, Protocol.TCP);
```

## Inspiration
upnp4j is a continuation of [WaifUPnP](https://github.com/adolfintel/WaifUPnP),
which is licensed under LGPL 2.1.
