package distributed.consistent.server;

public class ServerInfo {
    private String ip;
    private int port;
    private String bindingname;

    public ServerInfo(String ip, int port, String bindingname) {
        this.ip = ip;
        this.port = port;
        this.bindingname = bindingname;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindingname() {
        return bindingname;
    }

    public void setBindingname(String bindingname) {
        this.bindingname = bindingname;
    }
}
