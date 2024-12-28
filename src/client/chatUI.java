package client;

import data.*;
import utils.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class chatUI extends JFrame {
    private static String URL_DIR = System.getProperty("user.dir");
    private Socket socketChat;
    private String nameUser = "", nameGuest = "", nameFile = "";
    public boolean isStop = false, isSendFile = false, isReceiveFile = false;
    private ChatRoom chat;
    private int portServer = 0;
    private ArrayList<String> chatHistory = new ArrayList<>();

    @Serial
    private static final long serialVersionUID = 1L;
    private JTextField Messages;
    private JTextPane MessagesPane;
    private JButton sendButton;
    private JLabel receiveStatus;
    private chatUI frame = this;
    private JProgressBar progressBar;
    JButton btnSend;

    public chatUI(String user, String guest, Socket socket, int port) throws Exception {
        super();
        nameUser = user;
        nameGuest = guest;
        socketChat = socket;
        frame = new chatUI(user, guest, socket, port, port);
        frame.setVisible(true);
    }

    public chatUI(String user, String guest, Socket socket, int port, int a) throws Exception {
        super();
        nameUser = user;
        nameGuest = guest;
        socketChat = socket;
        this.portServer = port;
        chat = new ChatRoom(socketChat, nameUser, nameGuest);
        chat.start();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    initial();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void appendToPane(JTextPane tp, String msg) {
        HTMLDocument doc = (HTMLDocument) tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
            tp.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateChat_receive(String msg) {
        appendToPane(MessagesPane, "<div class='left' style='width: 40%; background-color: #f1f0f0;'>" + "    "
                + msg + "<br>" + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "</div>");
    }

    public void updateChat_send(String msg) {
        appendToPane(MessagesPane,
                "<table class='bang' style='color: white; clear:both; width: 100%;'>" + "<tr align='right'>"
                        + "<td style='width: 59%; '></td>" + "<td style='width: 40%; background-color: #0084ff;'>"
                        + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "<br>" + msg
                        + "</td> </tr>" + "</table>");
    }

    public void updateChat_notify(String msg) {
        appendToPane(MessagesPane,
                "<table class='bang' style='color: white; clear:both; width: 100%;'>" + "<tr align='right'>"
                        + "<td style='width: 59%; '></td>" + "<td style='width: 40%; background-color: #f1c40f;'>" + msg
                        + "</td> </tr>" + "</table>");
    }

    public void updateChat_send_Symbol(String msg) {
        appendToPane(MessagesPane, "<table style='width: 100%;'>" + "<tr align='right'>"
                + "<td style='width: 59%;'></td>" + "<td style='width: 40%;'>" + msg + "</td> </tr>" + "</table>");
    }

    public void initial() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                try {
                    isStop = true;
                    frame.dispose();
                    chat.sendMessage(constants.closeChat);
                    chat.stopChat();
                    System.gc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        setResizable(false);
        setTitle("Chat Frame");
        setBounds(100, 100, 576, 560);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBounds(0, 0, 573, 67);
        contentPane.add(panel);
        panel.setLayout(null);

        JLabel lblNewLabel = new JLabel();
        lblNewLabel.setBounds(20, 5, 50, 50);
        panel.add(lblNewLabel);

        JLabel nameLabel = new JLabel(nameGuest);
        nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
        nameLabel.setToolTipText("");
        nameLabel.setBounds(96, 5, 200, 50);
        panel.add(nameLabel);

        JPanel panel_1 = new JPanel();
        MessagesPane = new JTextPane();
        MessagesPane.setEditable(false);
        MessagesPane.setContentType("text/html");
        MessagesPane.setBackground(Color.BLACK);
        MessagesPane.setForeground(Color.WHITE);
        MessagesPane.setFont(new Font("Courier New", Font.PLAIN, 18));
        appendToPane(MessagesPane, "<div class='clear' style='background-color:white'></div>");

        panel_1.setBounds(0, 66, 542, 323);
        panel_1.setLayout(null);
        JScrollPane scrollPane = new JScrollPane(MessagesPane);
        scrollPane.setBounds(20, 20, 522, 323);
        panel_1.add(scrollPane);
        contentPane.add(panel_1);

        JPanel panel_2 = new JPanel();
        panel_2.setBounds(0, 372, 573, 72);
        contentPane.add(panel_2);
        panel_2.setLayout(null);

        progressBar = new JProgressBar();
        progressBar.setBounds(20, 27, 520, 40);
        progressBar.setVisible(true);
        panel_2.add(progressBar);

        JPanel panel_3 = new JPanel();
        panel_3.setBounds(0, 446, 562, 73);
        contentPane.add(panel_3);
        panel_3.setLayout(null);

        btnSend = new JButton();
        btnSend.setBorder(new EmptyBorder(0, 0, 0, 0));
        btnSend.setContentAreaFilled(false);
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = Messages.getText();
                if (msg.isEmpty())
                    return;
                Messages.setText("");
                try {
                    chat.sendMessage(encode.sendMessage(msg));
                } catch (Exception err) {
                    err.printStackTrace();
                }
                updateChat_send(msg);
            }
        });
        btnSend.setBounds(498, 5, 50, 50);
        panel_3.add(btnSend);

        sendButton = new JButton();
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    isSendFile = true;
                    nameFile = fileChooser.getSelectedFile().getName();
                    File file = fileChooser.getSelectedFile();
                    try {
                        chat.sendMessage(encode.sendFile(nameFile));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        chat.sendFile(file);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        sendButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        sendButton.setContentAreaFilled(false);
        sendButton.setBounds(440, 5, 50, 50);
        panel_3.add(sendButton);

        Messages = new JTextField();
        Messages.setBounds(20, 0, 413, 58);
        panel_3.add(Messages);
        Messages.setColumns(10);
        Messages.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSend.doClick();
                }
            }
        });

        receiveStatus = new JLabel("");
        receiveStatus.setFont(new Font("Tahoma", Font.PLAIN, 18));
        receiveStatus.setBounds(47, 529, 465, 29);
        receiveStatus.setVisible(false);
        contentPane.add(receiveStatus);
    }
 
    private Image scaleImage(Image image, int w, int h) {
        return image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    public class ChatRoom extends Thread {
        private Socket connect;
        private boolean continueSendFile = true, finishReceive = false;
        private int Sent = 0, sizeOfData = 0, sizeOfFile = 0, Received = 0;
        private String nameReceivedFile = "";
        private InputStream fileSend;
        private file dataFile;

        public ChatRoom(Socket connection, String name, String guest) throws Exception {
            connect = new Socket();
            connect = connection;
            nameGuest = guest;
        }

        @Override
        public void run() {
            super.run();
            OutputStream out = null;
            while (!isStop) {
                try {
                    ObjectInputStream in = new ObjectInputStream(connect.getInputStream());
                    Object obj = in.readObject();
                    if (obj instanceof String) {
                        String msgObj = obj.toString();
                        if (msgObj.equals(constants.closeChat)) {
                            isStop = true;
                            JOptionPane.showMessageDialog(frame, "Your friend denied to connect with you!");
                            try {
                                isStop = true;
                                frame.dispose();
                                chat.sendMessage(constants.closeChat);
                                chat.stopChat();
                                System.gc();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            connect.close();
                            break;
                        }
                        if (decode.checkFile(msgObj)) {
                            isReceiveFile = true;
                            nameReceivedFile = msgObj.replace(constants.fileRequestOpen + " ", "");
                            File fileReceive = new File(URL_DIR + "/" + nameReceivedFile);
                            if (!fileReceive.exists()) {
                                fileReceive.createNewFile();
                            }
                            String msg = constants.fileRequestAckOpen + " " + Integer.toBinaryString(portServer);
                            sendMessage(msg);

                        } else if (decode.checkFeedback(msgObj)) {
                            sendButton.setEnabled(false);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        sendMessage(constants.dataBegin);
                                        updateChat_notify("You are sending file: " + nameFile);
                                        isSendFile = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else if (msgObj.equals(constants.dataBegin)) {
                            finishReceive = false;
                            receiveStatus.setVisible(true);
                            out = new FileOutputStream(URL_DIR + nameReceivedFile);
                        } else if (msgObj.equals(constants.dataClose)) {
                            updateChat_receive(
                                    "You receive file: " + nameReceivedFile + " with size " + Received + " KB");
                            Received = 0;
                            out.flush();
                            out.close();
                            receiveStatus.setVisible(false);

                            new Thread(this::showSaveFile).start();
                            finishReceive = true;
                        } else {
                            String message = decode.getMessage(msgObj);
                            updateChat_receive(message);
                        }
                    } else if (obj instanceof file data) {
                        ++Received;
                        out.write(data.data);
                    }
                } catch (Exception e) {
                    File fileTemp = new File(URL_DIR + "\\" + nameReceivedFile);
                    if (fileTemp.exists() && !finishReceive) {
                        fileTemp.delete();
                    }
                }
            }
        }

        private void getData(File file) throws Exception {
            if (file.exists()) {
                Sent = 0;
                dataFile = new file();
                sizeOfFile = (int) file.length();
                sizeOfData = sizeOfFile % 1024 == 0 ? (int) (file.length() / 1024)
                        : (int) (file.length() / 1024) + 1;
                fileSend = new FileInputStream(file);
            }
        }

        public void sendFile(File file) throws Exception {
            sendButton.setEnabled(false);
            getData(file);
            receiveStatus.setVisible(true);
            if (sizeOfData > constants.maxMsgSize / 1024) {
                receiveStatus.setText("File is too large...");
                fileSend.close();
                sendMessage(constants.dataClose);
                sendButton.setEnabled(true);
                isSendFile = false;
                fileSend.close();
                return;
            }

            progressBar.setVisible(true);
            progressBar.setValue(0);

            receiveStatus.setText("Sending ...");
            do {
                if (continueSendFile) {
                    continueSendFile = false;
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                fileSend.read(dataFile.data);
                                sendMessage(dataFile);
                                Sent++;
                                if (Sent == sizeOfData - 1) {
                                    int size = sizeOfFile - Sent * 1024;
                                    dataFile = new file(size);
                                }
                                progressBar.setValue(Sent * 100 / sizeOfData);
                                if (Sent >= sizeOfData) {
                                    fileSend.close();
                                    isSendFile = true;
                                    sendMessage(constants.dataClose);
                                    progressBar.setVisible(false);
                                    receiveStatus.setVisible(false);
                                    isSendFile = false;
                                    sendButton.setEnabled(true);
                                    updateChat_notify("File sent complete");
                                    fileSend.close();
                                }
                                continueSendFile = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } while (Sent < sizeOfData);
        }

        private void showSaveFile() {
            while (true) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = new File(fileChooser.getSelectedFile().getAbsolutePath() + "/" + nameReceivedFile);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                            Thread.sleep(1000);
                            InputStream input = new FileInputStream(URL_DIR + nameReceivedFile);
                            OutputStream output = new FileOutputStream(file.getAbsolutePath());
                            copyFileReceive(input, output, URL_DIR + nameReceivedFile);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(frame, "Error processing your file");
                        }
                        break;
                    } else {
                        int resultContinue = JOptionPane.showConfirmDialog(frame, "This file already exists, do you want to save file?", null, JOptionPane.YES_NO_OPTION);
                        if (resultContinue == 0)
                            continue;
                        else
                            break;
                    }
                }
            }
        }

        public synchronized void sendMessage(Object obj) throws Exception {
            ObjectOutputStream out = new ObjectOutputStream(connect.getOutputStream());
            if (obj instanceof String) {
                String message = obj.toString();
                out.writeObject(message);
                out.flush();
                if (isReceiveFile)
                    isReceiveFile = false;
            } else if (obj instanceof file) {
                out.writeObject(obj);
                out.flush();
            }
        }

        public void stopChat() {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void copyFileReceive(InputStream inputStr, OutputStream outputStr, String path) throws IOException {
        byte[] buffer = new byte[1024];
        int lenght;
        while ((lenght = inputStr.read(buffer)) > 0) {
            outputStr.write(buffer, 0, lenght);
        }
        inputStr.close();
        outputStr.close();
        File fileTemp = new File(path);
        if (fileTemp.exists()) {
            fileTemp.delete();
        }
    }
}
