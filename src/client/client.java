package client;

import data.user;
import utils.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static ArrayList<user> clientList = null;
    private clientUtils cUtils;
    private InetAddress IP;
    private int serverPort;
    private static int clientPort = 10000;
    private int timeOut = 5000;
    private String username = "";
    private boolean isStop = false;
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public client(String arg, int arg1, String name, String userData, int port) throws Exception {
        IP = InetAddress.getByName(arg);
        username = name;
        clientPort = arg1;
        clientList = decode.getUserList(userData);
        serverPort = port;

        new Thread(this::updateFriend).start();
        cUtils = new clientUtils(username);
        (new Request()).start();
    }
    public static int getPort() {
        return clientPort;
    }
    public void request() throws Exception {
        clientSocket = new Socket();
        SocketAddress addr = new InetSocketAddress(IP, serverPort);
        clientSocket.connect(addr);
        String message = encode.onlineRequest(username);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.writeObject(message);
        out.flush();
        in = new ObjectInputStream(clientSocket.getInputStream());
        message = (String) in.readObject();
        in.close();
        clientList = decode.getUserList(message);
        new Thread(this::updateFriend).start();
    }
    public void exit() throws Exception {
        isStop = true;
        clientSocket = new Socket();
        SocketAddress addr = new InetSocketAddress(IP, serverPort);
        clientSocket.connect(addr);
        String message = encode.offlineRequest(username);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.writeObject(message);
        out.flush();
        out.close();
        cUtils.exit();
    }

    public void initialNewChat(String IP, int host, String guest) throws Exception {
        Socket connectionSocket = new Socket(InetAddress.getByName(IP), host);
        ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());
        out.writeObject(encode.chatRequest(username));
        out.flush();
        ObjectInputStream in = new ObjectInputStream(connectionSocket.getInputStream());
        String message = (String) in.readObject();
        if (message.equals(constants.reject)) {
            JOptionPane.showMessageDialog(new JFrame(), "Your friend denied to connect with you!");
            connectionSocket.close();
            return;
        }
        new chatUI(username, guest, connectionSocket, clientPort);
    }

    public void initialNewGroupChat(String IP, ArrayList<String> guest, String groupName) throws Exception {
        guest.remove(username);
        ArrayList<Socket> connections = new ArrayList<>();
        ArrayList<ObjectOutputStream> outputs = new ArrayList<>();
        boolean allAccepted = true;

        for (String guestUser : guest) {
            for (user clientUser : clientList) {
                if (clientUser.getUsername().equals(guestUser)) {
                    try {
                        Socket connectionSocket = new Socket(InetAddress.getByName(IP), clientUser.getPort());
                        ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());
                        out.flush(); // Important: flush the header

                        connections.add(connectionSocket);
                        outputs.add(out);

                        // Send the request
                        String request = encode.groupChatRequest(username, guest, groupName);
                        out.writeObject(request);
                        out.flush();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (int i = 0; i < connections.size(); i++) {
            Socket socket = connections.get(i);
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String message = (String) in.readObject();
                if (message.equals(constants.reject)) {
                    allAccepted = false;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                allAccepted = false;
                break;
            }
        }

        if (!allAccepted) {
            JOptionPane.showMessageDialog(new JFrame(), "One of your friends denied to connect with you!");
        } else if (!connections.isEmpty()) {
            System.out.println("Creator connections: " + connections);
            new groupChatUI(username, guest, connections, clientPort, groupName);
        }

        if (!allAccepted) { 
            for (Socket socket : connections) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateFriend() {
        int size = clientList.size();
        clientUI.resetList();
        int i = 0;
        while (i < size) {
            if (!clientList.get(i).getUsername().equals(username)) {
                clientUI.updateFriends(clientList.get(i).getUsername());
            }
            i++;
        }
    }

    public class Request extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop) {
                try {
                    Thread.sleep(timeOut);
                    request();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}