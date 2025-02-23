package example;

import arc.util.Log;
import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import mindustry.Vars;
import mindustry.Vars.*;
import static mindustry.Vars.*;
import mindustry.ai.*;
import mindustry.async.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.editor.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.maps.Map;
import mindustry.maps.*;
import mindustry.mod.*;
import mindustry.net.*;
import mindustry.service.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.meta.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

import static arc.Core.*;

public class Main {
    public static int port = 6571;
    public static String ip = "121.127.37.17";
    public static void main(String[] args) {
        if (args != null)
            Log.info(args);
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
        // endregion

        // region sendPacket
        snedAndListen(c);
    }

    public static Object sendAndListen(Packets.ConnectPacket packet) {
        try (Socket socket = new Socket(ip, port)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(packet);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
