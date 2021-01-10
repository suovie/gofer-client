package client;

import java.io.*;
import java.net.*;

public class Upload implements Runnable {

    public String addr;
    public int port;
    public Socket socket;
    public FileInputStream input;
    public OutputStream output;
    public File file;
    public PaneInterface pane;

    public Upload(String addr, int port, File file, PaneInterface gface) {
        super();
        try {
            this.file = file;
            pane = gface;
            socket = new Socket(InetAddress.getByName(addr), port);
            output = socket.getOutputStream();
            input = new FileInputStream(file);
        } catch (Exception ex) {
            pane.display("Exception on Upload " + ex.getMessage());
        }
    }

    public void run() {
        try {
            byte[] buffer = new byte[1024];
            int count;

            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            output.flush();

            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ex) {
            pane.display("Exception [Upload : run()] " + ex.getMessage());
        }
    }
}
