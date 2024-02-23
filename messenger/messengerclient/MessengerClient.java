package messenger.messengerclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MessengerClient {

    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream streamToServer;
    private ObjectInputStream streamFromServer;

    public MessengerClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = null;
        this.streamToServer = null;
        this.streamFromServer = null;
    }

    private void connect() throws IOException {
        try {
            this.socket = new Socket(this.host, this.port);
        } catch (UnknownHostException e) {
            System.out.println("Unrecognised host: " + this.host);
            System.out.println("Aborting...");
            // System.exit(1); // kill the program here
            throw new IOException(e); // Re-raise in this.run()
        }
        this.streamToServer = new ObjectOutputStream(
            this.socket.getOutputStream()
        );
        this.streamFromServer = new ObjectInputStream(
            this.socket.getInputStream()
        );
    }

    private void sendMessageToServer(String message) {
        try {
            this.streamToServer.writeObject(message);
            String result = (String) this.streamFromServer.readObject();
            System.out.println("[messenger] "+result);
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("Encountered IOException while sending message to server.");
        } catch (ClassNotFoundException e) {
            // e.printStackTrace();
            System.out.println("Unrecognised class by server.");
        } 
    }

    private String getMessageFromTerminal() {
        Scanner scanner = new Scanner(System.in);
        // System.out.println("Input message here: ");
        String message = scanner.nextLine();
        //scanner.close(); // commented out so the program can run forever
        return message;
    }

    public void run() {
        try {
            this.connect();
            System.out.println("Input messages here: ");
            while (true) {
                String message = this.getMessageFromTerminal();
                this.sendMessageToServer(message);
            }
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("Encountered error upon connecting to server.");
        }
    }
}
