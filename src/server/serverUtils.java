package server;

import data.user;
import utils.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class serverUtils {
    private ArrayList<user> userList = null;
    private ServerSocket mainServer;
    private Socket socket;
    public boolean isStop = false;
    public boolean isExit = false;
    private static int serverPort;

    String file = System.getProperty("user.dir") + "\\client.txt";

    ArrayList<user> getUserList() {
        return userList;
    }
    public serverUtils(int port) throws Exception {
        mainServer = new ServerSocket(port);
        userList = new ArrayList<user>();
        serverPort = port;
        System.out.println("Done");
        (new connectionPending()).start();
    }

    public void closeServer() throws Exception {
        isStop = true;
        mainServer.close();
        if (socket != null) {
            socket.close();
        }
    }

    public void savePeer(String user, String IP, int port) {
        if (userList.isEmpty())
            userList = new ArrayList<user>();
        user newUser = new user(user, IP, port);
        userList.add(newUser);
    }

    public void savePeerToFile(String name, String password) {
        try (FileWriter f = new FileWriter(file, true);
             BufferedWriter b = new BufferedWriter(f);
             PrintWriter p = new PrintWriter(b);){
            p.println(name + "~" + password);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValid (String name, String password, String command) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split("~");
                if (split.length == 2) {
                    if (command.equals(constants.login)) {
                        if (name.equals(split[0])) {
                            return password.equals(split[1]);
                        }
                    }
                    else  {
                        if (name.equals(split[0])) {
                            return false;
                        }
                    }
                }
                line = br.readLine();
            }
            return !command.equals(constants.login);
        }
        catch (FileNotFoundException e) {
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
    public boolean waitForConnection() throws Exception {
        socket = mainServer.accept();
        ObjectInputStream clientIn = new ObjectInputStream(socket.getInputStream());
        String message = (String) clientIn.readObject();
        ArrayList<String> data = decode.getUser(message);
        String command = decode.getLoginCmd(message);
        if (data != null) {
            if (isValid(data.get(0), data.get(2), command)) {
                if (command.equals(constants.signUp))
                    savePeerToFile(data.get(0), data.get(2));
                savePeer(data.get(0), socket.getInetAddress().toString(), Integer.parseInt(data.get(1)));
                server.increaseClientNumber();
            }
            else
                return false;
        }
        else {
            int size = userList.size();
            decode.updateOnline(userList, message);
            if (size != userList.size()) {
                isExit = true;
                server.decreaseClientNumber();
            }
        }
        return true;
    }
    public String sendSession() {
        StringBuilder message = new StringBuilder(constants.accept + " ");
        int size = userList.size();
        for (user current : userList) {
            message.append(current.getUsername());
            message.append("-");
            message.append(current.getHost());
            message.append("-");
            message.append(current.getPort());
            message.append("-");
        }
        return message.toString();
    }
    public class connectionPending extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (!isStop) {
                    ObjectOutputStream clientOut;
                    if (waitForConnection()) {
                        if (isExit) {
                            isExit = false;
                        }
                        else {
                            clientOut = new ObjectOutputStream(socket.getOutputStream());
                            clientOut.writeObject(sendSession());
                            clientOut.flush();
                            clientOut.close();
                        }
                    }
                    else {
                        clientOut = new ObjectOutputStream(socket.getOutputStream());
                        clientOut.writeObject(constants.reject);
                        clientOut.flush();
                        clientOut.close();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
