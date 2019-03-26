import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class LBServer {

	private Set<String> hosts;
	private ServerSocket lbServer;
	//Handle to control clients
	class LBHandle implements Runnable {
		Thread thread;
		private Socket client;

		public LBHandle(Socket client) {
			this.client = client;
			thread = new Thread(this);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				//Receive commands
				BufferedReader reader;
				reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter writer = new PrintWriter(client.getOutputStream());
				String request = reader.readLine();
				String response = "";
				while (!request.equals("BYE")) {	
					// Connect to all server
					int minClient = 1000;
					String minHost = "";

					for (String hostInfo : hosts) {
						int numberOfClient = getClients(hostInfo);
						if (numberOfClient == -1)
							continue;
						if (minClient > numberOfClient) {
							minClient = numberOfClient;
							minHost = hostInfo;
						}
					}
					// Get the server with minHost
					response = forwardResponet(minHost, request);
					writer.println(response);
					System.out.println(minHost+" reply: "+response);
					writer.flush();
					
					request = reader.readLine();
				}
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void start() {
			thread.start();
		}

	}

	public LBServer(int port, Set<String> hosts) {	//Create LBServer
		this.hosts = hosts;
		// servers.add("localhost:9091");
		// TODO Auto-generated constructor stub

		try {
			lbServer = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public int getClients(String hostInfo) {	//Return numbers of clients connecting to host (Server)
		try {
			
			Scanner scanner = new Scanner(System.in);

			int pos = hostInfo.indexOf(':');
			String host = hostInfo.substring(0, pos);
			int port = Integer.parseInt(hostInfo.substring(pos + 1));
			
			
			Socket askClient;
			askClient = new Socket(host, port);
			PrintWriter writer = new PrintWriter(askClient.getOutputStream());
			writer.println("CLIENTS");
			writer.flush();
			BufferedReader reader = new BufferedReader(new InputStreamReader(askClient.getInputStream()));
			String message = reader.readLine();
			int numberOfClient = Integer.parseInt(message);
			System.out.println(message);

			writer.println("BYE");
			writer.flush();
			scanner.close();
			askClient.close();
			return numberOfClient;
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			return -1;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return -1;
		}
	}

	public String forwardResponet(String hostInfo, String message) {	//Forward commands from client to server
		try {
			Scanner scanner = new Scanner(System.in);

			int pos = hostInfo.indexOf(':');
			String host = hostInfo.substring(0, pos);
			int port = Integer.parseInt(hostInfo.substring(pos + 1));

			Socket askClient;
			askClient = new Socket(host, port);
			PrintWriter writer = new PrintWriter(askClient.getOutputStream());
			writer.println(message);
			writer.flush();
			BufferedReader reader = new BufferedReader(new InputStreamReader(askClient.getInputStream()));
			String response = reader.readLine();

			writer.println("BYE");
			writer.flush();
			scanner.close();
			askClient.close();
			return response;
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			return "ERROR";
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "ERROR";
		}
	}

	public void start() {
		try {
			while (true) {
				Socket client = lbServer.accept();
				LBHandle handle = new LBHandle(client);
				handle.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Set<String> askHosts = new TreeSet<String>();
		if (args.length < 1) {
			System.err.println("Invalid arguments!");
			System.exit(1);
		}
		int port = 9001;
		try {
			port = Integer.parseInt(args[0]);
			for (int i = 1; i < args.length; i++) {
				askHosts.add(args[i]);
			}
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		LBServer lb = new LBServer(port, askHosts);
		lb.start();
	}

}
