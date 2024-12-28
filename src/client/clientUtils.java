package client;

import client.*;
import utils.*;

import javax.swing.*;
import java.io.*;
import java.net.*;

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
                    String name = decode.getNameRequest(message);
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
