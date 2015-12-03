import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
	public static void main(String[] args) {
		Socket client = null;
		int portNumber = -1;
		ServerSocket server = null;

		if (args.length == 1)
			portNumber = Integer.parseInt(args[0]);
		else {
			System.out.println("Takes exactly one argument: portnumber\nExiting now");
			System.exit(-1);
		}
		while (true) {
			try {
				server = new ServerSocket(portNumber);
				System.out.println("Now listening on port " + portNumber);
			} catch (IOException e) {
				System.out.println("Could not listen on port " + portNumber);
				System.exit(-1);
			}
			try {
				client = server.accept();
				System.out.println("Server accepted connection from " + client.getInetAddress());
				new HttpThread(client).start();
				server.close();
			} catch (IOException e) {
				System.out.println("Server could not accept connection.");
				System.exit(-1);
			}
			
		}
	}
}