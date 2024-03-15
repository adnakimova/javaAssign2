package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XmlToJsonServlet {
    private static final String FILE_PREFIX = "Information-";
    private static final String FILE_EXTENSION = ".log";

    public static void main(String[] args) throws Exception {
        int port = 9091;
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                     BufferedReader br = new BufferedReader(isr);
                     OutputStream os = exchange.getResponseBody()) {

                    StringBuilder xmlBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        xmlBody.append(line).append("\n");
                    }

                    String jsonString = convertXMLtoJSON(xmlBody.toString());

                    // Save JSON to file
                    saveToFile(jsonString);

                    exchange.sendResponseHeaders(200, jsonString.length());
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(jsonString.getBytes());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, 0);
                }
            } else {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
            }
        }
    }

    private static String convertXMLtoJSON(String xmlString) {
        JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
        return xmlJSONObj.toString(4);
    }

    private static void saveToFile(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        String type = jsonObject.getJSONObject("Data").getString("Type");
        String fileName = FILE_PREFIX + getCurrentDate() + FILE_EXTENSION;
        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file, true);
             PrintWriter out = new PrintWriter(writer)) {
            long recordsCount = getRecordCount(file);
            // Write record count only if it's the first record in the file
            if (recordsCount == 0) {
                writer.write("Record count: " + (recordsCount + 1) + "\n");
            }
            out.println(jsonString);
            System.out.println("Saved to " + fileName);
            System.out.println("Number of records in " + fileName + ": " + (recordsCount + 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long getRecordCount(File file) {
        long count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Record count:")) {
                    count = Long.parseLong(line.substring("Record count: ".length()).trim());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }



    private static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
}
