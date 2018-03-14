package distributed.consistent.server;

import distributed.consistent.Utility;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {

    private static int parseAndGetPortNumber(String[] args) {
        //take port number as command line argument
        //System.out.println(args.length);
        if (args.length != 1) {
            System.out.println("Error.\nUsage java Server port_no");
            System.exit(1);
        }
        int port = -1;
        try {
            //check if port is integer
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
            System.out.println("Invalid Port Number.\nUsage java Client port_no");
            System.exit(1);
        }
        return port;
    }

    private static String getIP() throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Your current IP address : " + ip);
        return ip;
    }

    private static void joinMainServer(ServerInfoRepository repository) throws Exception {
        ConfigManager clientManager = ConfigManager.create();

        //join the main server by calling appropriate endpoint
        String serverEndPoint = "rmi://" + clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS)
                + ":" + clientManager.getValue(ConfigManager.LEADER_PORT_NUMBER) + "/" +
                clientManager.getValue(ConfigManager.LEADER_BINDING_NAME);
        IInterServerCommunication stub = (IInterServerCommunication) Naming.lookup(serverEndPoint);
        stub.joinMainServer(repository.getOwnInfo().getIp(), clientManager.getValue(ConfigManager.RMI_BINDING_NAME),
                clientManager.getIntegerValue(ConfigManager.RMI_PORT_NUMBER));
    }

    private static String getRMIEndpoint(String ip, int port, String binding_name) throws IOException {
        ConfigManager configManager = ConfigManager.create();
        Utility utility = new Utility();
        String rmi_end_point = utility.createRMIConnectionString(ip, port,
                binding_name);
        System.out.println("Your current RMI address : " + rmi_end_point);
        return rmi_end_point;
    }
    public static void main(String[] args) {
        try {
            String ip = getIP();
            int port = parseAndGetPortNumber(args);

            ServerInfoRepository serverInfoRepository= ServerInfoRepository.create();
            serverInfoRepository.register(ip,port);
            if (!serverInfoRepository.isLeader()) {
                joinMainServer(serverInfoRepository);
            } else {
                System.out.println("I AM THE BOSS!!");
            }

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            System.out.println("Current relative path is: " + s);

            //LocateRegistry.createRegistry(port);
            InterServerCommunication stub = new InterServerCommunication();
            Naming.rebind(getRMIEndpoint(ip,port,ConfigManager.create().getValue(ConfigManager.RMI_BINDING_NAME)), stub);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
