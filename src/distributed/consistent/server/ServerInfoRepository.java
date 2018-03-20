package distributed.consistent.server;


import java.io.IOException;
import java.util.*;


public class ServerInfoRepository {

    private static ServerInfoRepository repository;
    private boolean isLeader;
    private boolean hasRegistered;
    private ServerInfo ownInfo;
    private boolean isLocked;
    private HashSet<ServerInfo> connectedServerList;
    private ServerInfo leaderInfo;

    private ServerInfoRepository() {
        hasRegistered = false;
        connectedServerList = new HashSet<>();
        try {
            ConfigManager configManager = ConfigManager.create();
            leaderInfo = new ServerInfo(configManager.getValue(ConfigManager.LEADER_IP_ADDRESS),
                    configManager.getIntegerValue(ConfigManager.LEADER_PORT_NUMBER),
                    configManager.getValue(ConfigManager.LEADER_BINDING_NAME));
        } catch (IOException e) {
            System.out.println("Unable to find Config file in ServerInfoRepository");
            System.exit(1);
        }
    }

    public static ServerInfoRepository create() {
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

        if (this.isLeader) {
            ServerInfo newServer = new ServerInfo(ip, port, configManager.getValue(ConfigManager.RMI_BINDING_NAME));
            connectedServerList.add(newServer);
        }
    }

    public void addServerAddress(String ip, int port, String bindingName) throws Exception {
        if (!hasRegistered) throw new Exception("Please register first by calling register()");
        if (!isLeader) throw new Exception("Only supported for Leader");
        ServerInfo newServer = new ServerInfo(ip, port, bindingName);
        connectedServerList.add(newServer);
    }

    public void removeServerAddress(String ip, int port, String bindingName) throws Exception {
        if (!hasRegistered) throw new Exception("Please register first by calling register()");
        if (!isLeader) throw new Exception("Only supported for Leader");
        ServerInfo newServer = new ServerInfo(ip, port, bindingName);
        connectedServerList.remove(newServer);
    }

    public boolean isLeader() throws Exception {
        if (!hasRegistered) throw new Exception("Please register first by calling register()");
        return isLeader;
    }

    public boolean getLockStatus()throws Exception{
        return isLocked;
    }

    public void unLock() {
        isLocked = false;
    }

    public synchronized boolean lock() {
        if(!isLocked){
            isLocked = true;
            return true;
        }
        else
            return false;

    }
    public ServerInfo getOwnInfo() {
        return ownInfo;
    }

    public ArrayList<ServerInfo> getConnectedServerList() {
        return new ArrayList<>(this.connectedServerList);
    }

    public ServerInfo getLeaderInfo() {
        return leaderInfo;
    }

    public void setLeaderInfo(ServerInfo leaderInfo) {
        this.leaderInfo = leaderInfo;
    }
}
