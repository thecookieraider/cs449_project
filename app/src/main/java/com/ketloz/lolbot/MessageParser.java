package com.ketloz.lolbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageParser {
    private void exec(String command, List<String> args) {

    }

    public void parseMessage(String message) {
        if (message.startsWith("!")) {
            final String[] cmdAndArgs = message.split(" ");
            final String command = args[0];
            final List<String> args = Arrays.asList(cmdAndArgs).subList(1, cmdAndArgs.length);
            this.exec(command, args);
        }
    }
}
