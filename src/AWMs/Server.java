package AWMs;

import java.net.*;
import java.io.*;

public class Server extends Thread {
	private ServerSocket serverSocket;
	private int portNum; // Connected Port
	private int queue; // Number of current connections in the queue
	private String hostName; // Name of host computer
	private ServerConnect[] connections;
	private Thread[] threads;
	private ConnectFlag flag;
	public Server() throws IOException {
		// Create server socket and set timeout limit
		serverSocket = new ServerSocket(0);
		serverSocket.setSoTimeout(100000000);

		// Get the port number it has connected to 
		portNum = serverSocket.getLocalPort();
		
		connections = new ServerConnect[2];
		threads = new Thread[2];
		flag = new ConnectFlag();

		// Get the name the server is connected to
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Hostname could not be resolved");
			System.exit(1);
		}
		// Write port number to config file
		PrintWriter writer = new PrintWriter("config.txt", "UTF-8");
		writer.println(portNum);
		writer.println(hostName);
		writer.close();
	}

	public void startServer() {
		while(true) {
			try {
				// Wait for connection and accept said connection 
				System.out.println("Waiting for client on port " + portNum + "...");
				Socket server = serverSocket.accept();

				// Connection accepted. Create new Connect thread to handle connection. 
				queue += 1; // Increase queue counter by 1;
				connections[queue - 1] = new ServerConnect(server, queue, hostName, flag);
				threads[queue - 1] = new Thread(connections[queue - 1], "Client " + Integer.toString(queue));
				threads[queue - 1].start();
				System.out.println("Connection established. There are " + Integer.toString(queue) + " connections.");

				if (queue == 2) {
					connections[0].setStart();
					connections[1].setStart(); 
				}

			} catch (SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Server host = new Server();
		host.startServer();
	}
}