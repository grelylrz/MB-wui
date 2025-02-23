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
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

import static arc.Core.*;

public class Main {
    public static Platform platform = new Platform(){};
    static Net net = new Net(platform.getNet());
    public static void main(String[] args) {
        Log.info("Started!");

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
        net.send(c, true);
    }
}
