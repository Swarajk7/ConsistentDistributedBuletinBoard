package distributed.consistent;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    private String[] allowedTypeList = "Sports, Lifestyle, Entertainment, Business, Technology, Science,Politics, Health".split(",");
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    boolean validateIP(String IP) {
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(IP);
        return matcher.matches();
    }

    boolean validateArticle(String article, boolean is_publish) {
        if (article.length() > 120) return false;
        String[] tokens = article.split(";", -1);
        if (tokens.length != 4) {
            return false;
        }
        if (tokens[0].trim().length() != 0) {
            //if type is specified, but not is not part of our list, then article is invalid.
            //not checking for uppercase and lowercase conflicts
            if (!Arrays.asList(allowedTypeList).contains(tokens[0].trim())) {
                return false;
            }
        }
        //for atleast one of the 3 things should be there.
        if (tokens[0].trim().isEmpty() && tokens[1].trim().isEmpty() && tokens[2].trim().isEmpty())
            return false;

        if (is_publish) {
            //content can't be empty for publishing an article
            if (tokens[3].trim().length() == 0) return false;
        } else {
            if (tokens[3].trim().length() != 0) return false;
        }
        //if none of the above check fails, it is considered as passed.
        return true;
    }

    String appendIPAndPort(String IP, int port) {
        return IP + ":" + port;
    }

    public String createRMIConnectionString(String ip, int port, String binding_name) {
        return "rmi://" + ip + ":" + port + "/" + binding_name;
    }

    public String createUDPMessageForRegister(String ip, int rmiport, String bindingname, int incomingupdport) {
        return "Register;RMI;"
                + ip + ";"
                + rmiport + ";"
                + bindingname + ";"
                + incomingupdport;
    }

    public String createUDPMessageForGetList(String ip, int rmiport) {
        return "GetList;RMI;" + ip + ";" + rmiport;
    }

    public String createUDPMessageForDeRegister(String ip, int rmiport) {
        return "Deregister;RMI;" + ip + ";" + rmiport;
    }

    public String createKeyForServerInfo(String ip, int port, String bindingname) {
        return ip + "#" + port + "#" + bindingname;
    }
}
