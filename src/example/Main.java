package example;

import arc.math.Rand;
import arc.util.Log;
import arc.struct.*;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.net.*;
import static mindustry.Vars.*;

import static example.BVars.*;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        net = net2;
        if(args != null && args.length > 0) {
            for (String arg : args) {
                Log.info(arg);
            }
        }
        // region packet
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
        //send(c, true);
        client.connect("121.127.37.17", 6571);
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

}
