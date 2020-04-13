package com.ketloz.lolbot;
import static org.junit.Assert.*;
import org.junit.Test;

public class MessageParserUnit {
    @Test
    public void canParseCommandsCorrectly() {
        MessageParser parser = new MessageParser();

        final String invalidMessage = "im not valid";
        final String validMessage = "!im valid";
        final String validCmdWithArgs = "!cmd args args args";

        assertFalse(parser.isValidMessage((invalidMessage)));
        assertTrue(parser.isValidMessage((validMessage)));

        final CommandBundle parseResult = parser.parseMessage(validCmdWithArgs);

        assertEquals("cmd", parseResult.getCommand());
        assertArrayEquals(new String[] { "args", "args", "args" }, parseResult.getCommandArguments().toArray());
    }
}