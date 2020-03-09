package com.ketloz.lolbot;
import static org.junit.Assert.*;
import org.junit.Test;

public class MessageParserUnit {
    @Test
    public void addition_isCorrect() {
        MessageParser parser = new MessageParser();

        final String invalidMessage = "im not valid";
        final String validMessage = "!im valid";
        final String validCmdWithArgs = "!cmd args args args";

        assertFalse(parser.isValidMessage((invalidMessage)));
        assertTrue(parser.isValidMessage((validMessage)));

        final MessageParser.CommandBundle parseResult = parser.parseMessage(validCmdWithArgs);

        assertEquals("cmd", parseResult.command);
        assertArrayEquals(new String[] { "args", "args", "args" }, parseResult.commandArguments.toArray());
    }
}