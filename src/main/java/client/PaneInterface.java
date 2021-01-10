package client;

import libgofer.Message;

public interface PaneInterface {

    public String getUserName();

    public void setUserName(String name);

    public int getPort();

    public String getHostAddress();

    public void updateUserList(String s);

    public void removeUserList(String s);

    public void connect(String host, int port);

    public void display(Message m);

    public void display(String s);

    public void interpreteCommand(String text);

    public void disconnect();

}
