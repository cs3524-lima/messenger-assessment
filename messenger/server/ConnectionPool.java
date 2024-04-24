package server;

import shared.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionPool {
    private List<ChatServerHandler> connections;
    private Map<String, List<ChatServerHandler>> groups;
    private Map<String, List<ChatServerHandler>> topics;

    public ConnectionPool() {
        this.connections = new ArrayList<>();
        this.groups = new HashMap<>();
        this.topics = new HashMap<>();
    }

    public synchronized void addConnection(ChatServerHandler handler) {
        this.connections.add(handler);
    }

    public synchronized void removeConnection(ChatServerHandler handler) {
        this.connections.remove(handler);
        removeFromAllGroups(handler);
        removeFromAllTopics(handler);
    }

    private synchronized void removeFromAllGroups(ChatServerHandler handler) {
        for (List<ChatServerHandler> group : this.groups.values()) {
            group.remove(handler);
        }
    }

    private synchronized void removeFromAllTopics(ChatServerHandler handler) {
        for (List<ChatServerHandler> topic : this.topics.values()) {
            topic.remove(handler);
        }
    }

    public synchronized void createGroup(String groupName) {
        if (!this.groups.containsKey(groupName)) {
            this.groups.put(groupName, new ArrayList<>());
            System.out.println("Group created: " + groupName);
        }
    }

    public synchronized void joinGroup(String groupName, ChatServerHandler handler) {
        if (this.groups.containsKey(groupName)) {
            this.groups.get(groupName).add(handler);
            System.out.println(handler.getUsername() + " joined group: " + groupName);
        }
    }

    public synchronized void leaveGroup(String groupName, ChatServerHandler handler) {
        if (this.groups.containsKey(groupName)) {
            this.groups.get(groupName).remove(handler);
            System.out.println(handler.getUsername() + " left group: " + groupName);
        }
    }

    public synchronized void removeGroup(String groupName) {
        if (this.groups.containsKey(groupName)) {
            this.groups.remove(groupName);
            System.out.println("Group removed: " + groupName);
        }
    }

    public synchronized boolean isGroupName(String target) {
        return this.groups.containsKey(target);
    }

    public synchronized void broadcastToGroup(Message message, String groupName) {
        if (this.groups.containsKey(groupName)) {
            for (ChatServerHandler handler : this.groups.get(groupName)) {
                handler.sendMessage(message);
            }
        }
    }

    public synchronized void subscribeToTopic(String topic, ChatServerHandler handler) {
        if (!this.topics.containsKey(topic)) {
            this.topics.put(topic, new ArrayList<>());
        }
        this.topics.get(topic).add(handler);
        System.out.println(handler.getUsername() + " subscribed to topic: " + topic);
    }

    public synchronized void unsubscribeFromTopic(String topic, ChatServerHandler handler) {
        if (this.topics.containsKey(topic)) {
            this.topics.get(topic).remove(handler);
            System.out.println(handler.getUsername() + " unsubscribed from topic: " + topic);
        }
    }

    public synchronized void createTopic(String topic) {
        if (!this.topics.containsKey(topic)) {
            this.topics.put(topic, new ArrayList<>());
            System.out.println("Topic created: " + topic);
        }
    }

    public synchronized List<String> getTopics() {
        return new ArrayList<>(this.topics.keySet());
    }

    public synchronized void forwardToSubscribers(Message message) {
        String messageBody = message.getMessageBody();
        for (Map.Entry<String, List<ChatServerHandler>> entry : this.topics.entrySet()) {
            String topic = entry.getKey();
            if (messageBody.contains(topic)) {
                for (ChatServerHandler handler : entry.getValue()) {
                    handler.sendMessage(new Message("TOPIC", topic, "Forwarded from group: " + message.getMessageBody(), message.getUser(), topic));
                }
            }
        }
    }

    public synchronized void sendToClient(Message message, String username) {
        for (ChatServerHandler handler : this.connections) {
            if (handler.getUsername().equals(username)) {
                handler.sendMessage(message);
                break;
            }
        }
    }

    public synchronized void broadcast(Message message) {
        for (ChatServerHandler handler : this.connections) {
            handler.sendMessage(message);
        }
    }
}