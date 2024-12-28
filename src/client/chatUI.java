package client;

import data.*;
import utils.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class chatUI extends JFrame {
    private static String URL_DIR = System.getProperty("user.dir");
    private Socket socketChat;
    private String userName = "", guestName = "", nameFile = "";
    public boolean isStop = false, isSendFile = false, isReceiveFile = false;
    private ChatRoom chat;
    private int portServer = 0;
    private ArrayList<String> chatHistory = new ArrayList<>();

    @Serial
    private static final long serialVersionUID = 1L;
    private JTextField Messages;
    private JTextPane MessagesPane;
    private JButton sendFileButton;
    private JLabel receiveStatus;
    private chatUI frame = this;
    private JProgressBar progressBar;
    private JButton sendButton;

    public chatUI(String user, String guest, Socket socket, int port) throws Exception {
        super();
        userName = user;
        guestName = guest;
        socketChat = socket;
        frame = new chatUI(user, guest, socket, port, port);
        frame.setVisible(true);
    }

    public chatUI(String user, String guest, Socket socket, int port, int a) throws Exception {
        super();
        userName = user;
        guestName = guest;
        socketChat = socket;
        this.portServer = port;
        chat = new ChatRoom(socketChat, userName, guestName);
        chat.start();
        EventQueue.invokeLater(() -> {
            try {
                initial();
                loadChatHistory();
            } catch (Exception e) {
                e.printStackTrace();
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
        String messageHtml = "<div style='margin: 5px;'>"
                + "<div style='float: left; background-color: #f1f0f0; padding: 8px; border-radius: 8px; max-width: 50%;'>"
                + msg + "<br><span style='color: gray; font-size: 12px;'>"
                + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "</span>"
                + "<a href='delete:" + chatHistory.size() + "' style='margin-left: 10px; color: #ff4444;'>[Delete]</a>"
                + "</div><div style='clear: both;'></div></div>";
        appendToPane(MessagesPane, messageHtml);
    }

    public void updateChat_send(String msg) {
        String messageHtml = "<div style='margin: 5px;'>"
                + "<div style='float: right; background-color: #0084ff; color: white; padding: 8px; border-radius: 8px; max-width: 50%;'>"
                + msg + "<br><span style='font-size: 12px;'>"
                + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "</span>"
                + "<a href='delete:" + chatHistory.size() + "' style='margin-left: 10px; color: #ffffff;'>[Delete]</a>"
                + "</div><div style='clear: both;'></div></div>";
        appendToPane(MessagesPane, messageHtml);
    }

    public void updateChat_notify(String msg) {
        appendToPane(MessagesPane,
                "<table class='bang' style='color: white; clear:both; width: 100%;'>" + "<tr align='right'>"
                        + "<td style='width: 59%; '></td>" + "<td style='width: 40%; background-color: #f1c40f;'>" + msg
                        + "</td> </tr>" + "</table>");
    }

    private void loadChatHistory() {
        chatHistory.clear();
        String[] currentList = {userName, guestName};
        Arrays.sort(currentList);
        String filename = currentList[0] + "_" + currentList[1] + ".txt";

        String historyFile = URL_DIR + "\\src\\history\\" + filename;
        File historyDir = new File(historyFile);
        if (!historyDir.exists()) {
            historyDir.mkdirs();
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(historyDir));
            String chatLine;
            while ((chatLine = br.readLine()) != null) {
                String[] chatComponents = chatLine.split("```");
                if (chatComponents.length == 2) {
                    chatHistory.add(chatLine + "\n");
                    if (chatComponents[0].equals(userName)) {
                        updateChat_send(chatComponents[1]);
                    }
                    else {
                        updateChat_receive(chatComponents[1]);
                    }
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToHistory(String sender, String message) {
        String[] currentList = {userName, guestName};
        Arrays.sort(currentList);
        String filename = currentList[0] + "_" + currentList[1] + ".txt";
        String historyFile = URL_DIR + "\\src\\history\\" + filename;

        try {
            FileWriter writer = new FileWriter(historyFile, true); // append mode
            writer.write(sender + "```" + message + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteMessageFromHistory(int messageIndex) {
        if (messageIndex >= 0 && messageIndex < chatHistory.size()) {
            chatHistory.remove(messageIndex);
            String[] currentList = {userName, guestName};
            Arrays.sort(currentList);
            String filename = currentList[0] + "_" + currentList[1] + ".txt";
            String historyFile = URL_DIR + "\\src\\history\\" + filename;

            try {
                FileWriter writer = new FileWriter(historyFile);
                for (String message : chatHistory) {
                    writer.write(message + "\n");
                }
                writer.close();
                try {
                    chat.sendMessage(constants.deleteMessage + messageIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                refreshChatDisplay();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error deleting message: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshChatDisplay() {
        MessagesPane.setText("");
        appendToPane(MessagesPane, "<div style='background-color:white'></div>");
        for (String chatLine : chatHistory) {
            String[] chatComponents = chatLine.split("```");
            if (chatComponents.length == 2) {
                if (chatComponents[0].equals(userName)) {
                    updateChat_send(chatComponents[1]);
                } else {
                    updateChat_receive(chatComponents[1]);
                }
            }
        }
    }

    private void setupDeleteLinks() {
        MessagesPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc.startsWith("delete:")) {
                    try {
                        int messageIndex = Integer.parseInt(desc.substring(7));
                        deleteMessageFromHistory(messageIndex - 1);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
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
        setTitle("Chat with " + guestName);
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

        JLabel nameLabel = new JLabel(guestName);
        nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
        nameLabel.setToolTipText("");
        nameLabel.setBounds(20, 5, 250, 50);
        panel.add(nameLabel);

        JPanel panel1 = new JPanel();
        MessagesPane = new JTextPane();
        MessagesPane.setEditable(false);
        MessagesPane.setContentType("text/html");
        MessagesPane.setBackground(Color.BLACK);
        MessagesPane.setForeground(Color.WHITE);
        MessagesPane.setFont(new Font("Courier New", Font.PLAIN, 18));
        appendToPane(MessagesPane, "<div class='clear' style='background-color:white'></div>");
        setupDeleteLinks();

        panel1.setBounds(0, 66, 542, 323);
        panel1.setLayout(null);
        JScrollPane scrollPane = new JScrollPane(MessagesPane);
        scrollPane.setBounds(20, 20, 522, 323);
        panel1.add(scrollPane);
        contentPane.add(panel1);

        JPanel panel2 = new JPanel();
        panel2.setBounds(0, 372, 573, 72);
        contentPane.add(panel2);
        panel2.setLayout(null);

        progressBar = new JProgressBar();
        progressBar.setBounds(20, 27, 520, 40);
        progressBar.setVisible(true);
        panel2.add(progressBar);

        JPanel panel3 = new JPanel();
        panel3.setBounds(0, 446, 562, 73);
        contentPane.add(panel3);
        panel3.setLayout(null);

        sendButton = new JButton();
        sendButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        sendButton.setContentAreaFilled(false);
        sendButton.setIcon(new ImageIcon(new ImageIcon(
                Objects.requireNonNull(chatUI.class.getResource("/images/send.png"))).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));

        sendButton.addActionListener(_ -> {
            String msg = Messages.getText();
            if (msg.isEmpty())
                return;
            Messages.setText("");
            try {
                chat.sendMessage(encode.sendMessage(msg));
                saveMessageToHistory(userName, msg);
            } catch (Exception err) {
                err.printStackTrace();
            }
            updateChat_send(msg);
        });
        sendButton.setBounds(498, 5, 50, 50);
        panel3.add(sendButton);

        sendFileButton = new JButton();
        sendFileButton.addActionListener(_ -> {
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
        });
        sendFileButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        sendFileButton.setContentAreaFilled(false);
        sendFileButton.setIcon(new ImageIcon(new ImageIcon(
                Objects.requireNonNull(chatUI.class.getResource("/images/document.png"))).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
        sendFileButton.setBounds(440, 5, 50, 50);
        panel3.add(sendFileButton);

        Messages = new JTextField();
        Messages.setBounds(20, 0, 413, 58);
        panel3.add(Messages);
        Messages.setColumns(10);
        Messages.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        receiveStatus = new JLabel("");
        receiveStatus.setFont(new Font("Tahoma", Font.PLAIN, 18));
        receiveStatus.setBounds(47, 529, 465, 29);
        receiveStatus.setVisible(false);
        contentPane.add(receiveStatus);
    }

    public class ChatRoom extends Thread {
        private Socket connect;
        private boolean continueSendFile = true, finishReceive = false;
        private int Sent = 0, sizeOfData = 0, sizeOfFile = 0, Received = 0;
        private String nameReceivedFile = "";
        private InputStream fileSend;
        private file dataFile;

        public ChatRoom(Socket connection, String name, String guest) {
            connect = connection;
            guestName = guest;
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
                        else if (msgObj.startsWith(constants.deleteMessage)) {
                            String indexStr = msgObj.substring(constants.deleteMessage.length());
                            int messageIndex = Integer.parseInt(indexStr);
                            if (messageIndex >= 0 && messageIndex < chatHistory.size()) {
                                chatHistory.remove(messageIndex);
                                String[] currentList = {userName, guestName};
                                Arrays.sort(currentList);
                                String filename = currentList[0] + "_" + currentList[1] + ".txt";
                                String historyFile = URL_DIR + "\\src\\history\\" + filename;

                                FileWriter writer = new FileWriter(historyFile);
                                for (String message : chatHistory) {
                                    writer.write(message + "\n");
                                }
                                writer.close();

                                // Refresh UI on EDT
                                SwingUtilities.invokeLater(() -> refreshChatDisplay());
                            }
                        }
                        else if (decode.checkFile(msgObj)) {
                            isReceiveFile = true;
                            nameReceivedFile = msgObj.replace(constants.fileRequestOpen + " ", "");
                            File fileReceive = new File(URL_DIR + "/" + nameReceivedFile);
                            if (!fileReceive.exists()) {
                                fileReceive.createNewFile();
                            }
                            String msg = constants.fileRequestAckOpen + " " + Integer.toBinaryString(portServer);
                            sendMessage(msg);
                        }
                        else if (decode.checkFeedback(msgObj)) {
                            sendFileButton.setEnabled(false);

                            new Thread(() -> {
                                try {
                                    sendMessage(constants.dataBegin);
                                    updateChat_notify("You are sending file: " + nameFile);
                                    isSendFile = false;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                        else if (msgObj.equals(constants.dataBegin)) {
                            finishReceive = false;
                            receiveStatus.setVisible(true);
                            out = new FileOutputStream(URL_DIR + nameReceivedFile);
                        }
                        else if (msgObj.equals(constants.dataClose)) {
                            updateChat_receive(
                                    "You receive file: " + nameReceivedFile + " with size " + Received + " KB");
                            Received = 0;
                            out.flush();
                            out.close();
                            receiveStatus.setVisible(false);

                            new Thread(this::showSaveFile).start();
                            finishReceive = true;
                        }
                        else {
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
            sendFileButton.setEnabled(false);
            getData(file);
            receiveStatus.setVisible(true);
            if (sizeOfData > constants.maxMsgSize / 1024) {
                receiveStatus.setText("File is too large...");
                fileSend.close();
                sendMessage(constants.dataClose);
                sendFileButton.setEnabled(true);
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
                    new Thread(() -> {
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
                                sendFileButton.setEnabled(true);
                                updateChat_notify("File sent complete");
                                fileSend.close();
                            }
                            continueSendFile = true;
                        } catch (Exception e) {
                            e.printStackTrace();
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
        int length;
        while ((length = inputStr.read(buffer)) > 0) {
            outputStr.write(buffer, 0, length);
        }
        inputStr.close();
        outputStr.close();
        File fileTemp = new File(path);
        if (fileTemp.exists()) {
            fileTemp.delete();
        }
    }
}
