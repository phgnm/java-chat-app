package data;

import utils.constants;

import java.io.*;

@SuppressWarnings("serial")
public class file implements Serializable {
    @SuppressWarnings("unused")
    private String openTags = constants.dataOpen;
    @SuppressWarnings("unused")
    private String closeTags = constants.dataClose;
    public byte[] data;

    public file() {
        data = new byte[constants.maxMsgSize];
    }

    public file(int size) {
        data = new byte[size];
    }
}
