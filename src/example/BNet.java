package example;

import arc.util.Log;
import mindustry.net.Net;

public class BNet extends Net {
    public BNet(NetProvider provider) {
        super(provider);
    }
    @Override
    public void handleException(Throwable e) {
        if(!e.getMessage().contains("ui") && !e.getMessage().contains("TextFormatter") && !e.getMessage().contains("renderer"))
            Log.err(e); // TODO.
    }
    @Override
    public void showError(Throwable e){
        if(!e.getMessage().contains("ui") && !e.getMessage().contains("TextFormatter") && !e.getMessage().contains("renderer"))
            Log.err(e); // TODO.
    }
}
