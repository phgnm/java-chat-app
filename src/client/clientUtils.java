package client;

import utils.*;

import java.io.*;
import java.net.*;

public class clientUtils {
    private String username = "";
    private ServerSocket user;
    private int port;
    private boolean isStop = false;

    public void stopServer() {
        isStop = true;
    }

    public boolean stopStatus() {
        return isStop;
    }

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

                    ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());

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
