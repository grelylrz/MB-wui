package example;

import arc.*;
import arc.files.Fi;
import arc.math.Rand;
import arc.mock.MockFiles;
import arc.net.Client;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.TaskQueue;
import arc.util.Threads;
import arc.util.Timer;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Universe;
import mindustry.gen.*;
import mindustry.net.ArcNetProvider;
import mindustry.net.ArcNetProvider.PacketSerializer;
import mindustry.net.Net;
import mindustry.net.NetworkIO;
import mindustry.net.Packets;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.Disconnect;
import mindustry.net.Packets.WorldStream;

import java.util.Locale;
import java.util.Random;
import java.util.zip.InflaterInputStream;

import static example.BVars.ip;
import static example.BVars.pport;
import static mindustry.Vars.logic;
import static mindustry.Vars.net;

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
    static Net net2 = new BNet(ale);
    static ArcNetProvider p = new ArcNetProvider();
    static Client client;
    static String locale = Locale.getDefault().toString();
    private static final Seq<ApplicationListener> listeners = new Seq<>();
    static int lastSnapID = 0;
    static boolean join = false;

    public static void main(String[] args) {
        Log.info("loading some basa.");
        Vars.loadLogger();
        Vars.content = new ContentLoader(); // Инициализация контента
        Vars.content.createBaseContent();  // Загрузка базового контента
        Vars.world = new World(); // Инициализация игрового мира
        net = net2;
        Vars.netClient = new NetClient();
        logic = new Logic();
        Groups.init();

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
                Log.info("setClText used, text @", s);
            }

            @Override
            public void post(Runnable runnable){
                //Threads.daemon(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        net.showError(e);
                    }
                //});
            }

            @Override
            public void exit() {
                Log.info("Exit used");
                System.exit(0);
            }
        };
        if (args.length == 2) {
            try {
                BVars.pport = Integer.parseInt(args[1]);
            } catch (Exception e) {
                Log.err("FATAL: Invalid port!");
                System.exit(2);
            }
            BVars.ip = args[0];
            Log.info("Parsed as @:@", BVars.ip, BVars.pport);
        } else {
            Log.err("FATAL: No ip or/and port to join!");
            System.exit(1);
        }
        Core.settings = new Settings();
        Core.files = new MockFiles();
        Vars.dataDirectory = Core.files.local("config");
        Vars.screenshotDirectory = Vars.dataDirectory.child("screenshots/");
        Vars.customMapDirectory = Vars.dataDirectory.child("maps/");
        Vars.mapPreviewDirectory = Vars.dataDirectory.child("previews/");
        Vars.saveDirectory = Vars.dataDirectory.child("saves/");
        Vars.tmpDirectory = Vars.dataDirectory.child("tmp/");
        Vars.modDirectory = Vars.dataDirectory.child("mods/");
        Vars.schematicDirectory = Vars.dataDirectory.child("schematics/");
        Vars.bebuildDirectory = Vars.dataDirectory.child("be_builds/");
        // Vars.emptyMap = new Map(new StringMap());
        Vars.universe = new Universe();
        Log.info("Loaded!");
        // region packet
        client = new Client(8192, 16384, new PacketSerializer());
        Log.info("generating packet");
        String test = randomString();
        Log.info("Your usid/uuid gen result " + test + " " + test.length());
        if(test.length() != 12) {
            Log.err("FATAL: Your usid/uuid gen is gen's >/< than 12 symb.");
            Core.app.exit();
        }
        Log.info("Bot locale " + locale);
        Log.info("Adding handlers...");
        Events.on(EventType.PlayerChatEvent.class, e -> {
            Log.info(e.player.plainName() + " " + e.message); // TODO
        });
        net2.handleClient(Connect.class, packet -> {
            Log.info("Generated packet for: @", packet.addressTCP);
            var c = new Packets.ConnectPacket();

            String uuid = randomString();
            String usid = randomString();
            int color = new Random().nextInt(999999);;
            String name = randomString(6);

            c.name = name;
            c.locale = locale;
            c.mods = new Seq<>();
            c.mobile = Math.random() < 0.5;
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
            System.exit(0);
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
        Vars.state = new GameState();
        Vars.state.set(GameState.State.playing);
        Vars.state.map = null;
        Vars.state.rules = new Rules();
        Log.info("GameState initialized.");
        // region shiza
        Timer.schedule(() -> {
            Log.info("finishing connect manually.");
            finishConnecting();
        }, 2);
        Vars.net.pingHost(ip, pport, host->{
            Log.info("@ @/@", host.name, host.players, host.playerLimit);
        }, e->Log.err(e));
        /*Timer.schedule(() -> {
            Player bot = Groups.player.find(p -> p.plainName().contains("test bot"));
            if(bot != null)
                Log.info(bot.unit().type);
        }, 0, 0.200f);*/
        /*Timer.schedule(()->{
            Vars.world.tiles.eachTile(t->{
                Log.info(t);
            });
        },10);*/
        while (true) {} // for stupid reasons
    }

    public static String randomString() {
        byte[] bytes = new byte[8];
        new Rand().nextBytes(bytes);
        return new String(Base64Coder.encode(bytes));
    }

    public static String randomString(int b) {
        byte[] bytes = new byte[b];
        new Rand().nextBytes(bytes);
        return new String(Base64Coder.encode(bytes));
    }

    public static void connectConfirmm() {
        /*
        * Call.connectConfirm()
        * */
        ConnectConfirmCallPacket packet = new ConnectConfirmCallPacket();
        net2.send(packet, true);
    }

    public static void finishConnecting(){
        connectConfirmm();
        net2.setClientLoaded(true);
        join = true;
    }
}
