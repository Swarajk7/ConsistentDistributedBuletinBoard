package distributed.consistent.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
Singleton class for handling server side config read and write.
ConfigurationManger is also readonly and hence won't cause any conflicts or concurrency issue.
 */
public class ConfigManager {
    public static final String RMI_BINDING_NAME="rmibindingname";
    public static final String NUMBER_OF_PUBLISH_THREADS = "numerofpublisherthreads";
    public static final String LEADER_BINDING_NAME = "leaderbindingname";
    public static final String LEADER_PORT_NUMBER = "leaderportnumber";
    public static final String LEADER_IP_ADDRESS = "leaderipaddress";
    public static final String DATABASE_FILE_PATH = "databasefilepath";
    public static final String QUORUM_READ_MEMBER_COUNT = "quorumreadmembercount";
    public static final String QUORUM_WRITE_MEMBER_COUNT = "quorumwritemembercount";
    public static final String SERVER_PROTOCOL = "protocol";
    public static final String SYNC_SLEEP_TIME_IN_SECONDS = "syncsleeptimeinseconds";




    //fix below line for better lookup.. don't hardcode
    private static String filename = "server_config.properties";
    private Properties prop;
    private static ConfigManager obj = null;

    private ConfigManager() {
    }

    public static ConfigManager create() throws IOException {
        if (obj != null) return obj;
        obj = new ConfigManager();
        obj.prop = new Properties();
        InputStream input = new FileInputStream(filename);
        obj.prop.load(input);
        return obj;
    }

    public String getValue(String key) {
        return this.prop.getProperty(key);
    }

    public int getIntegerValue(String key) {
        return Integer.parseInt(this.getValue(key));
    }
}
