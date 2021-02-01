# ngrok4j

[ngrok](https://ngrok.com/) is a command-line tool for creating public URLs to local servers. 

ngrok4j is a Java wrapper around ngrok. You can use it to start ngrok and  create tunnels to local services from your Java code.

## installation

[Install ngrok](https://ngrok.com/download) 

Add this library to your project (TODO: add to a public repo)

## usage

### Create an ngrok instance

```java
NgrokClient client = Ngrok.startClient();
``` 

Multiple instances of ngrok can be started at the same time, if you want.

### Create an ngrok tunnel

```java
TunnelDetails tunnel = client.connect("my tunnel", TunnelProtocol.HTTP, 8080);
System.out.println(tunnel.publicUrl);
``` 

This will print something like: `https://54ef8c5a6043.ngrok.io` - a public URL which will tunnel traffic to `localhost:8080`.

Alternatively builder-pattern:

```java
client.build()
      .http()
      .port(8080)
      .name("my tunnel")
      .connect();
```


### Stopping tunnels

```java
client.disconnect("my tunnel"); // stop one
client.disconnectAll();         // stop all
client.shutdown();              // stop this ngrok process
```

_Note on http tunnels:_ by default `bind_tls` is true, so whenever you use http proto two tunnels are created - http and https. If you disconnect https tunnel, http tunnel remains open.

 