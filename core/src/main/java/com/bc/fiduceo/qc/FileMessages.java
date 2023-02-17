package com.bc.fiduceo.qc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class FileMessages {

    private final HashMap<String, List<String>> messagesMap;

    FileMessages() {
        messagesMap = new HashMap<>();
    }

    HashMap<String, List<String>> getMessageMap() {
        return messagesMap;
    }

    public void add(String fileName, String message) {
        final List<String> messages = messagesMap.computeIfAbsent(fileName, k -> new ArrayList<>());
        messages.add(message);
    }
}
