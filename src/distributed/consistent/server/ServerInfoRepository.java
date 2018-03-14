package distributed.consistent.server;

import distributed.consistent.Utility;
import javafx.util.Pair;
import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static javax.swing.plaf.synth.ColorType.MAX_COUNT;


public class ServerInfoRepository {

    private static ServerInfoRepository repository;
    private boolean isLeader;
    private boolean hasRegistered;
    private ServerInfo ownInfo;
    private HashSet<ServerInfo> connectedServerList;
    private ServerInfoRepository() {
        hasRegistered = false;
        connectedServerList = new HashSet<>();
    }

    public static ServerInfoRepository create(){
        if (repository == null) repository = new ServerInfoRepository();
        return repository;
    }

    public void register(String ip, int port) throws IOException {
        hasRegistered = true;
        ConfigManager configManager = ConfigManager.create();
        this.ownInfo = new ServerInfo(ip, port,
                configManager.getValue(ConfigManager.RMI_BINDING_NAME));
        this.isLeader = configManager.getValue(ConfigManager.LEADER_IP_ADDRESS).equals(ownInfo.getIp())
                && (configManager.getIntegerValue(ConfigManager.LEADER_PORT_NUMBER) == ownInfo.getPort());
    }

    public void addServerAddress(String ip, int port,String binidingName) throws Exception {
        if(!hasRegistered) throw new Exception("Please register first by calling register()");
        if (!isLeader) throw new Exception("Only supported for Leader");
        ServerInfo newServer = new ServerInfo(ip, port, binidingName);
        connectedServerList.add(newServer);
    }

    public void removeServerAddress(String ip, int port,String binidingName) throws Exception {
        if(!hasRegistered) throw new Exception("Please register first by calling register()");
        if (!isLeader) throw new Exception("Only supported for Leader");
        ServerInfo newServer = new ServerInfo(ip, port, binidingName);
        connectedServerList.remove(newServer);
    }

    public boolean isLeader() throws Exception {
        if(!hasRegistered) throw new Exception("Please register first by calling register()");
        return isLeader;
    }

    public ServerInfo getOwnInfo() {
        return ownInfo;
    }
}
