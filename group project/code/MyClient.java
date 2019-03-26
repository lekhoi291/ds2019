import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MyClient {
	private String host; //Eg:localhost
	private int port; //Eg:9090
	private Socket client;
	
	
	public MyClient(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			client = new Socket(host, port);
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void start() {
		try {
			//Get commands
			InputStream in = client.getInputStream();
			BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));
			//Send commands
			OutputStream out = client.getOutputStream();
			PrintWriter writer = new PrintWriter(out);
			//Insert commands from keyboard
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter command: ");
			String command = scanner.nextLine();
			while (!command.equals("EXIT")) {	
				writer.println(command);
				writer.flush();
				System.out.println("Client say: " + command);
				String response = reader.readLine();
				System.out.println(response);
				System.out.println("Server say: " + response);
				System.out.println("Enter command: ");
				command = scanner.nextLine();
			}
			writer.println("BYE");
			writer.flush();
			client.close();

		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}
	
	public static void main(String[] args) {
		if(args.length!=2) {	//2args: localhost 9091
			System.err.println("Invalid Arguments!");
			System.exit(1);
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		
		MyClient client = new MyClient(host, port);
		client.start();
	}
}
