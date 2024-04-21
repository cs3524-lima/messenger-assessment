package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import shared.Message;

public class ChatServer {

    private int port;
    private ServerSocket serverSocket;
    private ConnectionPool connectionPool;
    private HashMap<String, List<String>> groups;

    public ChatServer(int port) {
        this.port = port;
        this.serverSocket = null;
        this.connectionPool = null;
        this.groups = new HashMap<>();
    }

    private void setup() throws IOException {
        System.out.println("ChatServer starting...");
        this.serverSocket = new ServerSocket(this.port);
        // make a connection pool for all the connecting clients.
        this.connectionPool = new ConnectionPool();
        System.out.println("Setup complete!");
        
    }
    
    public void createGroup(String groupName) {
        // Synchronize access to groups map to handle concurrent modifications
        synchronized (groups) {
            if (!groups.containsKey(groupName)) {
                groups.put(groupName, new ArrayList<>());
                System.out.println("Group created: " + groupName);
            }
        }
    }

    public void joinGroup(String groupName, String username) {
        synchronized (groups) {
            List<String> members = groups.get(groupName);
            if (members != null && !members.contains(username)) {
                members.add(username);
                System.out.println("User " + username + " joined group " + groupName);
            }
        }
    }

    public void leaveGroup(String groupName, String username) {
        synchronized (groups) {
            List<String> members = groups.get(groupName);
            if (members != null) {
                members.remove(username);
                System.out.println("User " + username + " left group " + groupName);
            }
        }
    }

    public void sendMessageToGroup(String groupName, String message, String senderUsername) {
        synchronized (groups) {
            List<String> members = groups.get(groupName);
            if (members != null) {
                for (String member : members) {
                    if (!member.equals(senderUsername)) { // Don't send the message to the sender
                        ChatServerHandler memberHandler = connectionPool.getHandler(member);
                        if (memberHandler != null) {
                            memberHandler.sendMessageToClient(new Message(message, senderUsername));
                        }
                    }
                }
            }
        }
    }

    private ChatServerHandler awaitClientConnection() {
        System.out.println("Waiting for new client connection...");
        try {
            Socket socket = this.serverSocket.accept();
            System.out.println("New client connected.");

            // create server_socket_handler and start it.
            ChatServerHandler handler = new ChatServerHandler(
                socket,
                this.connectionPool, 
                this
            );
            this.connectionPool.addConnection(null, handler);
            return handler;

        } catch (IOException e) {
            // e.printStackTrace();
            System.err.println("Could not establish connection with client.");
            return null;
        }
    }

    private void start() {
        while (true){
            ChatServerHandler handler = this.awaitClientConnection();
            if (handler != null) {
                // Start chat listener thread 
                Thread chatThread = new Thread(handler);
                chatThread.start();
            } else {
                // If a client failed connecting stop the server.
                // You could also do nothing here and just continue listening
                // for new connections.
                break;
            }
        }
    }

    public void run() {
        try {
            this.setup();
            this.start();  // Continuously accept new clients
        } catch (IOException e) {
            // If setup failed, stop here
            System.err.println("Setup failed; aborting...");
            return;
        }
        this.start();
        System.out.println("Server stopped.");
    
    }
}

