package utils;

import java.util.regex.*;

public class encode {
    private static Pattern messageCheck = Pattern.compile("[^<>]*[<>]");
    public static String signUp(String name, String port, String password) {
        return name + '-' + port + '-' + password + ' ' + constants.signUp;
    }
    public static String login(String name, String port, String password) {
        return name + '-' + port + '-' + password + ' ' + constants.login;
    }
    public static String onlineRequest(String name) {
        return name + ' ' + constants.onlineServer;
    }
    public static String offlineRequest(String name) {
        return name + ' ' + constants.offlineServer;
    }
    public static String chatRequest(String name) {
        return name + ' ' + constants.startChat;
    }
    public static String sendMessage(String message) {
        Matcher findMessage = messageCheck.matcher(message);
        StringBuilder result = new StringBuilder();
        while (findMessage.find()) {
            String subMessage = findMessage.group(0);
            int begin = subMessage.length();
            char next = message.charAt(begin - 1);
            result.append(subMessage);
            subMessage = message.substring(begin);
            message = subMessage;
            findMessage = messageCheck.matcher(message);
        }
        result.append(message);
        return constants.messageOpen + ' ' + result;
    }
    public static String sendFile(String name) {
        return constants.fileRequestOpen + ' ' + name;
    }
}