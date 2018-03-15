package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.server.interfaces.IClientServerCommunication;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

    private static Registry registry;

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
                repository.getOwnInfo().getPort());
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
            String ip = getIP(); //get the ip from machine
            int port = parseAndGetPortNumber(args); // get port number from args

            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create(); //create a repository to deal with server details
            serverInfoRepository.register(ip, port); //register it self. It will just add itself to repository. It's like a place to query about server.

            if (!serverInfoRepository.isLeader()) {
                // if not a leader, join Main Server. Main server will add these other servers to a hashset inside ServerRepository class.
                joinMainServer(serverInfoRepository);

            } else {
                System.out.println("I AM THE BOSS!!");
            }

            // Initiate database with no primary key if false, with autoinc primary key otherwise.
            // this will create database if not exists. So we are okay.
            Utility utility = new Utility();
            String dbpath = utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort());
            ArticleRepository articleRepository = new ArticleRepository(dbpath);
            articleRepository.InitiateDatabase(serverInfoRepository.isLeader());

            // start rmiregistry
            registry = LocateRegistry.createRegistry(port);

            // start interserver RMI
            IInterServerCommunication stub = new InterServerCommunication();
            Naming.rebind(getRMIEndpoint(ip, port, ConfigManager.create().getValue(ConfigManager.RMI_BINDING_NAME)), stub);

            // start client communication RMI
            IClientServerCommunication stub2 = new ClientServerCommunication();
            Naming.rebind(getRMIEndpoint(ip, port,
                    ConfigManager.create().getValue(ConfigManager.RMI_BINDING_NAME) + "client"),
                    stub2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
