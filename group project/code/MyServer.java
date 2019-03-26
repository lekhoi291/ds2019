import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

public class MyServer {
	private int numberOfClient;
	private ServerSocket server;
	private int port;

	public MyServer(int port) {	//Create Sever
		// TODO Auto-generated constructor stub
		this.port = port;
		numberOfClient = 0;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	class Handle implements Runnable {	//Create handles for clients
		Thread thread;
		Socket client;

		public Handle(Socket client) {
			thread = new Thread(this);
			this.client = client;
		}
		
		//Save the key-value string into a file
		public void saveMap(TreeMap<String, String> map) {
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("map.dat"));
				out.writeObject(map);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//Load file
		public TreeMap<String, String> loadMap() {
			TreeMap<String, String> map = new TreeMap<String, String>();
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("map.dat"));
				map = (TreeMap<String, String>) in.readObject();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return map;
		}

		@Override
		public void run() {
			try {
				//Create a map to store key-value data
				TreeMap<String,String> map;
				//Read command from Client through LBServer
				BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter writer = new PrintWriter(client.getOutputStream());

				String input = reader.readLine();
				System.out.println("Client said: " + input);
				map = loadMap();
				while (!input.equals("BYE")) {
					String output = "";
					if (input.equals("CLIENTS")) {
						output = "" + numberOfClient;
						
					} else if (input.startsWith("SET")) {
						// SET my-index "my key"
						String message = input.substring(4);
						System.out.println(message);
						int pos = message.indexOf(' ');
						if (pos == -1) {
							output = "Invalid command!";
						} else {
							String key = message.substring(0, pos);
							String value = message.substring(pos + 2, message.length() - 1);
							System.out.println(key + ":" + value);

							// SET
							// ...
							if(map.containsKey(key)) {
								map.put(key, value);
								output = "Updated";
							}
							else {
								map.put(key, value);
								output = "Inserted!";
							}
							
						}
					} else if (input.startsWith("GET")) {
						String index = input.substring(4);
						// GET
						// ...
						output = map.get(index);

					} else if (input.startsWith("DEL")) {
						String key = input.substring(7);
						// DELETE
						// ...
						if(map.containsKey(key)) {
							map.remove(key);
							output = "Deleted!";
						}
						else {
							output = "No key";
						}
					}
					saveMap(map);
					writer.println(output);
					writer.flush();
					System.out.println("Server send: " + output);

					input = reader.readLine();
				}

				numberOfClient--;
				client.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		public void start() {
			thread.start();
		}

	}

	public void start() {
		try {
			while (true) {
				Socket client = server.accept();
				numberOfClient++;
				System.out.println("New client connected!");
				Handle handle = new Handle(client);
				handle.start();
			}
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Invalid Arguments!");
			System.exit(1);
		}
		int port = Integer.parseInt(args[0]);
		MyServer server = new MyServer(port);
		server.start();
	}
}
