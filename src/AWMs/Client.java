package AWMs;

import java.net.*;
import java.io.*;
import java.lang.Runtime.*;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class Client {
	private static boolean run = true;
	private static int queue = 0;
	private static boolean needShutDown = true;
	private int portNum;
	private boolean move;
	private String serverName;
	private Socket awmClient;
	private DataInputStream in;
	private DataOutputStream out;

	private class ClientFlag {
		boolean flag = true;
		public synchronized void keyboardGo () {
			while (flag) {
				try {
					System.out.println("Connect 1 going to sleep");
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Keyboard Go!");

			flag = true;
			notify();
		}

		public synchronized void clientGo() {
			while (!flag) {
				try {
					System.out.println("Connect 1 going to sleep");
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Client Go!");

			flag = false;
			notify();
		}


	}

	private class KeyRunner implements Runnable {
		GlobalKeyboardHook keyboardHook;
		AWMKeyAdapter awmAdapter;
		public KeyRunner(AWMKeyAdapter awmAdapter) {
			this.awmAdapter = awmAdapter;
		}

		public void run() {
			// Create global keyboard hook to listen to key commands
			keyboardHook = new GlobalKeyboardHook();
			System.out.println("Global keyboard hook running");
		}

		public void activateKeyListener() {
			keyboardHook.addKeyListener(awmAdapter);
		}

		public void deactivateKeyListener() {
			keyboardHook.removeKeyListener(awmAdapter);
		}

		public GlobalKeyboardHook getKeyboardHook() {
			return keyboardHook;
		}
	}

	// Object used to specify what keypresses and key releases do @Override GlobalKeyAdapter
	private class AWMKeyAdapter extends GlobalKeyAdapter {
		@Override public void keyPressed(GlobalKeyEvent event) {
			System.out.println(event);
			try {
				out.writeInt(event.getVirtualKeyCode());
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				System.out.println("Waiting");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			

			if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_ESCAPE ||
				event.getVirtualKeyCode() == GlobalKeyEvent.VK_CANCEL) {
				run = false;
			}
		}

		@Override public void keyReleased(GlobalKeyEvent event) {
			System.out.println(event);
			try {
				out.writeInt(event.getVirtualKeyCode());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				System.out.println("Waiting");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	// Object used to shutdown keyboardHook and close client if player closes client unexpectedly
	// Won't start if player connects, no game, and then disconnects
	private class ShutDownClient implements Runnable {
		GlobalKeyboardHook keyboardHook;
		Socket awmClient;
		public ShutDownClient (GlobalKeyboardHook keyboardHook, Socket awmClient) {
			this.keyboardHook = keyboardHook;
			this.awmClient = awmClient;
		}

		public void run() {
			if (needShutDown) {
				try {
					keyboardHook.shutdownHook();
					System.out.println("Keyboard Hook closed in emergency shutdown");
					in.close();
					out.close();
					awmClient.close();
					System.out.println("Client closed in emergency shutdown");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Emergency shutdown not needed, goodbye");
			}

		}
	}

	public Client () throws IOException {
		// Read port number from file
		BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
		String portString = reader.readLine();
		portNum = Integer.parseInt(portString);
		serverName = reader.readLine();
	}

	public void connectToServer() {
		// Connect to server
		try {
			System.out.println("Connecting to " + serverName + " on port " + portNum + "...");
			awmClient = new Socket(serverName, portNum);
			System.out.println("Connected to " + awmClient.getRemoteSocketAddress());

			// Create output stream and send message to server
			out = new DataOutputStream(awmClient.getOutputStream());
			out.writeUTF("Hello from " + serverName + awmClient.getLocalSocketAddress());
			out.flush();

			// Create input stream and read message from server
			in = new DataInputStream(awmClient.getInputStream());
			System.out.println("Server replies " + in.readUTF());
			queue = Integer.parseInt(in.readUTF()); // Get player number
			System.out.println("Player number: " + queue);
			System.out.println(in.readUTF());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void playWithServer() {
		// Create shutdown hook in case of unexpected disconnect
		AWMKeyAdapter awmAdapter = new AWMKeyAdapter();
		KeyRunner runKeyboard = new KeyRunner(awmAdapter);
		Thread keyboardThread = new Thread(runKeyboard, "Keyboard Thread");

		ShutDownClient emergency = new ShutDownClient(runKeyboard.getKeyboardHook(), awmClient);
		Thread emergencyThread = new Thread(emergency, "EmergencyThread");
		Runtime.getRuntime().addShutdownHook(emergencyThread);
		System.out.println("Shut down hook created.");
		keyboardThread.run();
		
		try {
			while (run) {
				keyboardThread.sleep(120);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Global keyboard hook shutdown");
			runKeyboard.getKeyboardHook().shutdownHook();
		}
	}

	public void shutDown() throws IOException {
		// Close socket
		in.close();
		out.close();
		awmClient.close();
		System.out.println("Client disconnected");
	}

	public static void main(String [] args) throws IOException {
		Client player = new Client();
		player.connectToServer();
		player.playWithServer();
		player.shutDown();
		needShutDown = false;
	}
}
