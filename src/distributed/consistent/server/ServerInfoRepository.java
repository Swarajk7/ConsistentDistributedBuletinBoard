package distributed.consistent.server;


import java.io.IOException;
import java.util.*;


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

        if (this.isLeader) {
            ServerInfo newServer = new ServerInfo(ip, port, configManager.getValue(ConfigManager.RMI_BINDING_NAME));
            connectedServerList.add(newServer);
        }
    }

    public void addServerAddress(String ip, int port,String bindingName) throws Exception {
        if(!hasRegistered) throw new Exception("Please register first by calling register()");
        if (!isLeader) throw new Exception("Only supported for Leader");
        ServerInfo newServer = new ServerInfo(ip, port, bindingName);
        connectedServerList.add(newServer);
    }

    public void removeServerAddress(String ip, int port,String bindingName) throws Exception {
        if(!hasRegistered) throw new Exception("Please register first by calling register()");
        if (!isLeader) throw new Exception("Only supported for Leader");
        ServerInfo newServer = new ServerInfo(ip, port, bindingName);
        connectedServerList.remove(newServer);
    }

    public boolean isLeader() throws Exception {
        if(!hasRegistered) throw new Exception("Please register first by calling register()");
        return isLeader;
    }

    public ServerInfo getOwnInfo() {
        return ownInfo;
    }

    public ArrayList<ServerInfo> getConnectedServerList() {
        return  new ArrayList<ServerInfo>(this.connectedServerList);
    }
}
