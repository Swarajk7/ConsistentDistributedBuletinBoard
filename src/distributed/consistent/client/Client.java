package distributed.consistent.client;

import distributed.consistent.model.Article;
import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.ServerInfo;
import distributed.consistent.server.interfaces.IClientServerCommunication;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static IClientServerCommunication getRMIStub(String ip, int port, String bindingname) throws Exception {
        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + ip
                + ":" + port + "/" +
                bindingname;
        return (IClientServerCommunication) Naming.lookup(serverEndPoint);
    }

    public static void main(String args[]) {

        try {
            ConfigManager clientManager = ConfigManager.create();

            //join the main server by calling appropriate endpoint

            String selectedIP = clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS);

            int port = clientManager.getIntegerValue(ConfigManager.LEADER_PORT_NUMBER);

            String bindingname = "serverclient";

            int selectedArticleId = -1;
            int maximumIdSeenYet = -1;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                    System.out.println("Choose an Option? \n1. JOIN A SERVER\n" +
                            "2. CHANGE SERVER \n3. Read An Article \n4. Post An Article\n5. Display Articles\n6. Reply to " +
                            "Article\n");
                    int readValue = Integer.parseInt(reader.readLine());
                    switch (readValue) {
                        case 1:
                            ArrayList<ServerInfo> listOfServers = getRMIStub(selectedIP, port, bindingname).getListOfServers();
                            System.out.println("Select a server to connect?");
                            for(int i=0;i<listOfServers.size();i++){
                                System.out.println(i+1 + " " + listOfServers.get(i).getIp() + ":" + listOfServers.get(i).getPort());
                            }

                            int server = Integer.parseInt(reader.readLine());
                            if (server == 1) {
                                port = 5005;
                            }
                            else if(server == 2){
                                port = 5006;
                            }
                            else port = 5007;
                            break;
                        case 2:
                            System.out.println("Select a server to connect?  ");
                            System.out.println("1. 5005");
                            System.out.println("2. 5006");
                            System.out.println("3. 5007");
                            server = Integer.parseInt(reader.readLine());
                            if (server == 1) {
                                port = 5005;
                            }
                            else if(server == 2){
                                port = 5006;
                            }
                            else port = 5007;
                            break;
                        case 3:
                            System.out.println("Article ID?\n");
                            int id = Integer.parseInt(reader.readLine());
                            List<Article> articleList = getRMIStub(selectedIP, port, bindingname).readArticle(id,maximumIdSeenYet);
                            for (Article article : articleList) {
                                for (int i = 0; i < article.getIndentCount(); i++) System.out.print("  ");
                                System.out.println(article.getID() + " : " + article.getContent());
                                maximumIdSeenYet = Math.max(article.getID(), maximumIdSeenYet);
                            }
                            selectedArticleId = id;
                            maximumIdSeenYet = Math.max(selectedArticleId, maximumIdSeenYet);
                            break;
                        case 4:
                            System.out.println("Article Content?\n");
                            String content = reader.readLine();
                            getRMIStub(selectedIP, port, bindingname).postArticle(content, -1, -1);
                            break;
                        case 5:

                            boolean displayMoreArticles = true;
                            int displayId = 1;
                            while (displayMoreArticles) {

                                Article[] articles = getRMIStub(selectedIP, port, bindingname).readArticles(displayId, maximumIdSeenYet);

                                if(articles.length == 0)
                                    displayMoreArticles = false;

                                for (int i = 0; i < articles.length; i++) {
                                    if (articles[i] != null) {
                                        System.out.println(articles[i].getID() + " : " + articles[i].getContent());
                                        maximumIdSeenYet = Math.max(articles[i].getID(), maximumIdSeenYet);
                                    } else {
                                        displayMoreArticles = false;
                                    }
                                }
                                displayId = displayId + 8;
                                if (!displayMoreArticles) {
                                    System.out.println("\nNo more articles to display\n");
                                    break;
                                }


                                System.out.println("Do you want to display more articles? \nEnter 1 for more articles and 2 " +
                                        "if you don't want to display anymore articles");
                                int moreArticles = Integer.parseInt(reader.readLine());
                                if (moreArticles == 2) {
                                    displayMoreArticles = false;
                                }

                            }
                            break;
                        case 6:
                            System.out.println("Please enter the article number to which you want to reply\n");
                            int articleNumber = Integer.parseInt(reader.readLine());
                            System.out.println("Please enter your reply\n");
                            String reply = reader.readLine();
                            if (selectedArticleId == -1) selectedArticleId = articleNumber;
                            getRMIStub(selectedIP, port, bindingname).postArticle(reply, articleNumber, selectedArticleId);
                            break;
                        default:
                            System.out.println("Please enter a valid input");
                            break;
                    }
            }
        }catch (Exception ex) {
                System.out.println(ex.getMessage());
        }
    }
}

