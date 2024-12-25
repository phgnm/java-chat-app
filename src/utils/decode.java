package utils;

import data.user;

import java.util.ArrayList;
import java.util.Arrays;

public class decode {
    public static ArrayList<String> getUser(String message) {
        ArrayList<String> user = new ArrayList<>();
        String[] split = message.split(" ");
        if (split.length > 1) {
            if (split[1].equals(constants.signUp) || split[1].equals(constants.login)) {
                String[] splitUser = split[0].split("-");
                if (splitUser.length == 3) {
                    user.addAll(Arrays.asList(splitUser));
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
        return user;
    }
    public static String getLoginCmd(String message) {
        String[] split = message.split(" ");
        return split[split.length - 1];
    }
    public static ArrayList<user> getUserList(String message) {
        ArrayList<user> users = new ArrayList<>();
        String[] split = message.split(" ");
        if (split.length > 1) {
            if (split[0].equals(constants.accept)) {
                for (int i = 1; i < split.length; i++) {
                    String[] splitUser = split[i].split("-");
                    if (splitUser.length == 3) {
                        user newUser = new user();
                        newUser.setName(splitUser[0]);
                        newUser.setHost(splitUser[1].replace("/", ""));
                        newUser.setPort(Integer.parseInt(splitUser[2]));
                        users.add(newUser);
                    }
                }
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
        return users;
    }
    public static ArrayList<user> updateOnline(ArrayList<user> users, String message) {
        String[] split = message.split(" ");
        if (split.length > 1) {
            if (split[1].equals(constants.onlineServer)) {
                return users;
            }
            if (split[1].equals(constants.offlineServer)) {
                for (int i = 0; i < users.size(); i++) {
                    if (split[0].equals(users.get(i).getUsername())) {
                        users.remove(i);
                        break;
                    }
                }
            }
        }
        return users;
    }
    public static String getNameRequest(String message) {
        String [] split = message.split(" ");
        if (split.length > 1 && split[1].equals(constants.startChat)) {
                return split[0];
        }
        return null;
    }
    public static String getMessage(String message) {
        String[] split = message.split(" ");
        if (split[0].equals(constants.messageOpen))
            return message.replace(constants.messageOpen + " ", "");
        return null;
    }
    public static boolean checkFile(String name) {
        String[] split = name.split(" ");
        return split.length > 1 && split[0].equals(constants.fileRequestOpen);
    }
    public static boolean checkFeedback (String message) {
        String[] split = message.split(" ");
        return split.length > 1 && split[0].equals(constants.fileRequestAckOpen);
    }
}