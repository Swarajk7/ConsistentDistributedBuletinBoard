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

    public static void sync() throws Exception {
        List<ServerInfo> connectedServers = getLeaderRMIStub().getConnectedServers();
        ArrayList<ServerInfoWithMaxId> serverInfoWithMaxIdArrayList = new ArrayList<>();
        for (ServerInfo serverInfo : connectedServers) {
            String serverEndPoint = "rmi://" + serverInfo.getIp()
                    + ":" + serverInfo.getPort() + "/" +
                    serverInfo.getBindingname();
            IInterServerCommunication stub = (IInterServerCommunication) Naming.lookup(serverEndPoint);
            //System.out.println(serverInfo.getPort() + ":" + stub.findMaxId());
            serverInfoWithMaxIdArrayList.add(new ServerInfoWithMaxId(serverInfo.getIp(), serverInfo.getPort(),
                    serverInfo.getBindingname(), stub.findMaxId()));
        }
        getLeaderRMIStub().UpdateQuorumMembers(serverInfoWithMaxIdArrayList);
    }
}