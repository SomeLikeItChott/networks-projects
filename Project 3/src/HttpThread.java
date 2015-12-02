import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpThread extends Thread {
	private ServerSocket server;
	private int portNumber;
	
	
	public HttpThread(ServerSocket server) {
		this.server = server;
		portNumber = server.getLocalPort();
	}


	public void run(){
		BufferedReader in;
		Socket client = null;
		
		try {
			client = server.accept();
			System.out.println("Server accepted connection from " + client.getInetAddress());
		} catch (IOException e) {
			System.out.println("Accept failed on port " + portNumber);
			System.exit(-1);
		}
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			while(in.ready())
				System.out.println(in.readLine());
		} catch (IOException e) {
			System.out.println("Couldn't get an inputStream from the client");
			System.exit(-1);
		}
	}
}
