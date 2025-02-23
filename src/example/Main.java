package example;

import arc.Core;
import arc.math.Rand;
import arc.util.Log;
import arc.struct.*;
import arc.util.Time;
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

import java.util.zip.InflaterInputStream;

import static example.BVars.*;

public class Main {
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
        Log.info("generationg packet");
        var c = new Packets.ConnectPacket();
        c.name = "grely test bot";
        c.locale = "ru";
        c.mods = new Seq<>();
        c.mobile = false;
        c.versionType = "official";
        c.color = 1111260159;
        c.usid = "pWx0+DFqzGE=";
        c.uuid = "+nBf/gh4cLM=";
        // region send
        send(c, true);
        //client2.connect("121.127.37.17", 6571);
        net2.handleClient(Packets.WorldStream.class, data -> {
            Log.info("Received world data: @ bytes.", data.stream.available());
            NetworkIO.loadWorld(new InflaterInputStream(data.stream));

            finishConnecting();
        });
    }

    public static void send(Object object, boolean reliable){
        // Log.info("send used"); // DEBUG
        ale.sendClient(object, reliable);
    }

    public static void confirm() {
        Log.info("Confirming connect");
        ConnectConfirmCallPacket packet = new ConnectConfirmCallPacket();
        send(packet, true);
    }

    public void disconnect(){
        Log.info("Disconnecting.");
        ale.disconnectClient();
    }

    public String randomString() {
        byte[] bytes = new byte[8];
        new Rand().nextBytes(bytes);
        String result = new String(Base64Coder.encode(bytes));
        return result;
    }

    private static void finishConnecting(){
        state.set(GameState.State.playing);
        net.setClientLoaded(true);
        Core.app.post(Call::connectConfirm);
    }
}
