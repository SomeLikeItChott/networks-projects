import java.io.*;
import java.net.*;

public class TalkClient {
	public static void main(String[] args) {
		// Create socket connection
		// System.out.println("Starting TalkClient");
		String serverName = "127.0.0.1";
		int portNumber = 16405;
		String message = null;
		try {
			Socket socket = new Socket(serverName, portNumber);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			while (true) {
				message = in.readLine();
				out.println(message);
			}
		} catch (UnknownHostException e) {
			System.out.println("Uknown Host:" + serverName);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("No I/O");
			System.exit(1);
		}
	}
}