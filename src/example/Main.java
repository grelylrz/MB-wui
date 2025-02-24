package example;

import arc.Core;
import arc.func.Cons;
import arc.math.Rand;
import arc.util.Log;
import arc.struct.*;
import arc.util.Time;
import arc.util.io.ReusableByteInStream;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.core.GameState;
import mindustry.core.Logic;
import mindustry.core.Control;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.net.*;
import static mindustry.Vars.*;
import mindustry.core.NetClient;
import mindustry.logic.*;
import mindustry.type.*;

import java.io.DataInputStream;
import java.util.Locale;
import java.util.zip.InflaterInputStream;

import static example.BVars.*;

public class Main {

    /** List of entities that were removed, and need not be added while syncing. */
    private IntSet removed = new IntSet();
    /** Byte stream for reading in snapshots. */
    private ReusableByteInStream byteStream = new ReusableByteInStream();
    private DataInputStream dataStream = new DataInputStream(byteStream);
    /** Packet handlers for custom types of messages. */
    private ObjectMap<String, Seq<Cons<String>>> customPacketHandlers = new ObjectMap<>();
    /** Packet handlers for custom types of messages, in binary. */
    private ObjectMap<String, Seq<Cons<byte[]>>> customBinaryPacketHandlers = new ObjectMap<>();

    public static void main(String[] args) {
        Vars.loadLogger();
        net = net2;
        NetClient client2 = new NetClient();
        netClient = client2;
        Log.info("Inited");
        /*Vars.logic = new Logic();
        state = new GameState();
        // groups init
        control = new Control();
        // костыль
        MapPreviewLoader.setupLoaders();*/
        if(args != null && args.length > 0) {
            for (String arg : args) {
                Log.info(arg);
            }
        }
        // region packet
        Log.info("generating packet");
        String test = randomString();
        Log.info("Your usid/uuid gen result " + test + " " + test.length());
        if(test.length() > 12 || test.length() < 12) {
            Log.err("Your usid/uuid gen is gen's >/< than 12 symb.");
        }
        var c = new Packets.ConnectPacket();
        c.name = "grely test bot";
        c.locale = Locale.getDefault().toString();;
        c.mods = new Seq<>();
        c.mobile = false;
        c.versionType = "official";
        c.color = 749469439;
        c.usid = randomString();
        c.uuid = randomString();
        // region send
        try {
            ale.connectClient(ip, pport, () -> {
                send(c, true);
                net2.handleClient(Packets.WorldStream.class, data -> {
                    Log.info("Received world data: @ bytes.", data.stream.available());
                    finishConnecting();
                });
            });
        } catch (Exception e) {Log.err("Error while ale.connectClient ", e);}
    }

    public static void send(Object object, boolean reliable){
        Log.info("send used"); // DEBUG
        ale.sendClient(object, reliable);
    }

    public static void confirm() {
        Log.info("Confirming connect");
        ConnectConfirmCallPacket packet = new ConnectConfirmCallPacket();
        send(packet, true);
    }

    public static void disconnect(){
        Log.info("Disconnecting.");
        ale.disconnectClient();
    }

    public static String randomString() {
        byte[] bytes = new byte[8];
        new Rand().nextBytes(bytes);
        String result = new String(Base64Coder.encode(bytes));
        return result;
    }

    private static void finishConnecting(){
        state.set(GameState.State.playing);
        net.setClientLoaded(true);
        confirm();
    }

    // anuke
    public void addPacketHandler(String type, Cons<String> handler){
        customPacketHandlers.get(type, Seq::new).add(handler);
    }

    public Seq<Cons<String>> getPacketHandlers(String type){
        return customPacketHandlers.get(type, Seq::new);
    }

    public void addBinaryPacketHandler(String type, Cons<byte[]> handler){
        customBinaryPacketHandlers.get(type, Seq::new).add(handler);
    }

    public Seq<Cons<byte[]>> getBinaryPacketHandlers(String type){
        return customBinaryPacketHandlers.get(type, Seq::new);
    }
}
