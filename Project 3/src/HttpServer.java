import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {
	public static void main(String[] args) {
		int portNumber = -1;
		ServerSocket server = null;
		
		if(args.length == 1)
			portNumber = Integer.parseInt(args[0]);
		else{
			System.out.println("Takes exactly one argument: portnumber\nExiting now");
			System.exit(-1);
		}
		try {
			server = new ServerSocket(portNumber);
			System.out.println("New thread listening on port " + portNumber);
			new HttpThread(server).run();
		} catch (IOException e) {
			System.out.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}
}