package distributed.consistent.server.threads;

import distributed.consistent.server.MultiCastHelper;
import distributed.consistent.server.ServerInfo;
import distributed.consistent.server.ServerInfoRepository;

import java.io.IOException;

public class ServerInfoReceiverThread implements Runnable {
    public ServerInfoReceiverThread() {
        new Thread(this).start();
    }
    @Override
    public void run() {
        while (true) {
            MultiCastHelper multiCastHelper = new MultiCastHelper();
            try {
                ServerInfo serverInfo = multiCastHelper.receive();
                ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
                serverInfoRepository.setLeaderInfo(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
