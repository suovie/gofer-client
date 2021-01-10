package client;

public class App {

    public static void main(String[] args) {
        PaneInterface pane = new CLI();

        if (args.length != 2 ) {
            pane.display("Usage: [server_addr] [port]");
            pane.disconnect();
        }

        pane.connect(args[0], Integer.parseInt(args[1]));
    }
}
