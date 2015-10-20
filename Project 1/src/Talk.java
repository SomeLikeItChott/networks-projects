import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Talk {
	public static void main(String[] args) {
		Talk talk = new Talk();
		String serverName;
		int portNumber;
		if (args.length > 0) {
			if (args[0].equals("-h")) {
				serverName = talk.getServerName(args);
				portNumber = talk.getPortNumber(args);
				talk.client(serverName, portNumber);
			} else if (args[0].equals("-s")) {
				portNumber = talk.getPortNumber(args);
				talk.serve(portNumber);
			} else if (args[0].equals("-a")) {
				serverName = talk.getServerName(args);
				portNumber = talk.getPortNumber(args);
				talk.auto(serverName, portNumber);
			} else if (args[0].equals("-help")) {
				talk.printHelp();
			} else {
				System.out.println("unexpected input\nrun \"Talk -help\" for help");
			}
		} else {
			System.out.println("Talk cannot be run without arguments");
		}
	}

	private int getPortNumber(String[] args) {
		// use args.length-1 as limit so that we don't overflow array
		// if "-p" is the last argument
		for (int i = 0; i < args.length - 1; i++)
			if (args[i].equals("-p")){
				try{
					return Integer.parseInt(args[i + 1]);
				}catch(NumberFormatException error){
					System.out.println(args[i + 1] + " is not a portnumber!");
					System.exit(-1);
				}
			}
		return 12987;
	}

	private String getServerName(String[] args) {
		// use args.length-1 as limit so that we don't overflow array
		// if "-h" or "-a" is the last argument
		for (int i = 0; i < args.length - 1; i++)
			if (args[i].equals("-h") || args[i].equals("-a")) {
				if (args[i + 1].startsWith("-"))
					continue;
				else
					return args[i + 1];
			}
		System.out.print("No address or hostname specified, inferring address: ");
		try {
			System.out.println(InetAddress.getLocalHost().getHostAddress());
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("Couldn't find address of this computer");
			System.exit(-1);
		}
		return null;
	}

	private void printHelp() {
		System.out.println("Talk.java written by Sam Chott\n"
				+ "Talk supports two-way chat communications over server sockets.\n\n"
				+ "Talk -h [hostname | IP address] [-p portnumber]\n"
				+ "will cause Talk to behave as a client and connect to the given hostname and portnumber.\n\n"
				+ "Talk -s [-p portnumber]\n"
				+ "will cause Talk to behave as a server and wait for connections on the given portnumber.\n\n"
				+ "Talk -a [hostname | IP address] [-p portnumber]\n"
				+ "will cause Talk to attempt to connect to existing servers at the given hostname and portnumber."
				+ "If there are no servers running, Talk will run as a server at the given portnumber.\n\n"
				+ "Talk –help\n" + "will display this information.\n\n"
				+ "In all cases, if no hostname or address is given, Talk will default to the current computer's address.\n"
				+ "If no portnumber is given, Talk will attempt to connect on port 12987.");
	}

	private void auto(String serverName, int portNumber) {
		try {
			Socket socket = new Socket(serverName, portNumber);
			client(socket);
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host:" + serverName);
			System.exit(1);
		} catch (IOException e) {
			// there must have been no server
			serve(portNumber);
		}
	}

	private void serve(int portNumber) {
		System.out.println("Starting server");
		BufferedReader remoteIn = null;
		Socket client = null;
		ServerSocket server = null;
		try {
			server = new ServerSocket(portNumber);
			System.out.println("Server listening on port " + portNumber);
		} catch (IOException e) {
			System.out.println("Server unable to listen on specified port");
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
			remoteIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			System.out.println("Couldn't get an inputStream from the client");
			System.exit(-1);
		}
		communicate(client, remoteIn);
	}

	private void client(String serverName, int portNumber) {
		try {
			Socket socket = new Socket(serverName, portNumber);
			client(socket);
		} catch (UnknownHostException e) {
			System.out.println("Uknown Host:" + serverName);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("No I/O");
			System.exit(1);
		}
	}

	private void client(Socket server) {
		// Create socket connection
		System.out.println("Starting client");
		BufferedReader remoteIn = null;
		try {
			remoteIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
		} catch (IOException e) {
			System.out.println("Client unable to communicate with server");
			System.exit(-1);
		}
		communicate(server, remoteIn);
	}

	private void communicate(Socket remote, BufferedReader remoteIn) {
		String message = null;
		try {
			BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter remoteOut = new PrintWriter(remote.getOutputStream(), true);
			while (true) {
				if (systemIn.ready()) {
					message = systemIn.readLine();
					if (message.equals("STATUS"))
						printStatus(remote);
					else
						remoteOut.println(message);
				}
				if (remoteIn.ready()) {
					message = remoteIn.readLine();
					System.out.println("[remote]" + message);
				}
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host:" + remote.getRemoteSocketAddress());
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Communication failed");
			System.exit(-1);
		}
	}

	private void printStatus(Socket remote) {
		System.out.println("your IP: " + remote.getLocalAddress() + "\nyour portnum: " + remote.getLocalPort());
		System.out.println("their IP: " + remote.getRemoteSocketAddress() + "\ntheir portnum: " + remote.getPort());

	}
}
