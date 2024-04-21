package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

import shared.Message;

public class ChatServerHandler implements Runnable{

    private Socket socket;
    private ObjectInputStream streamFromClient;
    private ObjectOutputStream streamToClient;
    private ConnectionPool connectionPool; // for broadcast message
    private ChatServer server;  
    private String username;

    public ChatServerHandler(Socket socket, ConnectionPool pool, ChatServer chatServer){
        this.socket = socket;
        this.connectionPool = pool;
        this.server = chatServer;

        try {
            this.streamToClient = new ObjectOutputStream(socket.getOutputStream());
            this.streamToClient.flush(); // Ensure any leftover data is cleared
            this.streamFromClient = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Failed setting up I/O streams: " + e.getMessage());
            close(); // Ensure cleanup if an exception occurs during setup
        }
    
    }


    private void registerUser() throws IOException, ClassNotFoundException {
        try {
            this.username = (String) this.streamFromClient.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed registering user " + this.username);
            // Let the run() function handle this error
            throw e;
        }
        this.connectionPool.broadcast(
            this.getUserMessage("joined the chat!")
        );
    }


    private Message getUserMessage(String messageBody) {
        return new Message(messageBody, this.username);
    }

    private void close() {
        if (username != null) {
            connectionPool.removeUser(username);
            connectionPool.broadcast(getUserMessage(username + " just left the chat."));
        }
        try {
            if (streamFromClient != null) streamFromClient.close();
            if (streamToClient != null) streamToClient.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public void sendMessageToClient(Message msg){
        try {
            // output message object
            streamToClient.writeObject(msg);
        } catch (IOException e) {
            System.err.println("Failed sending message `" + msg.getMessageBody()
                               + "` to `" + this.username + "`.");
        }
    }

    private String getGroupCommand(String userInput) {
        if (userInput.toUpperCase().startsWith("CREATE")) {
            return "CREATE";
        } else if (userInput.toUpperCase().startsWith("JOIN ")) {
            return "JOIN";
        } else if (userInput.toUpperCase().startsWith("LEAVE ")) {
            return "LEAVE";
        } else if (userInput.toUpperCase().startsWith("SEND ")) {
            return "SEND";
        } else {
            return ""; // Default case for normal messages or unrecognized commands
        }
    }

    private void parseCommand(String userInput) {
        String[] tokens = userInput.trim().split("\\s+");
        switch (tokens[0].toUpperCase()) {
            case "CREATE":
                if (tokens.length > 1) server.createGroup(tokens[1]);
                break;
            case "JOIN":
                if (tokens.length > 1) server.joinGroup(tokens[1], this.username);
                break;
            case "LEAVE":
                if (tokens.length > 1) server.leaveGroup(tokens[1], this.username);
                break;
            case "SEND":
                if (tokens.length > 2) {
                    String groupName = tokens[1];
                    String message = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length));
                    server.sendMessageToGroup(groupName, message, this.username);
                }
                break;
            default:
                // Optionally handle unknown commands or log them
                break;
        }
    }

private void handleMessage(Message message) {
    System.out.println("Server received: " + message.getMessageBody() + " from " + message.getUser());
    if (message.getMessageBody().equalsIgnoreCase("exit")) return;
    connectionPool.broadcast(message);
}

@Override
public void run() {
    try {
        this.registerUser();

        while (true) {
            String userInput = (String) streamFromClient.readObject(); // Assuming all user inputs are Strings for simplicity
            String command = getGroupCommand(userInput);
            switch (command) {
                case "CREATE":
                case "JOIN":
                case "LEAVE":
                case "SEND":
                    parseCommand(userInput); // Delegate to parseCommand for detailed processing
                    break;
                default:
                    handleMessage(new Message(userInput, this.username)); // Treat as a normal message if not a command
                    break;
            }
        }
    } catch (IOException | ClassNotFoundException e) {
        System.err.println("Abruptly interrupted communication with `" + this.username + "`: " + e.getMessage());
        e.printStackTrace();
    } finally {
        this.close();
    }
}


    public String getClientName() {
        return this.username;
    }
}
