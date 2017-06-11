package AWMs;

import java.net.*;
import java.io.*;

public class ServerConnect implements Runnable {
	private Thread t;
	private int queue;
	private String hostName;
	private Socket connection;
	private boolean start;
	private boolean move;
	ConnectFlag flag;

	// Constructor accepts socket connection from main server, 
	// keeps track of number in the queue and host name, and creates a new thread to handle the connection 
	public ServerConnect(Socket connection, int queue, String hostName, ConnectFlag flag) {
		this.connection = connection;
		this.queue = queue;
		this.hostName = hostName;
		this.flag = flag;
		start = false;
	}

	public void setThreadName(String newName) {
		t.setName(newName);
	}

	public void setStart() {
		start = !start;
	}

	// run() starts the thread and handles the connection/communicates with the client	
	public void run() {
		try {
			System.out.println("Just connected to " + hostName + connection.getRemoteSocketAddress());

			// To prevent blocking
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.flush();

			// Create input stream and receive message from client
			DataInputStream in = new DataInputStream(connection.getInputStream());
			System.out.println("Client " + queue + " says " + in.readUTF());
				
			// Create output stream and reply to client
			out.writeUTF("Thank you for connecting to " + hostName + connection.getLocalSocketAddress() +
				". You are number " + queue + " in the queue.");
			
			out.writeUTF(Integer.toString(queue));
			out.flush();

			try {
				while (!start) {
					Thread.sleep(128);
				}
			} catch (InterruptedException e) {
					System.out.println("Awake!");
			}
			
			
			System.out.println(queue + " is awake!");
			out.writeUTF("Game has begun!");

			while (start) {
				if (queue == 1) {
					try {
						move = true;
						out.writeUTF(Boolean.toString(move));
					} catch (IOException e) {
						e.printStackTrace();
					}
					start = flag.playerOne(in, out, move);	
				} else {
					try {
						move = false;
						out.writeUTF(Boolean.toString(move));
					} catch (IOException e) {
						e.printStackTrace();
					}
					start = flag.playerTwo(in, out, move);
				}	
			}

			System.out.println(queue + " has disconnected. Goodbye!");
			
			in.close();
			out.close();
			connection.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
