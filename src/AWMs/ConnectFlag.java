package AWMs;

import java.net.*;
import java.io.*;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class ConnectFlag {
	boolean flag = false;
	int fromClient = 0;

	public synchronized boolean playerOne(DataInputStream in, DataOutputStream out, boolean move) {
		boolean quit;
		while (flag) {
			try {
				System.out.println("Connect 1 going to sleep");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Connect 1 wide awake " + move);
		
		if (move) {
			quit = getMove(in, 1);	
		} else {
			quit = receiveMove(out, 1);
		}
		
		flag = true;
		notify();
		
		System.out.println("Connect 2 notified");

		return quit;
	}

	public synchronized boolean playerTwo(DataInputStream in, DataOutputStream out, boolean move) {
		boolean quit;
		while (!flag) {
			try {
				System.out.println("Connect 2 going to sleep");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Connect 2 wide awake " + move);
		
		if (move) {
			quit = getMove(in, 2);	
		} else {
			quit = receiveMove(out, 2);
		}
		
		flag = false;
		notify();
		
		System.out.println("Connect 1 notified");

		return quit;
	}

	// Receive key events from client. Disconnect server when char 'q' is pressed
	public boolean getMove(DataInputStream in, int client) {
		try {
			fromClient = in.readInt();
			System.out.println("Player " + client + " typed: " + fromClient);
			if (fromClient == GlobalKeyEvent.VK_ESCAPE || 
				fromClient == GlobalKeyEvent.VK_CANCEL) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	// Send key events to client for execution
	public boolean receiveMove(DataOutputStream out, int client) {
		System.out.println("Player " + client + " receives: " + fromClient);
		return true;
		//out.writeInt(fromClient);
	}
}