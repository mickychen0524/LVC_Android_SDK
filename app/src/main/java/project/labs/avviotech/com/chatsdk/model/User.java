package project.labs.avviotech.com.chatsdk.model;


public class User {
    private String name;
    private String key;
    private String ip;
    private boolean isConnected;

    public User(String key) {
        this.key = key;
        name = key;
        this.isConnected = false;
        this.ip = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
