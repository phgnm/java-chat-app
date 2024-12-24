package client;

import client.clientUtils;
import utils.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.*;

public class login extends JFrame implements ActionListener {
    private Pattern checkName = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    private JTextField IPField;
    private JTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    int port;
    String IP;
    String username;
    String password;
    JButton connectButton;
    JButton signupButton;
    String file = System.getProperty("user.dir") + "\\server.txt";
    List<String> serverList = new ArrayList<>();

    public login() {
        setTitle("Login Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 500);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblNewLabel = new JLabel("Connecting to Server");
        lblNewLabel.setForeground(Color.RED);
        lblNewLabel.setFont(new Font("Cambria", Font.PLAIN, 32));
        lblNewLabel.setBounds(200, 0, 350, 50);

        JComboBox comboBox = new JComboBox();
        comboBox.setForeground(Color.DARK_GRAY);
        String[] data = null;
        try {
            data = readFileServer();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        if (data == null || data.length == 0) {
            lblNewLabel.setBounds(200, 24, 350, 50);
            comboBox.setVisible(false);
        } else {
            comboBox.setModel(new DefaultComboBoxModel(data));
            // Handle event
            comboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String ss = (String) cb.getSelectedItem();
                    String[] s = ss.split(" - ");
                    updateServer(s[0], s[1]);
                }
            });
        }
        comboBox.setFont(new Font("Cambria", Font.PLAIN, 14));
        comboBox.setBounds(270, 50, 150, 40);

        contentPane.add(lblNewLabel);
        contentPane.add(comboBox);

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                "Setting", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Cambria", Font.ITALIC | Font.BOLD, 14),
                new Color(0, 0, 0)));
        panel.setBackground(Color.CYAN);
        panel.setBounds(50, 98, 600, 280);
        contentPane.add(panel);
        panel.setLayout(null);

        JLabel lblNewLabel_1 = new JLabel("IP Address  Server");
        lblNewLabel_1.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblNewLabel_1.setBounds(26, 32, 136, 37);
        panel.add(lblNewLabel_1);

        JLabel lblNewLabel_2 = new JLabel("Port");
        lblNewLabel_2.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblNewLabel_2.setBounds(26, 92, 45, 37);
        panel.add(lblNewLabel_2);

        JLabel lblNewLabel_3 = new JLabel("Your Name");
        lblNewLabel_3.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblNewLabel_3.setBounds(26, 152, 84, 37);
        panel.add(lblNewLabel_3);

        JLabel lblNewLabel_4 = new JLabel("Password");
        lblNewLabel_4.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblNewLabel_4.setBounds(26, 212, 84, 37);
        panel.add(lblNewLabel_4);

        IPField = new JTextField();
        IPField.setFont(new Font("Cambria", Font.PLAIN, 14));
        IPField.setBounds(237, 32, 277, 37);
        panel.add(IPField);
        IPField.setColumns(10);
        try {
            IPField.setText(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        portField = new JTextField();
        portField.setText("1227");
        portField.setFont(new Font("Cambria", Font.PLAIN, 14));
        portField.setBounds(237, 92, 277, 37);
        panel.add(portField);
        portField.setColumns(10);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Cambria", Font.PLAIN, 14));
        usernameField.setBounds(237, 152, 277, 37);
        panel.add(usernameField);
        usernameField.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setBounds(237, 212, 277, 37);
        panel.add(passwordField);
        passwordField.setColumns(10);

        signupButton = new JButton("Sign up");
        signupButton.setBounds(100, 402, 200, 40);
        signupButton.setFont(new Font("Cambria", Font.PLAIN, 16));
        signupButton.addActionListener(this);
        contentPane.add(signupButton);

        connectButton = new JButton("Connect to server");
        connectButton.setBounds(375, 402, 200, 40);
        connectButton.setFont(new Font("Cambria", Font.PLAIN, 16));
        connectButton.addActionListener(this);
        contentPane.add(connectButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton || e.getSource() == signupButton) {
            username = usernameField.getText();
            IP = IPField.getText();
            password = String.valueOf(passwordField.getPassword());

            if (checkName.matcher(username).matches() && checkName.matcher(password).matches() && !IP.isEmpty()) {
                String command = e.getActionCommand();

                try {
                    // Login
                    Random rd = new Random();
                    int portPeer = 10000 + rd.nextInt() % 1000;
                    InetAddress ipServer = InetAddress.getByName(IP);
                    int portServer = Integer.parseInt(portField.getText());
                    Socket socketClient = new Socket(ipServer, portServer);

                    String msg = command.equals("Sign up") ?
                            encode.signUp(username, Integer.toString(portPeer), password)
                            : encode.login(username, Integer.toString(portPeer), password);
                    ObjectOutputStream out = new ObjectOutputStream(socketClient.getOutputStream());
                    out.writeObject(msg);
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(socketClient.getInputStream());
                    msg = (String) in.readObject();

                    socketClient.close();
                    if (msg.equals(constants.reject)) {
                        JOptionPane.showMessageDialog(this,
                                command.equals("Sign up") ? constants.existName : constants.unmatchedLogin,
                                "Login Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    new clientUI(IP, portPeer, username, msg, portServer);
                    this.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, constants.serverOff, "Login Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, constants.unmatchedLogin, "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    login frame = new login();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateServer(String IP, String port) {
        IPField.setText(IP);
        portField.setText(port);
    }

    private String[] readFileServer() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(file));
        while (scanner.hasNext()) {
            String server = scanner.nextLine();
            serverList.add(server);
        }
        scanner.close();
        return serverList.toArray(new String[0]);
    }
}
