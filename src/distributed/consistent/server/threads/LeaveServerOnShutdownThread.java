package distributed.consistent.server.threads;

import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.ServerInfoRepository;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class LeaveServerOnShutdownThread extends Thread implements Runnable {
    private void leaveServer() throws IOException, NotBoundException {
        ServerInfoRepository repository = ServerInfoRepository.create();
        ConfigManager clientManager = ConfigManager.create();

        //join the main server by calling appropriate endpoint
        String serverEndPoint = "rmi://" + clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS)
                + ":" + clientManager.getValue(ConfigManager.LEADER_PORT_NUMBER) + "/" +
                clientManager.getValue(ConfigManager.LEADER_BINDING_NAME);
        IInterServerCommunication stub = (IInterServerCommunication) Naming.lookup(serverEndPoint);
        stub.leaveMainServer(repository.getOwnInfo().getIp(), clientManager.getValue(ConfigManager.RMI_BINDING_NAME),
                repository.getOwnInfo().getPort());
    }
    @Override
    public void run() {
        try {
            leaveServer();
        } catch (Exception ex) {
            System.out.println("Couldn't leave server");
        }
    }
}
