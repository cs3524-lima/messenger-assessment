package server;

//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

import shared.Message;

public class ConnectionPool {
    private Map<String, ChatServerHandler> connections = new HashMap<>();

    // add ChatServerHandler into a map with username as a key
    public void addConnection(String username, ChatServerHandler handler) {
        connections.put(username, handler);
    }


    // broadcast messages
    // public void broadcast(Message message) {
    //     for (ChatServerHandler handler: this.connections){
    //         String clientName = handler.getClientName();
    //         if (clientName == null) { 
    //             // The client has not registered yet; skip
    //             continue;
    //         } else if (!clientName.equals(message.getUser())) {
    //             System.out.println("Relaying to " + handler.getClientName());
    //             handler.sendMessageToClient(message);
    //         }
    //     }
    // }

    public void broadcast(Message message) {
        for (Map.Entry<String, ChatServerHandler> entry : connections.entrySet()) {
            String clientName = entry.getKey();
            ChatServerHandler handler = entry.getValue();
            if (clientName.equals(message.getUser())) {
                continue;  // Skip the sender
            }
            System.out.println("Relaying to " + clientName);
            handler.sendMessageToClient(message);
        }
    }

    public void removeUser(String username) {
        if (connections.containsKey(username)) {
            connections.remove(username);
            System.out.println("User " + username + " removed from connection pool.");
        }
    }

    public ChatServerHandler getHandler(String username) {
        return connections.get(username);
    }
}
