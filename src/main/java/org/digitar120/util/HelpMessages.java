package org.digitar120.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class HelpMessages {
    private HelpMessages(){}

    public static String printHelpMessage() throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/main/resources/help.txt")));
    }
}
