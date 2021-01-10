package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import libgofer.Message;
import libgofer.Sound;

public class Client implements Runnable {

    public String serverAddr;
    public int port;
    public Socket socket;
    public PaneInterface pane;
    public ObjectInputStream in;
    public ObjectOutputStream out;

    public File file;

    public boolean login = false;
    public boolean connected = false;
    private volatile boolean running = false;
    public Thread worker = null;

    public Client(PaneInterface pane) throws IOException {
        this.pane = pane;
        this.serverAddr = pane.getHostAddress();
        this.port = pane.getPort();

        socket = new Socket(InetAddress.getByName(serverAddr), port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();

        in = new ObjectInputStream(socket.getInputStream());
        running = true;

        if (worker == null) {
            worker = new Thread(this);
            worker.start();
        }
    }

    public boolean running() {
        return running;
    }

    public synchronized void run() {
        while (running()) {
            try {
                Message message = (Message) in.readObject();

                if (message.type.equals(Message.TYPE_MESSAGE)) {
                    Sound.sound();
                }

                this.interpreteMessage(message);
            } catch (Exception e) {
                pane.display("Client exception: " + e);
                this.stop();
            }
        }
    }

    public void send(Message m) {
        try {
            out.writeObject(m);
            out.flush();
        } catch (Exception e) {
            pane.display("Client: cannot send message - " + e);
        }
    }

    public void interpreteMessage(Message msg) throws Exception {
        // MESSAGE TYPE: message
        if (msg.type.equals(Message.TYPE_MESSAGE)) {
            pane.display(msg);
        }

        else if (msg.type.equals(Message.TYPE_LOGIN)) {
            if (msg.content.equals("TRUE")) {
                pane.setUserName(msg.recipient);
                this.login = true;
            } else {
                this.login = false;
            }

            pane.display(msg);
        }

        else if (msg.type.equals(Message.TYPE_CONNECT)) {
            pane.display(msg);
        }

        else if (msg.type.equals(Message.TYPE_NEW_USER)) {
            pane.updateUserList(msg.content);
        }

        else if (msg.type.equals(Message.TYPE_SIGNUP)) {
            pane.display(msg);
        }

        else if (msg.type.equals(Message.TYPE_SIGNOUT)) {
            pane.display(msg);

            if (msg.content.equals(pane.getUserName())) {
                this.stop();
            } else {
                pane.removeUserList(msg.content);
            }
        }

        // upload ready containing recipient's port number
        else if (msg.type.equals(Message.TYPE_UPLOAD)) {
            int port_no = Integer.parseInt(msg.content);

            Upload upload = new Upload(msg.sender, port_no, this.file, pane);
            Thread uploadThread = new Thread(upload);

            uploadThread.start();
            this.file = null;
        }

        // download request asking for port number
        else if(msg.type.equals(Message.TYPE_DOWNLOAD)){
            String filename = "" + msg.content;

            Download download = new Download(filename, pane, msg.sender);
            Thread downloadThread = new Thread(download);
            downloadThread.start();

            send(new Message(Message.TYPE_DOWNLOAD, pane.getUserName(), ""+download.port, msg.sender));
        }

        else if(msg.type.equals(Message.TYPE_SENDFAIL)) {
            pane.display(msg);
        }

        else {
            pane.display("Unknown message object: " + msg);
        }
    }

    public void stop() {
        running = false;
        pane.disconnect();
    }
}
