package distributed.consistent.sync;

import distributed.consistent.model.ServerInfoWithMaxId;
import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.ServerInfo;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class SyncHelper {
    private static IInterServerCommunication getLeaderRMIStub() throws Exception {
        ConfigManager clientManager = ConfigManager.create();

        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS)
                + ":" + clientManager.getValue(ConfigManager.LEADER_PORT_NUMBER) + "/" +
                clientManager.getValue(ConfigManager.LEADER_BINDING_NAME);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }
    private static IInterServerCommunication getQuorumLeaderRMIStub(ServerInfo maxIdServerInfo) throws Exception {
        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + maxIdServerInfo.getIp()
                + ":" + maxIdServerInfo.getPort() + "/" +
                maxIdServerInfo.getBindingname();
        //System.out.println(serverEndPoint);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }

    public static void sync() throws Exception {
        List<ServerInfo> connectedServers = getLeaderRMIStub().getConnectedServers();
        ArrayList<ServerInfoWithMaxId> serverInfoWithMaxIdArrayList = new ArrayList<>();
        ServerInfoWithMaxId serverInfoWithMaxIdGlobal = null;
        int maxIdYetSeen = -1;
        for (ServerInfo serverInfo : connectedServers) {
            String serverEndPoint = "rmi://" + serverInfo.getIp()
                    + ":" + serverInfo.getPort() + "/" +
                    serverInfo.getBindingname();
            IInterServerCommunication stub = (IInterServerCommunication) Naming.lookup(serverEndPoint);
            //System.out.println(serverInfo.getPort() + ":" + stub.findMaxId());
            ServerInfoWithMaxId serverInfoWithMaxId = new ServerInfoWithMaxId(serverInfo.getIp(), serverInfo.getPort(),
                    serverInfo.getBindingname(), stub.findMaxId());
            serverInfoWithMaxIdArrayList.add(serverInfoWithMaxId);
            if (serverInfoWithMaxId.getMaximum_id() > maxIdYetSeen) {
                serverInfoWithMaxIdGlobal = serverInfoWithMaxId;
            }
        }
        getQuorumLeaderRMIStub(serverInfoWithMaxIdGlobal.getServerInfo()).
                UpdateQuorumMembers(serverInfoWithMaxIdArrayList);
    }
}
