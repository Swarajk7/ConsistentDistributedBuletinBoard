package distributed.consistent.sync;

import distributed.consistent.model.ServerInfoWithMaxId;
import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.ServerInfo;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class Synchronizer {
    private static final int MILLISECS_IN_SEC = 1000;
    public static void main(String[] args) {
        while (true) {
            try {
                SyncHelper.sync();
                ConfigManager configManager = ConfigManager.create();
                Thread.sleep(configManager.getIntegerValue(ConfigManager.SYNC_SLEEP_TIME_IN_SECONDS) * MILLISECS_IN_SEC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
