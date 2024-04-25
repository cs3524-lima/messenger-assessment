package client;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private String host;
    private int port;
    private String username;
    private Socket socket;
    private Scanner scanner;
    private ObjectInputStream streamFromServer;
    private ObjectOutputStream streamToServer;
    private Thread listenerThread;
    private boolean exitFlag;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.username = null;
        this.socket = null;
        this.scanner = null;
        this.streamFromServer = null;
        this.streamToServer = null;
        this.listenerThread = null;
        this.exitFlag = false;
    }

    private void setup() {
        try {
            this.socket = new Socket(this.host, this.port);
            this.scanner = new Scanner(System.in);
            this.streamToServer = new ObjectOutputStream(this.socket.getOutputStream());
            this.streamFromServer = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Failed to connect to the server.");
            this.exitFlag = true;
        }
    }

    private void registerUser() {
        System.out.print("Enter your username: ");
        this.username = this.scanner.nextLine();
        this.sendMessage("REGISTER", "", this.username);
    }

    private String getUserInput() {
        System.out.print("> ");
        return this.scanner.nextLine();
    }

    private void sendMessage(String messageType, String target, String messageBody) {
        try {
            Message message = new Message(messageType, target, messageBody, this.username);
            this.streamToServer.writeObject(message);
        } catch (IOException e) {
            System.err.println("Failed to send the message.");
            this.exitFlag = true;
        }
    }

    private String getCommand(String userInput) {
        if (userInput.startsWith("CREATE ")) {
            return "CREATE";
        } else if (userInput.startsWith("JOIN ")) {
            return "JOIN";
        } else if (userInput.startsWith("LEAVE ")) {
            return "LEAVE";
        } else if (userInput.startsWith("REMOVE ")) {
            return "REMOVE";
        } else if (userInput.startsWith("SEND ")) {
            return "SEND";
        } else if (userInput.startsWith("SUBSCRIBE ")) {
            return "SUBSCRIBE";
        } else if (userInput.startsWith("UNSUBSCRIBE ")) {
            return "UNSUBSCRIBE";
        } else if (userInput.startsWith("TOPIC ")) {
            return "TOPIC";
        } else if (userInput.equalsIgnoreCase("TOPICS")) {
            return "TOPICS";
        } else if (userInput.equalsIgnoreCase("EXIT") || userInput.equalsIgnoreCase("QUIT")) {
            return "EXIT";
        } else if (userInput.startsWith("REGISTER ")) {
            return "REGISTER";
        }
        return "";
    }

    private void handleUserInput() {
        while (!this.exitFlag) {
            String userInput = this.getUserInput();
            String command = this.getCommand(userInput);

            switch (command) {
                case "CREATE":
                    String groupName = userInput.substring(7);
                    this.sendMessage("CREATE", groupName, "");
                    break;
                case "JOIN":
                    groupName = userInput.substring(5);
                    this.sendMessage("JOIN", groupName, "");
                    break;
                case "LEAVE":
                    groupName = userInput.substring(6);
                    this.sendMessage("LEAVE", groupName, "");
                    break;
                case "REMOVE":
                    groupName = userInput.substring(7);
                    this.sendMessage("REMOVE", groupName, "");
                    break;
                case "SEND":
                    String[] parts = userInput.split(" ", 3);
                    if (parts.length == 3) {
                        String target = parts[1];
                        String messageBody = parts[2];
                        this.sendMessage("SEND", target, messageBody);
                    } else {
                        System.out.println("Invalid SEND command format.");
                    }
                    break;
                case "SUBSCRIBE":
                    String topic = userInput.substring(10);
                    this.sendMessage("SUBSCRIBE", topic, "");
                    break;
                case "UNSUBSCRIBE":
                    topic = userInput.substring(12);
                    this.sendMessage("UNSUBSCRIBE", topic, "");
                    break;
                case "TOPIC":
                    topic = userInput.substring(6);
                    this.sendMessage("TOPIC", topic, "");
                    break;
                case "TOPICS":
                    this.sendMessage("TOPICS", "", "");
                    break;
                case "REGISTER":
                    String username = userInput.substring(9);
                    this.sendMessage("REGISTER", "", username);
                    break;
                case "EXIT":
                    this.exitFlag = true;
                    this.deregisterUser();
                    break;
                default:
                    if (!userInput.isEmpty()) {
                        this.sendMessage("MESSAGE", "", userInput);
                    }
                    break;
            }
        }
    }

    private void deregisterUser() {
        this.sendMessage("DEREGISTER", "", "");
    }

    private void startListenerThread() {
        this.listenerThread = new Thread(() -> {
            while (!this.exitFlag) {
                try {
                    Message message = (Message) this.streamFromServer.readObject();
                    String messageType = message.getMessageType();
                    String messageBody = message.getMessageBody();
                    String user = message.getUser();
                    String timestamp = message.getTimestamp();

                    switch (messageType) {
                        case "TOPIC":
                            System.out.println("[" + timestamp + "] " + "Forwarded from topic: " + messageBody);
                            break;
                        case "TOPICS":
                            System.out.println("Available topics: " + messageBody);
                            break;
                        default:
                            System.out.println("[" + timestamp + "] " + user + ": " + messageBody);
                            break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!this.exitFlag) {
                        System.err.println("Failed to receive the message.");
                    }
                }
            }
        });
        this.listenerThread.start();
    }

    public void start() {
        this.setup();
        if (!this.exitFlag) {
            this.registerUser();
            this.startListenerThread();
            this.handleUserInput();
        }
        this.close();
    }

    private void close() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.scanner != null) {
                this.scanner.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close the resources.");
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8888;
        ChatClient client = new ChatClient(host, port);
        client.start();
    }
}