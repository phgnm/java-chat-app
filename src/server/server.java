package server;

import data.user;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class server extends JFrame {
    private JPanel contentPane;
    private JTextField IP;
    private JTextField Port;
    private static JTextArea messages;
    private JLabel status;
    private static JLabel userNumber;
    private int port = 1227;
    private JButton stopButton;
    private JButton startButton;
    static serverUtils sUtils;

    public server() {
        // App size configuration
        setResizable(false);
        setTitle("Server menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 830, 700);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Config label
        JLabel lblConfig = new JLabel("Server configuration");
        lblConfig.setFont(new Font("Tahoma", Font.PLAIN, 27));
        lblConfig.setBounds(300, 0, 245, 75);
        contentPane.add(lblConfig);

        // Content box
        JPanel panel = new JPanel();
        panel.setBounds(30, 100, 300, 40);
        contentPane.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        // IP Label
        JLabel lblIP = new JLabel("IP");
        lblIP.setFont(new Font("Tahoma", Font.ITALIC | Font.BOLD, 18));
        panel.add(lblIP);
        panel.add(new JPanel());
        IP = new JTextField();
        IP.setEditable(false);
        IP.setForeground(Color.BLUE);
        IP.setFont(new Font("Tahoma", Font.ITALIC, 16));
        panel.add(IP);
        IP.setColumns(10);
        try {
            IP.setText(Inet4Address.getLocalHost().getHostAddress());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Port panel
        JPanel panel1 = new JPanel();
        panel1.setBounds(30, 150, 300, 40);
        contentPane.add(panel1);
        panel1.setLayout(null);

        JLabel lblPort = new JLabel("Port");
        lblPort.setFont(new Font("Tahoma", Font.ITALIC | Font.BOLD, 21));
        lblPort.setBounds(0, 10, 55, 25);
        panel1.add(lblPort);

        // Address panel
        JPanel panel2 = new JPanel();
        panel2.setBounds(20, 0, 10, 40);
        panel1.add(panel2);

        Port = new JTextField();
        Port.setForeground(Color.RED);
        Port.setText("1227");
        Port.setFont(new Font("Tahoma", Font.PLAIN, 16));
        Port.setBounds(95, 0, 185, 40);
        panel1.add(Port);
        Port.setColumns(10);

        // Server information panel
        JPanel panel3 = new JPanel();
        panel3.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                "Server Information", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Cambria", Font.ITALIC, 14),
                new Color(0, 0,0)));
        panel3.setBackground(Color.CYAN);
        panel3.setBounds(430, 90, 350, 150);
        contentPane.add(panel3);
        panel3.setLayout(null);

        JLabel lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblStatus.setBounds(20, 35, 75, 15);
        panel3.add(lblStatus);

        status = new JLabel("OFF");
        status.setForeground(Color.RED);
        status.setFont(new Font("Tahoma", Font.PLAIN, 16));
        status.setBounds(200, 25, 125, 45);
        panel3.add(status);

        JLabel lblUserNum = new JLabel("User Numbers");
        lblUserNum.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblUserNum.setBounds(20, 95, 185, 30);
        panel3.add(lblUserNum);

        userNumber = new JLabel("0");
        userNumber.setForeground(Color.BLUE);
        userNumber.setFont(new Font("Tahoma", Font.PLAIN, 16));
        userNumber.setBounds(200, 95, 85, 35);
        panel3.add(userNumber);

        JPanel panel4 = new JPanel();
        panel4.setBorder(
                new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Tahoma", Font.BOLD, 16), null));
        panel4.setBounds(105, 560, 620, 65);
        contentPane.add(panel4);

        startButton = new JButton("Start");
        startButton.setFocusable(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    port = Integer.parseInt(Port.getText());
                    sUtils = new serverUtils(port);
                    server.updateMessage("SERVER STARTS ON PORT " + port);
                    status.setText("<html><font color='green'>RUNNING...</font></html>");
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                }
                catch (Exception ex) {
                    server.updateMessage("START ERROR");
                    ex.printStackTrace();
                }
            }
        });
        startButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        panel4.add(startButton);

        JPanel panel5 = new JPanel();
        panel4.add(panel5);
        panel4.add(new JPanel());

        stopButton = new JButton("Stop");
        stopButton.setFocusable(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userNumber.setText("0");
                try {
                    sUtils.closeServer();
                    server.updateMessage("SERVER STOPS");
                    status.setText("<html><font color='red'>STOPPED</font></html>");

                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                }
                catch (Exception ex) {
                    System.out.println("Here");
                    ex.printStackTrace();
                    server.updateMessage("SERVER STOPS");
                    status.setText("<html><font color='red'>STOPPED</font></html>");
                }
            }
        });
        stopButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        panel4.add(stopButton);

        JPanel panel6 = new JPanel();
        panel6.setBorder(
                new TitledBorder(null, "User list", TitledBorder.LEADING, TitledBorder.TOP,
                    new Font("Tahoma", Font.BOLD, 16), null));
        messages = new JTextArea();
        messages.setBackground(Color.BLACK);
        messages.setForeground(Color.WHITE);
        messages.setFont(new Font("Tahoma", Font.PLAIN, 16));
        panel6.setBounds(30, 250, 760, 300);
        panel6.setLayout(new GridLayout(0, 1, 0, 0));
        JScrollPane scrollPane = new JScrollPane(messages);
        panel6.add(scrollPane);
        contentPane.add(panel6);
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

    public static void displayUser() {
        messages.setText("");
        ArrayList<user> list = sUtils.getUserList();
        for (int i = 0; i < list.size(); i++) {
            messages.append((i + 1) + "\t" + list.get(i).getUsername() + "\n");
        }
    }
    public static void updateMessage(String message) {
        messages.append(message + "\n");
    }

    public static void increaseClientNumber() {
        int number = Integer.parseInt(userNumber.getText());
        userNumber.setText(Integer.toString(number + 1));
        displayUser();
    }

    public static void decreaseClientNumber() {
        int number = Integer.parseInt(userNumber.getText());
        userNumber.setText(Integer.toString(number - 1));
        displayUser();
    }
}