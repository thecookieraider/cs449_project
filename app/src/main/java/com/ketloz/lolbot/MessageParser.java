package com.ketloz.lolbot;

import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageParser {
    public class CommandBundle {
        public String command;
        public List<String> commandArguments;

        public CommandBundle(String command, String[] arguments) {
            this.command = command;
            this.commandArguments = Arrays.asList(arguments);
        }

        public CommandBundle(String command, List<String> arguments) {
            this.command = command;
            this.commandArguments = arguments;
        }
    }

    private final static String LOGGER_NAME = "MessageParser";
    private final static Logger LOGGER = Logger.getLogger(MessageParser.LOGGER_NAME);
    private void exec(String command, List<String> args) {

    }

    private static void logMethodEntry(String methodName) {
        MessageParser.LOGGER.entering("MessageParser", methodName);
    }

    private static void logMethodExit(String methodName) {
        MessageParser.LOGGER.exiting("MessageParser", methodName);
    }

    public CommandBundle parseMessage(String message) {
        MessageParser.logMethodEntry("parseMessage");
        CommandBundle returnValue = null;

        if (message.startsWith("!")) {
            final String[] cmdAndArgs = message.split(" ");
            final String command = cmdAndArgs[0];
            final List<String> args = Arrays.asList(cmdAndArgs).subList(1, cmdAndArgs.length);
            returnValue = new CommandBundle(command, args);
        }

        MessageParser.logMethodExit("parseMessage");
        return returnValue;
    }

    public boolean isValidMessage(String message) {
        MessageParser.LOGGER.log(Level.INFO, "Checking if message is valid");

        final boolean messageIsValid = message.startsWith("!");
        MessageParser.LOGGER.log(Level.INFO, "Message is valid: " + messageIsValid);

        return messageIsValid;
    }
}
