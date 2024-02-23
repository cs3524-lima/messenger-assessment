package messenger.messengerclient;

public class RunMessengerClient {
    public static void main(String[] args) {
        MessengerClient client = new MessengerClient("localhost", 50000);
        client.run();
    }
}
