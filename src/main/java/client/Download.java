package client;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import libgofer.Message;
import libgofer.Sound;

public class Download implements Runnable {

    public ServerSocket server;
    public Socket socket;
    public int port;
    public String saveTo, sender;
    public InputStream input;
    public FileOutputStream output;
    public PaneInterface pane;

    public Download(String saveTo, PaneInterface pane, String sender) {
        try {
            server = new ServerSocket(0);
            port = server.getLocalPort();
            this.saveTo = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-" + saveTo;
            this.pane = pane;
            this.sender = sender;
        } catch (IOException ex) {
            pane.display("Exception on Download " + ex.getMessage());
        }
    }

    public void run() {
        try {
            socket = server.accept();

            input = socket.getInputStream();
            output = new FileOutputStream(saveTo);

            byte[] buffer = new byte[1024];
            int count;

            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }

            output.flush();

            Sound.sound();
            pane.display(
                new Message("message", sender, "File \"" + this.saveTo + "\"" + " received from " + sender, pane.getUserName())
            );

            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ex) {
            pane.display("Exception on Download (run) " + ex.getMessage());
        }
    }
}
