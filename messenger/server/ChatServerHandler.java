package server;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ChatServerHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream streamFromClient;
    private ObjectOutputStream streamToClient;
    private ConnectionPool connectionPool;
    private String username;

    public ChatServerHandler(Socket clientSocket, ConnectionPool connectionPool) {
        this.clientSocket = clientSocket;
        this.connectionPool = connectionPool;
        this.username = null;
    }

    private void setup() throws IOException {
        this.streamFromClient = new ObjectInputStream(this.clientSocket.getInputStream());
        this.streamToClient = new ObjectOutputStream(this.clientSocket.getOutputStream());
    }

    private void registerUser() throws IOException, ClassNotFoundException {
        Message message = (Message) this.streamFromClient.readObject();
        this.username = message.getMessageBody();
        System.out.println("User registered: " + this.username);
        this.connectionPool.broadcast(new Message("SYSTEM", "", this.username + " joined the chat.", ""));
    }

    @Override
    public void run() {
        try {
            this.setup();
            this.registerUser();

            while (true) {
                Message message = (Message) this.streamFromClient.readObject();
                String messageType = message.getMessageType();
                String target = message.getTarget();
                String messageBody = message.getMessageBody();
                String topic = message.getTopic();

                System.out.println("Received message: " + message);

                switch (messageType) {
                    case "CREATE":
                        System.out.println("Processing CREATE");
                        this.connectionPool.createGroup(target);
                        break;
                    case "JOIN":
                        System.out.println("Processing JOIN");
                        this.connectionPool.joinGroup(target, this);
                        break;
                    case "LEAVE":
                        System.out.println("Processing LEAVE");
                        this.connectionPool.leaveGroup(target, this);
                        break;
                    case "REMOVE":
                        System.out.println("Processing REMOVE");
                        this.connectionPool.removeGroup(target);
                        break;
                    case "SEND":
                        System.out.println("Processing SEND message");
                        if (this.connectionPool.isGroupName(target)) {
                            System.out.println("Broadcasting to group: " + target);
                            this.connectionPool.broadcastToGroup(message, target);
                            this.connectionPool.forwardToSubscribers(message);
                        } else {
                            System.out.println("Sending to client: " + target);
                            this.connectionPool.sendToClient(message, target);
                        }
                        break;
                    case "SUBSCRIBE":
                        System.out.println("Processing SUBSCRIBE");
                        this.connectionPool.subscribeToTopic(target, this);
                        break;
                    case "UNSUBSCRIBE":
                        System.out.println("Processing UNSUBSCRIBE");
                        this.connectionPool.unsubscribeFromTopic(target, this);
                        break;
                    case "TOPIC":
                        System.out.println("Processing TOPIC");
                        this.connectionPool.createTopic(target);
                        break;
                    case "TOPICS":
                        System.out.println("Processing TOPICS");
                        List<String> topics = this.connectionPool.getTopics();
                        this.sendMessage(new Message("TOPICS", "", String.join(", ", topics), ""));
                        break;
                    case "MESSAGE":
                        System.out.println("Processing MESSAGE");
                        this.connectionPool.broadcast(message);
                        this.connectionPool.forwardToSubscribers(message);
                        break;
                    case "DEREGISTER":
                        System.out.println("Processing DEREGISTER");
                        this.connectionPool.broadcast(new Message("SYSTEM", "", this.username + " left the chat.", ""));
                        break;
                    default:
                        System.out.println("Unknown message type: " + messageType);
                        break;
                }

                this.createTopicsFromHashtags(messageBody);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client disconnected: " + this.username);
        } finally {
            this.connectionPool.removeConnection(this);
            this.close();
        }
    }

    private void close() {
        try {
            if (this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close the client socket.");
        }
    }

    public void sendMessage(Message message) {
        try {
            this.streamToClient.writeObject(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to client: " + this.username);
        }
    }

    private void createTopicsFromHashtags(String messageBody) {
        String[] words = messageBody.split("\\s+");
        for (String word : words) {
            if (word.startsWith("#")) {
                String hashtag = word.substring(1);
                System.out.println("Found hashtag: " + hashtag);
                this.connectionPool.createTopic(hashtag);
                this.connectionPool.subscribeToTopic(hashtag, this);
            }
        }
    }

    public String getUsername() {
        return this.username;
    }
}