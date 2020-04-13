package com.ketloz.lolbot;

import java.util.Arrays;
import java.util.List;

public class CommandBundle {
    private String command;
    private List<String> commandArguments;

    public CommandBundle(String command, String[] arguments) {
        this.command = command;
        this.commandArguments = Arrays.asList(arguments);
    }

    public CommandBundle(String command, List<String> arguments) {
        this.command = command;
        this.commandArguments = arguments;
    }

    public List<String> getCommandArguments() {
        return this.commandArguments;
    }

    public String getCommand() {
        return this.command;
    }
}
