package messenger.messengerserver;  

import java.io.IOException; 
import java.io.ObjectInputStream; 
import java.io.ObjectOutputStream; 
import java.net.ServerSocket; 
import java.net.Socket;

public class MessengerServer {

    private int port;
    private ObjectInputStream streamFromClient;
    private ObjectOutputStream streamToClient;
    private ServerSocket serverSocket;


    public MessengerServer(int port) {
        this.port = port;
        this.streamFromClient = null;
        this.streamToClient = null;
        this.serverSocket = null;
    }

    private void setup() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
    }

    private void awaitClient() throws IOException {
        System.out.println("Waiting for client...");
        Socket socket = this.serverSocket.accept();
        // Client connected

        System.out.println("Client connected.");
        
        // Setup streams
        this.streamFromClient = new ObjectInputStream(
            socket.getInputStream()
        );
        this.streamToClient = new ObjectOutputStream(
            socket.getOutputStream()
        );
        
    }

    private void processResult() throws IOException {
        // loop until break
        while (true) {
            String result = "**NO RESULT**";
            // Read data from the client
            try {
                String readString = (String) streamFromClient.readObject();
                System.out.println("Received from client: " + readString);

                // Process the data and send it back to the client
                result = readString;
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
                System.out.println("Unrecognised class.");
            }

            // Send the result back to the client
            this.streamToClient.writeObject(result);
            // Flush the stream to ensure the result is sent immediately
            this.streamToClient.flush();
        }
    }

    public void run() {
        try {
            this.setup();
            while (true) {
                this.awaitClient();
                this.processResult();
            }
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("Encountered IOException; closing...");
        }
    }
}
