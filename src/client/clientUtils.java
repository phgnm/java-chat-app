package client;

import utils.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class clientUtils {
    private String username = "";
    private ServerSocket user;
    private int port;
    private boolean isStop = false;

    public clientUtils(String name) throws Exception {
        username = name;
        port = client.getPort();
        user = new ServerSocket(port);
        (new waitForConnect()).start();
    }

    public void exit() throws Exception {
        isStop = true;
        user.close();
    }

    class waitForConnect extends Thread {
        Socket connection;
        ObjectInputStream in;

        @Override
        public void run() {
            super.run();
            while(!isStop) {
                try {
                    connection = user.accept();
                    in = new ObjectInputStream(connection.getInputStream());
                    String message = (String) in.readObject();
                    System.out.println(message);
                    String[] messageParts = message.split(" ");
                    String name = messageParts[0];
                    if (Objects.equals(messageParts[1], constants.startChat))
                    {
                        int res = JOptionPane.showConfirmDialog(new JFrame(), name + " wants to connect with you!", null, JOptionPane.YES_NO_OPTION);
                        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                        if (res == 1)
                            out.writeObject(constants.reject);
                        else if (res == 0) {
                            out.writeObject(constants.accept);
                            new chatUI(username, name, connection, port);
                        }
                        out.flush();
                    }
                    else if (Objects.equals(messageParts[1], constants.startGroupChat))
                    {
                        String[] userList = name.split("~~~");
                        int res = JOptionPane.showConfirmDialog(new JFrame(), userList[0] + " wants to create a group what with you and " + (userList.length - 2) + " other people", null, JOptionPane.YES_NO_OPTION);
                        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                        if (res == 1)
                            out.writeObject(constants.reject);
                        else if (res == 0) {
                            String groupName = userList[userList.length - 1];
                            ArrayList<String> guestList = new ArrayList<>(Arrays.asList(userList));
                            guestList.removeFirst();
                            guestList.removeLast();
                            out.writeObject(constants.accept);
                            new groupChatUI(username, guestList, connection, port, groupName);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                user.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
