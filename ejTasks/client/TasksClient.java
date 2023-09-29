import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TasksClient {
    public static void main(String[] a_args) {
        System.err.println("Usage: java TasksClient [host] [port]");

        if (a_args.length > 2) {
        	System.exit(-1);
        }
        
        String serverHost = (a_args.length >= 1) ? a_args[0] : "localhost";
        int serverPort = (a_args.length >= 2) ? Integer.parseInt(a_args[1]) : 8000;

        try (Socket socket = new Socket(serverHost, serverPort);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
            ) {

            System.out.println("Connected to the server. Sending username.");
            String username = UsernameProvider.getUsername();
            writer.write(username + "\n");
            writer.flush();

            Thread serverReaderThread = new Thread(() -> {
                try (InputStream input = socket.getInputStream()) {
                	byte[] buffer = new byte[1024]; // buffer size doesn't matter too much
                	int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                    	String s = new String(buffer, 0, bytesRead);
                        System.out.print(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            });
            serverReaderThread.start();
            
            while (true) {
                String userInput = consoleReader.readLine();
                if (userInput == null) {
                    break; // Exit if the user closes the console input
                }
                writer.write(userInput + "\n");
                writer.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
