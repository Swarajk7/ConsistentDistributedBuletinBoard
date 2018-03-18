package distributed.consistent.model;

import distributed.consistent.server.ServerInfo;

import java.io.Serializable;

public class ServerInfoWithMaxId implements Serializable {
    private ServerInfo serverInfo;
    private int maximum_id;

    public ServerInfoWithMaxId(String ip, int port, String bindingname, int maximum_id) {
        this.serverInfo = new ServerInfo(ip, port, bindingname);
        this.maximum_id = maximum_id;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public int getMaximum_id() {
        return maximum_id;
    }
}
