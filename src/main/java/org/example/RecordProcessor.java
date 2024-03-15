package org.example;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RecordProcessor {

    private static final int MAX_RECORDS_PER_FILE = 100;

    public static void main(String[] args) {
        String sourceDirectory = "instant_data_files";
        String destinationDirectory = "processed_files";

        File sourceDir = new File(sourceDirectory);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.err.println("Source directory doesn't exist or is not a directory.");
            return;
        }

        File destinationDir = new File(destinationDirectory);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        for (File file : sourceDir.listFiles()) {
            if (file.isFile()) {
                processFile(file, destinationDirectory);
            }
        }
    }

    private static void processFile(File file, String destinationDirectory) {
        List<String> records = new ArrayList<>();
        String destinationFileName = destinationDirectory + File.separator + file.getName().replace(".log", "-0001.log");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
                if (records.size() == MAX_RECORDS_PER_FILE) {
                    writeToFile(records, destinationFileName);
                    records.clear();
                    destinationFileName = getNextFileName(destinationFileName);
                }
            }
            if (!records.isEmpty()) {
                writeToFile(records, destinationFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(List<String> records, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, true))) {
            for (String record : records) {
                writer.println(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getNextFileName(String fileName) {
        String baseName = fileName.substring(0, fileName.lastIndexOf("-"));
        int index = Integer.parseInt(fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf("."))) + 1;
        return String.format("%s-%04d.log", baseName, index);
    }
}
