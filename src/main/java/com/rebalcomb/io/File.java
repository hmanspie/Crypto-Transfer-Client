package com.rebalcomb.io;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class File {
    public static void writeFile(String text, String file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(text);
        fileWriter.close();
    }
    public static String readFile(String file) throws IOException {
        Path fileName = Path.of(file);
        return Files.readString(fileName);
    }
}
