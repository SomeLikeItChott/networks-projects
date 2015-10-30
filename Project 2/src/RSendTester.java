import java.net.InetSocketAddress;

public class RSendTester {
	public static void main(String[] args){
		RSendUDP sender = new RSendUDP();
		
		sender.setFilename("veryimportant.txt");
		sender.setReceiver(new InetSocketAddress("localhost", 32456));
		sender.sendFile();
	}
}
