package server;

import shared.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private ConnectionPool connectionPool;

    public ChatServer(int port) {
        this.port = port;
        this.serverSocket = null;
        this.connectionPool = new ConnectionPool();
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("Chat server started on port " + this.port);

            while (true) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ChatServerHandler handler = new ChatServerHandler(clientSocket, this.connectionPool);
                this.connectionPool.addConnection(handler);

                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Failed to start the server.");
        } finally {
            this.stop();
        }
    }

    private void stop() {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to stop the server.");
        }
    }

    public static void main(String[] args) {
        int port = 8888;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}