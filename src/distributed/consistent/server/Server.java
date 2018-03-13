package distributed.consistent.server;

import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;

import java.net.InetAddress;
import java.sql.SQLException;

public class Server {

    public static void main(String[] args) {
        try {
            String ip;
            ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Your current IP address : " + ip);

            ServerInfoRepository repository = ServerInfoRepository.create();
            repository.register(ip);

            if(!repository.isLeader()) {
                //join the main server by calling appropriate endpoint
            }

            //start rmi

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
