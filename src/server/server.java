package server;

import javax.swing.*;
import java.awt.*;

public class server extends JFrame {
    public server() {
        setResizable(false);
        setTitle("Server menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    server menu = new server();
                    menu.setVisible(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}