package distributed.consistent.server;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    private String ip;
    private int port;
    private String bindingname;
    private static Boolean isLocked;

    public ServerInfo(String ip, int port, String bindingname) {
        this.ip = ip;
        this.port = port;
        this.bindingname = bindingname;
        isLocked = false;
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
    public static synchronized boolean lock() {
        if(!isLocked){
            isLocked = true;
            return true;
        }
        else
            return false;

    }

    public void unLock() {
        this.isLocked = false;
    }

    public boolean getLockStatus(){
        return this.isLocked;
    }

    public String getBindingname() {
        return bindingname;
    }

    public void setBindingname(String bindingname) {
        this.bindingname = bindingname;
    }
}
