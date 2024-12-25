package data;

public class user {
    private String userName = "";
    private String userHost = "";
    private int userPort = 0;

    public void setUser(String name, String host, int port) {
        userName = name;
        userHost = host;
        userPort = port;
    }
    public void setName(String name) {
        userName = name;
    }
    public void setHost(String host) {
        userHost = host;
    }
    public void setPort(int port) {
        userPort = port;
    }
    public String getUsername() {
        return userName;
    }
    public String getHost() {
        return userHost;
    }
    public int getPort() {
        return userPort;
    }
}