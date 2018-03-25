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
            ArrayList<Integer> replyUnderSelectedArticle = new ArrayList<>();
            int maximumIdSeenYet = -1;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Choose an Option? \n1. Choose Server\n" +
                        "2. Read An Article \n3. Post An Article\n4. Display Articles\n5. Reply to " +
                        "Article\n");
                int readValue = Integer.parseInt(reader.readLine());
                switch (readValue) {
                    case 1:
                        selectedIP = clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS);
                        port = clientManager.getIntegerValue(ConfigManager.LEADER_PORT_NUMBER);
                        ArrayList<ServerInfo> listOfServers = getRMIStub(selectedIP, port, bindingname).getListOfServers();
                        System.out.println("Select a server to connect?");
                        for (int i = 0; i < listOfServers.size(); i++) {
                            System.out.println(i + 1 + " " + listOfServers.get(i).getIp() + ":" + listOfServers.get(i).getPort());
                        }

                        int server = Integer.parseInt(reader.readLine());
                        if (server < 1 || server > listOfServers.size()) {
                            System.out.println("Please enter a valid server number");
                        } else {
                            port = listOfServers.get(server - 1).getPort();
                            System.out.println("Client joined server : " + listOfServers.get(server - 1).getIp() +
                                    ":" + port);
                        }
                        break;
                    case 2:
                        System.out.println("Article ID?\n");
                        int id = Integer.parseInt(reader.readLine());
                        replyUnderSelectedArticle.clear();
                        List<Article> articleList = getRMIStub(selectedIP, port, bindingname).readArticle(id, maximumIdSeenYet);
                        for (Article article : articleList) {
                            for (int i = 0; i < article.getIndentCount(); i++) System.out.print("  ");
                            System.out.println(article.getID() + " : " + article.getContent());
                            maximumIdSeenYet = Math.max(article.getID(), maximumIdSeenYet);
                            replyUnderSelectedArticle.add(article.getID());
                        }
                        selectedArticleId = id;
                        maximumIdSeenYet = Math.max(selectedArticleId, maximumIdSeenYet);
                        break;
                    case 3:
                        System.out.println("Article Content?\n");
                        String content = reader.readLine();
                        int generatedid = getRMIStub(selectedIP, port, bindingname).
                                postArticle(content, -1, -1);
                        maximumIdSeenYet = Math.max(generatedid, maximumIdSeenYet);
                        break;
                    case 4:

                        boolean displayMoreArticles = true;
                        int displayId = 1;
                        while (displayMoreArticles) {

                            Article[] articles = getRMIStub(selectedIP, port, bindingname).readArticles(displayId, maximumIdSeenYet);


                            for (int i = 0; i < articles.length; i++) {
                                if (articles[i] != null) {
                                    System.out.println(articles[i].getID() + " : " + articles[i].getContent());
                                    maximumIdSeenYet = Math.max(articles[i].getID(), maximumIdSeenYet);
                                } else {
                                    displayMoreArticles = false;
                                }
                            }

                            if (!displayMoreArticles) {
                                System.out.println("\nNo more articles to display\n");
                                break;
                            }

                            displayId = articles[articles.length - 1].getID() + 1;
                            System.out.println("Do you want to display more articles? \nEnter 1 for more articles and 2 " +
                                    "if you don't want to display anymore articles");

                            int moreArticles = Integer.parseInt(reader.readLine());
                            if (moreArticles == 2) {
                                displayMoreArticles = false;
                            }

                        }
                        break;
                    case 5:
                        if (selectedArticleId == -1) {
                            System.out.println("Please select an article first.");
                            break;
                        }
                        System.out.println("Please enter the article number to which you want to reply\n");
                        int articleNumber = Integer.parseInt(reader.readLine());
                        if (!replyUnderSelectedArticle.contains(articleNumber)) {
                            System.out.println("Make sure you have selected the right id.");
                            break;
                        }
                        System.out.println("Please enter your reply\n");
                        String reply = reader.readLine();
                        generatedid = getRMIStub(selectedIP, port, bindingname).postArticle(reply, articleNumber, selectedArticleId);
                        maximumIdSeenYet = Math.max(generatedid, maximumIdSeenYet);
                        break;
                    default:
                        System.out.println("Please enter a valid input");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}

