package client; /**
 * This thread is passed a socket that it reads from. Whenever it gets input
 * it writes it to the ChatScreen text area using the displayMessage() method.
 * Homework6 - Augustine Valdez and Braxden Hanzelka
 * 12/01/2020 Networks
 */

import java.io.*;

public class ReaderThread implements Runnable {
	BufferedReader fromServer;
	ChatScreen screen;

	public ReaderThread(BufferedReader fromServer, ChatScreen screen) {
		this.fromServer = fromServer;
		this.screen = screen;
	}

	public void run() {
		try {
			while (true) {
				String message = fromServer.readLine();
				message = message.substring(message.indexOf(" ")+1);

				// now display it on the display area
				screen.displayMessage(message);
			}
		}
		catch (IOException ioe) { System.out.println(ioe); }
	}
}
