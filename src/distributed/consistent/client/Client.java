package distributed.consistent.client;

import distributed.consistent.model.Article;
import distributed.consistent.server.ConfigManager;
import distributed.consistent.server.interfaces.IClientServerCommunication;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
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
        String selectedIP = "10.0.0.210";
        int port = 0;
        String bindingname = "pubsubclient";

        int selectedArticleId = -1;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.println("Choose an Option? \n1. JOIN A SERVER\n" +
                        "2. CHANGE SERVER \n3. Read An Article \n4. Post An Article\n5. Display Articles\n6. Reply to " +
                        "Article\n");
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
                        List<Article> articleList = getRMIStub(selectedIP, port, bindingname).readArticle(id);
                        for (Article article : articleList) {
                            for (int i = 0; i < article.getIndentCount(); i++) System.out.print("  ");
                            System.out.println(article.getID() + " : " + article.getContent());
                        }
                        selectedArticleId = id;
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

                            Article[] articles = getRMIStub(selectedIP, port, bindingname).readArticles(displayId);


                            for (int i = 0; i < articles.length; i++) {
                                if (articles[i] != null) {
                                    System.out.println(articles[i].getID() + " : " + articles[i].getContent());
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
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
