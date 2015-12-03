import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpThread extends Thread {
	private Socket client;
	private int portNumber;
	private final String SERVERTEXT = "ChottServer/1.0";

	public HttpThread(Socket client) {
		this.client = client;
		portNumber = client.getLocalPort();
	}

	public void run() {
		BufferedReader in = null;
		char character;
		String message = "";
		String method = "";
		String request = "";

		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			System.out.println("Couldn't get an inputStream from the client");
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			// get the different parts of the message
			character = (char) in.read();
			while (character != ' ') {
				message += character;
				method += character;
				character = (char) in.read();
			}
			System.out.println("method is: " + method);
			message += character;
			character = (char) in.read();
			while (character != ' ') {
				message += character;
				request += character;
				character = (char) in.read();
			}

			System.out.println("request is: " + request);
			while (in.ready()) {
				message += character;
				character = (char) in.read();
			}
			message += character;

			System.out.println("-----BEGIN REQUEST-----");
			System.out.println(message);
			System.out.println("----- END REQUEST -----");

			if (verifyMethod(method))
				if (verifyHostLine(message))
					sendResponse(method, request);

		} catch (IOException e) {
			System.out.println("There was an error when reading the request");
			e.printStackTrace();
			System.exit(-1);

		}
	}

	private boolean verifyHostLine(String message) {
		if (message.contains("\r\nHost: "))
			return true;
		else
			respond(400, "Bad request");
		return false;

	}

	private boolean verifyMethod(String method) {
		method = method.trim();
		if (method.equals("GET") || method.equals("HEAD"))
			return true;
		else if (method.equals("OPTIONS") || method.equals("POST") || method.equals("PUT") || method.equals("DELETE")
				|| method.equals("TRACE") || method.equals("CONN")) {
			respond(501, "Not implemented");
			return false;
		} else {
			respond(400, "Bad request");
			return false;
		}

	}

	private void sendResponse(String method, String request) {
		char[] resource = null;
		char character;
		BufferedReader stream = null;
		File file = new File("public_html" + request);
		

		if (!file.exists())
			respond(404, "Can't find resource");
		else {
			try {
				stream = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				System.out.println("Can't open up file");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				resource = new char[(int) file.length()];
				stream.read(resource);
			} catch (IOException e) {
				System.out.println("Reading that file really didn't work it's a whole issue");
				e.printStackTrace();
			}
			
			System.out.println(resource);

			respond(202, "OK", new String(resource), getType(request));
		}
	}

	private String getType(String request) {
		request.indexOf(".");
		String type = request.substring(request.lastIndexOf('.'), request.length());
		if (type.equals(".html") || type.equals(".htm"))
			return "text/html";
		else if (type.equals(".jpeg") || type.equals(".jpg"))
			return "image/jpeg";
		else if (type.equals(".gif"))
			return "image/gif";
		else if (type.equals(".pdf"))
			return "application/pdf";
		else
			return "unknown";
	}

	private void respond(int status, String reason) {
		String html = "<html><head><title>ERROR " + status + "</title></head><body>ERROR " + status + "<br>" + reason
				+ "</body></html>";

		String header = "HTTP/1.1 " + status + " " + reason + "\r\n" + "Content-Type: text/html\r\n"
				+ "Content-Length: " + html.length() + "\r\n" + "Server: " + SERVERTEXT + "\r\n";

		replyWithString(header, html);
	}

	private void respond(int status, String reason, String body, String type) {
		String header = "HTTP/1.1 " + status + " " + reason + "\r\n" + "Content-Type: " + type + "\r\n"
				+ "Content-Length: " + body.length() + "\r\n" + "Server: " + SERVERTEXT + "\r\n";

		replyWithString(header, body);
	}

	private void replyWithString(String header, String body) {
		OutputStream stream = null;
		PrintWriter writer = null;
		System.out.println("----BEGIN RESPONSE---- (leaving out body of response)");
		System.out.println(header);
		System.out.println("---- END RESPONSE ----");
		
		String message = header + "\r\n" + body;
		
		//System.out.println(message);
		
		try {
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			out.println(message);
			out.close();
			
			PrintWriter o = new PrintWriter(new File("public_html/thefile"));
			o.println(body);
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
