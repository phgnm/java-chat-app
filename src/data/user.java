package data;

public class user {
    private String username = "";
    private String host = "";
    private int port = 0;

    public user() {
        ;
    }
    public user(String name, String host, int port) {
        username = name;
        host = host;
        port = port;
    }
    public void setName(String name) {
        username = name;
    }
    public void setHost(String host) {
        host = host;
    }
    public void setPort(int port) {
        port = port;
    }
    public String getUsername() {
        return username;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
}