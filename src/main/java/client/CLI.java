package client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import libgofer.Message;

public class CLI implements PaneInterface {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private Client client;
    private int port;
    private String serverAddr, username;
    private boolean running = false;

    public CLI() {}

    public String getUserName() {
        return username;
    }

    public void setUserName(String name) {
        this.username = name;
    }

    public int getPort() {
        return this.port;
    }

    public String getHostAddress() {
        return serverAddr;
    }

    public void connect(String addr, int port) {
        this.serverAddr = addr;
        this.port = port;

        try {
            client = new Client(this);
            client.send(new Message(Message.TYPE_CONNECT, "", "", Message.SERVER));
            running = true;
            listen();
        } catch (Exception ex) {
            display("connect exception: " + ex, ANSI_RED);
            disconnect();
        }
    }

    private void listen() {
        Scanner in = new Scanner(System.in);

        while (running) {
            ready();
            interpreteCommand(in.nextLine().trim());
        }

        in.close();
        running = false;
        display("Shutting down client...", ANSI_RED);
        System.exit(0);
    }

    public void disconnect() {
        running = false;
        listen();
    }

    private void login(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty()) {
            client.send(new Message(Message.TYPE_LOGIN, username, password, Message.SERVER));
        } else {
            display("login: username Or password missing", ANSI_RED);
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public void display(Message msg) {

        if (msg.type.equals(Message.TYPE_LOGIN)) {
            if (msg.content.equals("TRUE")) {
                display("Login Successful.", ANSI_BLUE);
            } else {
                display("Login Failed", ANSI_RED);
            }
        }

        else if (msg.type.equals(Message.TYPE_SIGNOUT)) {
            if (msg.content.equals(getUserName())) {
                display("Good bye.", ANSI_RED);
            } else {
                display("["+msg.sender+"]: " + msg.content + " has signed out.", ANSI_BLUE);
            }
        }

        // personal message to me
        else if (msg.recipient.equals(this.getUserName())) {
            display("[" + msg.sender + "]! " + msg.content, ANSI_GREEN);
        }

        // broadcast from server
        else if (msg.sender.equals(Message.SERVER)) {
            display("[" + msg.sender + "]: " + msg.content, ANSI_BLUE);
        }

        // broadcast from clients
        else {
            display("[" + msg.sender + "]: " + msg.content, ANSI_YELLOW);
        }

        ready();
    }

    public void display(String s) {
        System.out.println(ANSI_BLUE + "\r> [" + getCurrentTime() + "] " + s + ANSI_RESET);
        ready();
    }

    private void display(String s, String color) {
        System.out.println(color + "\r> [" + getCurrentTime() + "] " + s + ANSI_RESET);
    }

    private void ready() {
        System.out.print(ANSI_CYAN + "\r> [" + getCurrentTime() + "] [" + username + "] ");
    }

    public void updateUserList(String s) {}

    public void removeUserList(String s) {}

    public void interpreteCommand(String text) {
        text = text.trim();

        if (text.isEmpty()) {
            ready();
            return;
        }

        else if (text.startsWith("!help")) {
            String[] tokens = text.trim().split(" ");

            if (!tokens[0].equals("!help")) {
                display("'" + tokens[0] + "' not supported", ANSI_RED);
            } else {
                display("!help !login !dm !bc !send !whois !bye");
            }

            return;
        }

        else if (!client.login && !text.startsWith("!login")) {
            display("You must login first. Use !login [username] [password]");
            return;
        }

        if (text.startsWith("!login") && !client.login) {
            try {
                String[] tokens = text.trim().split(" ");
                if (!tokens[0].equals("!login")) {
                    display("\'" + tokens[0] + "\' not supported", ANSI_RED);
                    return;
                }

                if (tokens.length != 3) {
                    throw new ArrayIndexOutOfBoundsException("!login: Expecting [username] [password]");
                } else {
                    login(tokens[1], tokens[2]);
                }

                return;
            } catch (Exception out) {
                display(out.getMessage(), ANSI_RED);
                return;
            }
        }

        else if (text.startsWith("!dm")) {
            try {
                String[] tokens = text.trim().split(" ", 3);
                if (!tokens[0].equals("!dm")) {
                    display("\'" + tokens[0] + "\' not supported", ANSI_RED);
                    return;
                }

                if (tokens.length != 3) {
                    throw new ArrayIndexOutOfBoundsException("!dm: Expecting [user] [message]");
                } else {
                    client.send(new Message(Message.TYPE_MESSAGE, username, tokens[2].trim(), tokens[1].trim()));
                    return;
                }
            } catch (Exception out) {
                display(out.getMessage(), ANSI_RED);
                return;
            }
        }

        else if (text.startsWith("!bc")) {
            try {
                String[] tokens = text.trim().split(" ", 2);
                if (!tokens[0].equals("!bc")) {
                    display("\'" + tokens[0] + "\' not supported", ANSI_RED);
                    return;
                }

                if (tokens.length != 2) {
                    throw new ArrayIndexOutOfBoundsException("!bc: Expecting [message]");
                } else {
                    client.send(new Message(Message.TYPE_MESSAGE, username, tokens[1].trim(), Message.TO_ALL));
                    return;
                }
            } catch (Exception out) {
                display(out.getMessage(), ANSI_RED);
                return;
            }
        }

        else if (text.startsWith("!send")) {
            try {
                String[] tokens = text.trim().split(" ", 3);
                if (!tokens[0].equals("!send")) {
                    display("\'" + tokens[0] + "\' not supported", ANSI_RED);
                    return;
                }

                if (tokens.length != 3) {
                    throw new ArrayIndexOutOfBoundsException("!send: Expecting [user] [file]");
                } else {
                    sendFile(tokens[2].trim(), tokens[1].trim());
                    return;
                }
            } catch (Exception out) {
                display(out.getMessage(), ANSI_RED);
                return;
            }
        }

        else if (text.equals("!whois")) {
            client.send(new Message(Message.TYPE_WHOIS, username, "", Message.SERVER));
            return;
        }

        else if (text.equals("!bye")) {
            client.send(new Message(Message.TYPE_SIGNOUT, username, "", Message.SERVER));
            return;
        }

        else {
            display("Command not supported. Use !help", ANSI_RED);
        }
    }

    public void sendFile(String file_path, String recipient) {
        File file = new File(file_path.replace("'", "").replace("\"", ""));

        if (!file.exists()) {
            display("File does not exist "+ file_path);
            return;
        }

        if (file.length() < (120 * 1024 * 1024)) {
            client.file = file;

            try {
                // send upload request to server
                client.send(new Message(Message.TYPE_UPLOAD, this.username, file.getName(), recipient));
            } catch (Exception ex) {
                display("!send exception: " +ex.getMessage());
            }
        }

        else {
            display("!send: File should not be nore than 120MB");
        }
    }

}
