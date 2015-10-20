import java.io.*;
import java.net.*;

public class TalkServer {
	public static void main(String[] args) {
		System.out.println("Starting TalkServer");
		BufferedReader in = null;
		int portNumber = 16405;
		String message = null;
		Socket client = null;
		ServerSocket server = null;
		try {
			server = new ServerSocket(portNumber);
			System.out.println("Server listening on port " + portNumber);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
		try {
			client = server.accept();
			System.out.println("Server accepted connection from " + client.getInetAddress());
		} catch (IOException e) {
			System.out.println("Accept failed on port " + portNumber);
			System.exit(-1);
		}
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			System.out.println("Couldn't get an inputStream from the client");
			System.exit(-1);
		}
		try {
			while (true) {
				if (in.ready()) {
					message = in.readLine();
					System.out.println(message);
				}
			}
		} catch (IOException e) {
			System.out.println("Read failed");
			System.exit(-1);
		}
	}
}