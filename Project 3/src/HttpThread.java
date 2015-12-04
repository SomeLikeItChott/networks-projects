import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class HttpThread extends Thread {
	private Socket client;
	private final String SERVERTEXT = "ChottServer/1.0";

	public HttpThread(Socket client) {
		this.client = client;
		client.getLocalPort();
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
		if (message.contains("\nHost: "))
			return true;
		else
			respondError(400, "Bad request (No Host field)");
		return false;

	}

	private boolean verifyMethod(String method) {
		method = method.trim();
		if (method.equals("GET") || method.equals("HEAD"))
			return true;
		else if (method.equals("OPTIONS") || method.equals("POST") || method.equals("PUT") || method.equals("DELETE")
				|| method.equals("TRACE") || method.equals("CONN")) {
			respondError(501, "Not implemented");
			return false;
		} else {
			respondError(400, "Bad request");
			return false;
		}

	}

	private void sendResponse(String method, String request) {
		byte[] resource = null;
		FileInputStream stream = null;
		File file = new File("public_html" + request);
		

		if (!file.exists())
			respondError(404, "Can't find resource");
		else {
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.out.println("Can't open up file");
				e.printStackTrace();
			}

			try {
				resource = new byte[(int) file.length()];
				stream.read(resource);
			} catch (IOException e) {
				System.out.println("Reading that file really didn't work it's a whole issue");
				e.printStackTrace();
			}
			
			//System.out.println(resource);
			respondOk(202, "OK", resource, getType(request), method);
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

	private void respondError(int status, String reason) {

		String header = "HTTP/1.1 " + status + " " + reason + "\r\n" + "Server: " + SERVERTEXT + "\r\n";

		replyWithString(header, new byte[0]);
	}

	private void respondOk(int status, String reason, byte[] resource, String type, String method) {
		String header = "HTTP/1.1 " + status + " " + reason + "\r\n" + "Content-Type: " + type + "\r\n"
				+ "Content-Length: " + resource.length + "\r\n" + "Server: " + SERVERTEXT + "\r\n";
		
		if(method.equals("GET"))
			replyWithString(header, resource);
		else
			replyWithString(header, new byte[0]);
	}
	
	private byte[] appendByteArrays(byte[] header, byte[] body){
		byte[] message = new byte[header.length + body.length];
		System.arraycopy(header, 0, message, 0, header.length);
		System.arraycopy(body, 0, message, header.length, body.length);
		
		return message;
	}

	private void replyWithString(String header, byte[] body) {
		//OutputStream stream = null;
		//PrintWriter writer = null;
		byte[] message;
		
		System.out.println("----BEGIN RESPONSE---- (leaving out body of response)");
		System.out.println(header);
		System.out.println("---- END RESPONSE ----");
		
		byte[] messageStart = (header + "\r\n").getBytes();
		message = appendByteArrays(messageStart, body);
		
		
		try {
			OutputStream out = client.getOutputStream();
			out.write(message);
			out.close();

		} catch (IOException e) {
			System.out.println("Couldn't write back to client");
			e.printStackTrace();
		}
		

	}
}
