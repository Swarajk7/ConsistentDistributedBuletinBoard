package distributed.consistent.server.threads;

import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.ServerInfo;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class CallReplicaServerThread extends Thread implements Runnable {
    private final String content;
    private final ServerInfo serverInfo;
    private final int id;
    private final int parentId;

    public CallReplicaServerThread(ServerInfo serverInfo, int id, String content, int parentId) {
        super();
        this.serverInfo = serverInfo;
        this.id = id;
        this.content = content;
        this.parentId = parentId;
    }

    @Override
    public void run() {
        try {
            // get stub for calling InterServer RMI functions.
            String serverEndPoint = "rmi://" + serverInfo.getIp()
                    + ":" + serverInfo.getPort() + "/" +
                    serverInfo.getBindingname();
            System.out.println(serverEndPoint);
            IInterServerCommunication stub = (IInterServerCommunication) Naming.lookup(serverEndPoint);

            stub.WriteArticleAtReplica(this.id, this.content,this.parentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
