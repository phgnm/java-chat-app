package client;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class clientUI extends JFrame implements WindowListener {
    private client clientNode;
    private static String IPClient = "";
    private static String username = "";
    private static String userData = "";
    private static int clientPort = 0;
    private static JList<String> activeList;
    private static int serverPort;
    private String name;
    static DefaultListModel<String> model = new DefaultListModel<>();
    String File = System.getProperty("user.dir") + "server.txt";
    private JButton saveButton;

    public clientUI(String ip, int port_Client, String name, String msg, int port_Server) throws Exception {
        IPClient = ip;
        clientPort = port_Client;
        username = name;
        userData = msg;
        serverPort = port_Server;

        EventQueue.invokeLater(() -> {
            try {
                clientUI frame = new clientUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public clientUI() throws Exception {
        this.addWindowListener(this);
        setResizable(false);

        clientNode = new client(IPClient, clientPort, username, userData, serverPort);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 680, 520);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblChat = new JLabel("Chat Client");
        lblChat.setForeground(Color.RED);
        lblChat.setFont(new Font("Tahoma", Font.PLAIN, 32));
        lblChat.setBounds(225, 10, 255, 65);
        contentPane.add(lblChat);

        JLabel lblWelcome = new JLabel("Welcome " + username);
        lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblWelcome.setBounds(25, 50, 310, 45);
        contentPane.add(lblWelcome);

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                "Users are online", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Tahoma", Font.ITALIC | Font.BOLD, 14),
                new Color(0, 0, 0)));
        panel.setBackground(Color.WHITE);
        panel.setBounds(25, 100, 400, 345);

        contentPane.add(panel);
        panel.setLayout(new GridLayout(1, 1));

        System.out.println(model.size());
        activeList = new JList<>(model);
        activeList.setBorder(new EmptyBorder(5, 5, 5, 5));
        activeList.setBackground(Color.WHITE);
        activeList.setForeground(Color.RED);
        activeList.setFont(new Font("Courier New", Font.PLAIN, 16));
        activeList.setBounds(10, 20, 575, 330);
        JScrollPane listPane = new JScrollPane(activeList);

        panel.add(listPane);

        JPanel panel1 = new JPanel();
        panel1.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                "Server info", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Tahoma", Font.ITALIC | Font.BOLD, 14),
                Color.WHITE));
        panel1.setForeground(Color.BLUE);
        panel1.setBackground(Color.BLACK);
        panel1.setBounds(440, 290, 200, 110);
        contentPane.add(panel1);
        panel1.setLayout(null);

        JLabel lblIPAddress = new JLabel("IP Address");
        lblIPAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblIPAddress.setForeground(Color.WHITE);
        lblIPAddress.setBounds(10, 20, 85, 25);
        panel1.add(lblIPAddress);

        JLabel lblServerPort = new JLabel("Port Server");
        lblServerPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblServerPort.setForeground(Color.WHITE);
        lblServerPort.setBounds(10, 50, 85, 25);
        panel1.add(lblServerPort);

        JLabel lblClientPort = new JLabel("Port Client");
        lblClientPort.setForeground(Color.WHITE);
        lblClientPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblClientPort.setBounds(10, 80, 75, 25);
        panel1.add(lblClientPort);

        JLabel lblIP = new JLabel("127.0.0.1");
        try {
            lblIP.setText(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        lblIP.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblIP.setForeground(Color.GREEN);
        lblIP.setBounds(90, 20, 115, 25);
        panel1.add(lblIP);

        JLabel lblCurrentPort = new JLabel(String.valueOf(serverPort));
        lblCurrentPort.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblCurrentPort.setForeground(Color.RED);
        lblCurrentPort.setBounds(90, 50, 75, 25);
        panel1.add(lblCurrentPort);

        JLabel lblClient = new JLabel(String.valueOf(clientPort));
        lblClient.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblClient.setForeground(Color.BLUE);
        lblClient.setBounds(90, 80, 90, 25);
        panel1.add(lblClient);

        saveButton = new JButton("Save Server");
        saveButton.addActionListener(_ -> saveServer());
        saveButton.setFocusable(false);
        saveButton.setBounds(490, 420, 110, 30);
        contentPane.add(saveButton);
        activeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                name = activeList.getModel().getElementAt(activeList.locationToIndex(arg0.getPoint()));
                connectChat();
            }
        });
    }

    public static void updateFriends(String msg) {
        System.out.println("Attempting to add friend: " + msg);
        System.out.println("Current list before addition: " + model.toString());
        model.addElement(msg);
        System.out.println("Current list after addition: " + model.toString());
        System.out.println("List size: " + model.getSize());
    }

    public static void resetList() {
        model.clear();
    }

    void saveServer() {
        try {
            FileWriter fw = new FileWriter(File, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(IPClient + " - " + serverPort);
            bw.newLine();
            bw.close();

            JOptionPane.showMessageDialog(this, "Server was successful saved.");
            saveButton.setVisible(false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void connectChat() {
        int n = JOptionPane.showConfirmDialog(this, "Do you want to connect to this guy?", "Connection",
                JOptionPane.YES_NO_OPTION);
        if (n == 0) {
            if (name.isEmpty() || client.clientList == null) {
                JOptionPane.showMessageDialog(this, "Invalid username!");
                return;
            }
            if (name.equals(username)) {
                JOptionPane.showMessageDialog(this, "This software doesn't support chat yourself function");
                return;
            }
            int size = client.clientList.size();
            for (int i = 0; i < size; i++) {
                if (name.equals(client.clientList.get(i).getUsername())) {
                    try {
                        clientNode.initialNewChat(client.clientList.get(i).getHost(),
                                client.clientList.get(i).getPort(), name);
                        return;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Friend is not found. Please update your list friend");
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            clientNode.exit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
