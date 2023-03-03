package com.bc.fiduceo.qc;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileMessagesTest {

    private FileMessages fileMessages;

    @Before
    public void setUp() {
        fileMessages = new FileMessages();
    }

    @Test
    public void testGetMessageMap_no_input() {
        final HashMap<String, List<String>> messageMap = fileMessages.getMessageMap();
        assertEquals(0, messageMap.size());
    }

    @Test
    public void testGetMessageMap_one_input_one_message() {
        fileMessages.add("test_file_1", "something was wrong");

        final HashMap<String, List<String>> messageMap = fileMessages.getMessageMap();
        assertEquals(1, messageMap.size());

        final List<String> messages = messageMap.get("test_file_1");
        assertEquals(1, messages.size());
        assertEquals("something was wrong", messages.get(0));
    }

    @Test
    public void testGetMessageMap_one_input_two_messages() {
        fileMessages.add("test_file_1", "something was wrong");
        fileMessages.add("test_file_1", "a second issue");

        final HashMap<String, List<String>> messageMap = fileMessages.getMessageMap();
        assertEquals(1, messageMap.size());

        final List<String> messages = messageMap.get("test_file_1");
        assertEquals(2, messages.size());
        assertEquals("something was wrong", messages.get(0));
        assertEquals("a second issue", messages.get(1));
    }

    @Test
    public void testGetMessageMap_two_inputs_one_message() {
        fileMessages.add("test_file_1", "something was wrong");
        fileMessages.add("test_file_2", "yo, here too!");

        final HashMap<String, List<String>> messageMap = fileMessages.getMessageMap();
        assertEquals(2, messageMap.size());

        List<String> messages = messageMap.get("test_file_1");
        assertEquals(1, messages.size());
        assertEquals("something was wrong", messages.get(0));

        messages = messageMap.get("test_file_2");
        assertEquals(1, messages.size());
        assertEquals("yo, here too!", messages.get(0));
    }
}
