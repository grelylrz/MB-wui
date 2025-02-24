package example;

import arc.Core;
import arc.graphics.Color;
import arc.math.Rand;
import arc.net.Client;
import arc.net.NetListener;
import arc.util.Log;
import arc.struct.*;
import arc.util.Threads;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.core.Platform;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.net.Packets.*;

import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;
import java.util.zip.InflaterInputStream;

import mindustry.net.ArcNetProvider.*;
import java.net.*;
import java.util.Locale;
import arc.net.Client.*;
import java.nio.channels.ClosedSelectorException;
import arc.Application;
import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.net.Net;
import mindustry.net.*;
import mindustry.ui.*;
import arc.Core;
import arc.ApplicationListener;

import static mindustry.Vars.*;
import static example.BVars.*;

/*
https://github.com/Kieaer/MindustryBotnet/tree/main/src/main
https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/net/ArcNetProvider.java
https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/net/Net.java
https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/core/NetClient.java
https://github.com/Anuken/Arc/blob/master/extensions/arcnet/src/arc/net/Client.java
 */

public class Main{
    static Platform platform = new Platform() {};
    static Net.NetProvider ale = platform.getNet();
    static Net net2 = new Net(ale);
    static ArcNetProvider p = new ArcNetProvider();
    static Client client;
    private static final TaskQueue runnables = new TaskQueue();
    static String locale = Locale.getDefault().toString();
    private static final Seq<ApplicationListener> listeners = new Seq<>();
    public static void main(String[] args) {
        Vars.loadLogger();
        net = net2;
        Vars.netClient = new NetClient();
        logic = new Logic();
        Groups.init();
        Log.info("Inited");
        if(args != null) {
            for (String arg : args) {
                Log.info(arg);
            }
        }
        // region shiza
        Core.app = new Application() {
            @Override
            public Seq<ApplicationListener> getListeners(){
                return listeners;
            }

            @Override
            public ApplicationType getType() {
                Log.info("GetType used");
                return null;
            }

            @Override
            public String getClipboardText() {
                Log.info("getCLTestUsed used");
                return "";
            }

            @Override
            public void setClipboardText(String s) {
                Log.info("setClText used");
            }

            @Override
            public void post(Runnable runnable){
                Threads.daemon(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        // Log.err(e); // TODO.
                    }
                });
            }

            @Override
            public void exit() {
                Log.info("Exit used");
            }
        };
        // region packet
        client = new Client(8192, 16384, new PacketSerializer());
        Log.info("generating packet");
        String test = randomString();
        Log.info("Your usid/uuid gen result " + test + " " + test.length());
        if(test.length() != 12) {
            Log.err("Your usid/uuid gen is gen's >/< than 12 symb.");
        }
        Log.info("Bot locale " + locale);
        Log.info("Adding handlers...");
        Events.on(EventType.PlayerChatEvent.class, e -> {
            Log.info(e.player.plainName() + " " + e.message); // TODO
        });
        net2.handleClient(Connect.class, packet -> {
            Log.info("Connecting to server: @", packet.addressTCP);
            var c = new Packets.ConnectPacket();

            String uuid = randomString();
            String usid = randomString();
            int color = new Random().nextInt(999999);;
            String name = "grely test bot";

            c.name = name;
            c.locale = locale;
            c.mods = new Seq<>();
            c.mobile = false;
            c.versionType = "official";
            c.color = color;
            c.usid = usid;
            c.uuid = uuid;
            net2.send(c, true);

            Player zz = Player.create();
            zz.name = name;
            zz.locale = locale;
            zz.color.set(color);
            Vars.player = zz;
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
            Log.warn("Con. closed, re-joining");
            main(args);
        });
        net2.handleClient(WorldStream.class, data -> {
            Log.info("Received world data: @ bytes.", data.stream.available());
            NetworkIO.loadWorld(new InflaterInputStream(data.stream));

            finishConnecting();
        });
        net2.handleClient(SendMessageCallPacket.class, data -> {
            Log.info("Message packet!");
            Log.info(data.message);
        });
        net2.handleClient(SendChatMessageCallPacket.class, data -> {
            Log.info("Chat packet!");
        });
        net2.handleClient(SendMessageCallPacket2.class, data -> {
            Log.info("Message packet2!");
            if(data.playersender != null) {
                Log.info(data.playersender.name + ": " + data.message + "(" + data.unformatted + ")");
            } else {
                Log.info(data.message + "(" + data.unformatted + ")");
            }
        });
        Log.info("Handlers added");
        try {
            Log.info("Trying to connect...");
            net2.connect(ip, pport, () -> {
                Log.info("Connecting to " + ip + ":" + pport);
            });
        } catch (Exception e) {
            Log.err("Error!", e);
        }

        // region shiza2
        Timer.schedule(() -> {
            Log.info("finishing connect.");
            finishConnecting();
        }, 5);
        Timer.schedule(() -> {
            message("test");
        }, 0, 5);
        while (true) {} // for stupid reasons
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
        connectConfirmm();
        net2.setClientLoaded(true);
    }

    public static void message(String message){
        SendChatMessageCallPacket packet = new SendChatMessageCallPacket();
        packet.message = message;
        net2.send(packet, true);
    }


}
