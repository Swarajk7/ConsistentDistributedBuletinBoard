package distributed.consistent.client;

import distributed.consistent.model.Article;
import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.interfaces.IClientServerCommunication;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

public class Client {
    private static IClientServerCommunication getRMIStub(String ip, int port, String bindingname) throws Exception {
        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + ip
                + ":" + port + "/" +
                bindingname;
        return (IClientServerCommunication) Naming.lookup(serverEndPoint);
    }

    public static void main(String args[]) {
        String selectedIP = "10.0.0.210";
        int port = 0;
        String bindingname = "pubsubclient";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.println("Choose an Option? \n1. JOIN A SERVER\n" +
                        "2. CHANGE SERVER \n3. Read An Article \n4. Post An Article\n");
                int readValue = Integer.parseInt(reader.readLine());
                switch (readValue) {
                    case 1:
                        //start ping thread
                        System.out.println("Select a server to connect?");
                        System.out.println("1. 5005");
                        System.out.println("2. 5006");
                        int server = Integer.parseInt(reader.readLine());
                        if (server == 1) {
                            port = 5005;
                        } else port = 5006;
                        break;
                    case 2:
                        System.out.println("Select a server to connect?  ");
                        System.out.println("1. 5005");
                        System.out.println("2. 5006");
                        server = Integer.parseInt(reader.readLine());
                        if (server == 1) {
                            port = 5005;
                        } else port = 5006;
                        break;
                    case 3:
                        System.out.println("Article ID?\n");
                        int id = Integer.parseInt(reader.readLine());
                        Article article = getRMIStub(selectedIP, port, bindingname).readArticle(id);
                        System.out.println(article.getID() + " : " + article.getContent());
                        break;
                    case 4:
                        System.out.println("Article Content?\n");
                        String content = reader.readLine();
                        getRMIStub(selectedIP, port, bindingname).postArticle(content);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
