package example;

import mindustry.core.NetClient;
import mindustry.core.Platform;
import mindustry.net.Net;

public class BVars {
    public static int port = 6571;
    public static String ip = "121.127.37.17";
    // region packet send
    public static Platform platform = new Platform() {};
    public static Net.NetProvider ale = platform.getNet();
    public static Net net2 = new Net(ale);
    // public static NetClient client = new NetClient();
}
