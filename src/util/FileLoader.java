package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper class for file loading.
 */
public class FileLoader {
    /**
     * Tries to load the given file.
     *
     * @param fileName Name and path to the file.
     * @return Source code of the file.
     * @throws IOException When the resource cannot be loaded.
     */
    public static String loadResource(String fileName) throws IOException {
        String result;
        try (InputStream in = FileLoader.class.getClass().getResourceAsStream(fileName)) {
            Scanner scanner = new Scanner(in, "UTF-8");
            result = scanner.useDelimiter("\\A").next();
            scanner.close();
        }
        return result;
    }

    /**
     * Loads the lines in a file into a list of strings.
     *
     * @param fileName Name and path to the file.
     * @return List of strings containing the individual lines of the file.
     * @throws IOException When the resource cannot be loaded.
     */
    public static List<String> readAllLines(String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(FileLoader.class.getClass().getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }
}