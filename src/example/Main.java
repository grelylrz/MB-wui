package example;

import arc.math.Rand;
import arc.net.Client;
import arc.util.Log;
import arc.struct.*;
import arc.util.Threads;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.core.Platform;
import mindustry.gen.ConnectConfirmCallPacket;
import mindustry.net.*;
import mindustry.net.Packets.*;

import java.io.IOException;
import java.util.zip.InflaterInputStream;

import mindustry.net.ArcNetProvider.*;
import java.net.*;
import java.util.Locale;
import arc.net.Client.*;
import java.nio.channels.ClosedSelectorException;

import static mindustry.Vars.*;
import static example.BVars.*;

/*
https://github.com/Kieaer/MindustryBotnet/tree/main/src/main
https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/net/ArcNetProvider.java
https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/net/Net.java
https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/core/NetClient.java
https://github.com/Anuken/Arc/blob/master/extensions/arcnet/src/arc/net/Client.java
 */

public class Main {
    static Platform platform = new Platform() {};
    static Net.NetProvider ale = platform.getNet();
    static Net net2 = new Net(ale);
    static ArcNetProvider p = new ArcNetProvider();
    static Client client;
    public static void main(String[] args) {
        Vars.loadLogger();
        net = net2;
        netClient = new NetClient();
        Log.info("Inited");
        if(args != null) {
            for (String arg : args) {
                Log.info(arg);
            }
        }
        // region packet
        client = new Client(8192, 16384, new PacketSerializer());
        Log.info("generating packet");
        String test = randomString();
        Log.info("Your usid/uuid gen result " + test + " " + test.length());
        if(test.length() != 12) {
            Log.err("Your usid/uuid gen is gen's >/< than 12 symb.");
        }
        String locale = Locale.getDefault().toString();
        Log.info("Bot locale " + locale);
        Log.info("Adding handlers...");
        net2.handleClient(Connect.class, packet -> {
            Log.info("Connecting to server: @", packet.addressTCP);
            var c = new Packets.ConnectPacket();
            c.name = "grely test bot";
            c.locale = locale;
            c.mods = new Seq<>();
            c.mobile = false;
            c.versionType = "official";
            c.color = 749469439;
            c.usid = randomString();
            c.uuid = randomString();
            net2.send(c, true);
        });
        net2.handleClient(Disconnect.class, packet -> {
            if(packet.reason != null){
                switch(packet.reason) {
                    case "closed" -> Log.warn("disconnect.closed");
                    case "timeout" -> Log.warn("disconnect.timeout");
                    default -> Log.warn("disconnect.error");
                }
            } else{
                Log.warn("Connect.closed");
            }
        });
        net2.handleClient(WorldStream.class, data -> {
            Log.info("Received world data: @ bytes.", data.stream.available());
            NetworkIO.loadWorld(new InflaterInputStream(data.stream));

            finishConnecting();
        });
        Log.info("Handlers added");
        try {
            Log.info("Trying to connect...");
            connectClient(ip, pport, () -> {
                Log.info("Connecting to " + ip + ":" + pport);
            });
        } catch (Exception e) {
            Log.err("Error!", e);
        }
    }

    public static String randomString() {
        byte[] bytes = new byte[8];
        new Rand().nextBytes(bytes);
        return new String(Base64Coder.encode(bytes));
    }

    public static void connectConfirmm() {
        ConnectConfirmCallPacket packet = new ConnectConfirmCallPacket();
        net2.send(packet, true);
    }

    public static void finishConnecting(){
        net2.setClientLoaded(true);
        connectConfirmm();
    }

    public static void connectClient(String ip, int port, Runnable success) {
        /*Threads.start(() -> {*/
            try {
                Log.info("Connecting to " + ip + ":" + port);
                client.connect(50000, ip, port, port);
                Threads.daemon("Client Update", () -> {
                    try {
                        while (client.isConnected()) {
                            client.update(500);
                        }
                    } catch (IOException e) {
                        Log.err("Error during client update", e);
                    }
                });
                Log.info("Connected to " + ip + ":" + port);
                success.run();
                Log.info("Your code runned after connection");

            } catch (Exception e) {
                Log.err("Error during connection", e);
            }
        /*});*/
    }
}
