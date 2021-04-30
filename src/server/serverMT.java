package server;

import java.net.*;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Homework6 - Augustine Valdez and Braxden Hanzelka
 * 12/01/2020 Networks
 */


public class serverMT {

    public static final int DEFAULT_PORT = 63546;

    // construct a thread pool for concurrency
    private static final Executor exec = Executors.newCachedThreadPool();
    public static Map<String, Socket> users = new HashMap<String, Socket>();

    public static void main(String[] args) throws IOException {
        ServerSocket sock = null;

        try {
            // establish the socket
            sock = new ServerSocket(DEFAULT_PORT);
            System.out.println("Waiting for connections...");

            while (true) {
                /**
                 * now listen for connections
                 * and service the connection in a separate thread.
                 */
                Runnable task = new ConnectionChat(sock.accept(), users);
                System.out.print("We have a connection!\n");
                exec.execute(task);
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            if (sock != null)
                sock.close();
        }
    }
}

class ConnectionChat implements Runnable {
    private Socket client;
    public static Map<String, Socket> users = new HashMap<String, Socket>();
    private BufferedReader fromSocket = null;
    private String username, message;
    private int firstSpace = 0;

    public ConnectionChat(Socket client, Map<String,Socket> users) {
        this.client = client;
        this.users = users;
    }

    /**
     * This method runs in a separate thread.
     */
    public void run() {
        // Pass in logic
        try {
            fromSocket = new BufferedReader(new InputStreamReader(client.getInputStream()));
            username = fromSocket.readLine();
            username = username.substring(username.indexOf(" ")+1);

            if(users.containsKey(username)) {
                // send status 0 or status 1 instead - STATUS CODES
                client.getOutputStream().write(("STATUS 0\r\n").getBytes());
            } else {
                users.put(username,client);
                client.getOutputStream().write(("STATUS 1\r\n").getBytes());
                // broadcasting new user to all existing users
                theIterator("has logged in.", users.values());
            }

            while (true) {
                message = fromSocket.readLine();
                firstSpace = message.indexOf(" ");
                // continuously loops until "exit button" pushed and then breaks out
                if(message.equals("EXIT")){
                    users.remove(username);
                    theIterator("has left the chatroom",users.values());
                    break;
                // reads message and sees if there is an "@" for private messaging
                } else if (message.indexOf("@",firstSpace) == 4) {
                    privateMessage(message, users);
                // the default of simply broadcasting all messages to everyone
                } else {
                    message = message.substring(message.indexOf(" ") + 1);
                    theIterator(message, users.values());
                    client.getOutputStream().flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // our helper method to iterate sending a msg to all users.
    public void theIterator(String message, Collection<Socket> connections) throws IOException {
        Iterator<Socket> itr = connections.iterator();
        while (itr.hasNext()){
            client = itr.next();
            client.getOutputStream().write(("MSG "+username+": "+message + "\r\n").getBytes());
        }
    }

    // our helper method to send private messages from one client to another
    public void privateMessage(String message,  Map<String, Socket> users) throws  IOException {
        message = message.substring(message.indexOf(" ")+1);
        String arr[] = message.split(" ", 2);
        String sendTo = arr[0].substring(1);
        String pMessage = arr[1];
        
        if (users.containsKey(sendTo)) {
            client = users.get(sendTo);
            client.getOutputStream().write(("PRIVMSG Private Message from - " + username + ": " + pMessage + "\r\n").getBytes());
        }
    }
}
