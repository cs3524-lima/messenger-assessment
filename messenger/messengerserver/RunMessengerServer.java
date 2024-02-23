package messenger.messengerserver;

public class RunMessengerServer {
    public static void main(String[] args) {
        // java echoexample.echoserver.RunServer --no-attach --port=50000
        // String[] args = ["--no-attach", "--port=50000"];

        MessengerServer server = new MessengerServer(50000);
        server.run();
    }
}
