package client;

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
    String IP;
    String username;
    String password;
    JButton connectButton;
    JButton signupButton;
    String file = System.getProperty("user.dir") + "\\src\\server.txt";
    List<String> serverList = new ArrayList<>();

    public login() {
        setTitle("Login Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 500);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblConnection = new JLabel("Connecting to Server");
        lblConnection.setForeground(Color.RED);
        lblConnection.setFont(new Font("Cambria", Font.PLAIN, 32));
        lblConnection.setBounds(200, 0, 350, 50);

        JComboBox comboBox = new JComboBox();
        comboBox.setForeground(Color.DARK_GRAY);
        String[] data = null;
        try {
            data = readFileServer();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        if (data == null || data.length == 0) {
            lblConnection.setBounds(200, 24, 350, 50);
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

        contentPane.add(lblConnection);
        contentPane.add(comboBox);

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                "Setting", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Cambria", Font.ITALIC | Font.BOLD, 14),
                new Color(0, 0, 0)));
        panel.setBackground(Color.CYAN);
        panel.setBounds(50, 100, 600, 280);
        contentPane.add(panel);
        panel.setLayout(null);

        JLabel lblIP = new JLabel("IP Address  Server");
        lblIP.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblIP.setBounds(25, 30, 135, 35);
        panel.add(lblIP);

        JLabel lblPort = new JLabel("Port");
        lblPort.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblPort.setBounds(25, 90, 45, 35);
        panel.add(lblPort);

        JLabel lblName = new JLabel("Your Name");
        lblName.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblName.setBounds(25, 150, 85, 35);
        panel.add(lblName);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 14));
        lblPassword.setBounds(25, 210, 85, 35);
        panel.add(lblPassword);

        IPField = new JTextField();
        IPField.setFont(new Font("Cambria", Font.PLAIN, 14));
        IPField.setBounds(235, 30, 275, 35);
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
        portField.setBounds(235, 90, 275, 35);
        panel.add(portField);
        portField.setColumns(10);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Cambria", Font.PLAIN, 14));
        usernameField.setBounds(235, 150, 275, 35);
        panel.add(usernameField);
        usernameField.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setBounds(235, 210, 275, 35);
        panel.add(passwordField);
        passwordField.setColumns(10);

        signupButton = new JButton("Sign up");
        signupButton.setBounds(100, 400, 200, 40);
        signupButton.setFont(new Font("Cambria", Font.PLAIN, 16));
        signupButton.addActionListener(this);
        contentPane.add(signupButton);

        connectButton = new JButton("Connect to server");
        connectButton.setBounds(375, 400, 200, 40);
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
                    Random rd = new Random();
                    int clientPort = 10000 + rd.nextInt() % 1000;
                    InetAddress serverIP = InetAddress.getByName(IP);
                    int serverPort = Integer.parseInt(portField.getText());
                    Socket socketClient = new Socket(serverIP, serverPort);

                    String msg = command.equals("Sign up") ?
                            encode.signUp(username, Integer.toString(clientPort), password)
                            : encode.login(username, Integer.toString(clientPort), password);
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
                    new clientUI(IP, clientPort, username, msg, serverPort);
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
        EventQueue.invokeLater(() -> {
            try {
                login frame = new login();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
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
