import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP extends RManageUDP implements edu.utulsa.unet.RReceiveUDPI {

	private long LAF, LFR;
	private UDPSocket socket;
	private ArrayList<byte[]> message;
	private InetAddress address;
	private int port;

	
	
	public boolean receiveFile() {
		System.out.println("Starting to receive!!!");
		byte[] frame = new byte[this.getHeaderLength() + this.getMessageLength()];
		DatagramPacket packet;
		message = new ArrayList<byte[]>();
		long seqNum;
		LFR = -1;
		LAF = LFR + getWindowSize();
		
		System.out.println(getWindowSize());
		try {
			socket = new UDPSocket(this.getLocalPort());
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while(LFR < LAF){
			packet = new DatagramPacket(frame, frame.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			port = packet.getPort();
			address = packet.getAddress();
			System.out.print("just got frame ");
			this.printFrame(frame);
			seqNum = getSeqNum(frame);
			
			System.out.println("LFR: " + LFR + "   LAF: " + LAF + "    SEQ: " + seqNum);
						
			if(LFR < seqNum && seqNum <= LAF){
				System.out.println("And now we are storing the frame");
				storeFrame(seqNum, frame);

				sendAckMaybe(seqNum);
			}else{
				System.out.println("not storing that frame");
				sendAckMaybe(seqNum);
			}
			
		}
		System.out.println("Receiver is done");
		
		try {
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(getFilename()));
			outputStream.write("jfdsakljfsdaklfdsal");
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socket.close();
		
		return false;
	}
	
	private int getLatestCumulativeFrame(){
		for(int i = 0; i < message.size(); i++){
			if(message.get(i) == null){
				System.out.println("found null in getLatestCumFrame");
				return (i - 1);
			}
		}
		return message.size() - 1;
	}
	
	private void storeFrame(long seqNum, byte[] frame) {
		while(seqNum >= message.size()){
			message.add(null);
		}
		
		message.set((int)seqNum, frame);
	}

	private void sendAckMaybe(long seqNum) {
		if(seqNum <= LFR + 1){
			//that means that the "next" frame has been received
			int latestCumFrame = getLatestCumulativeFrame();
			System.out.println("latest cumulative frame: " + latestCumFrame);
			sendAck(latestCumFrame);
			LFR = latestCumFrame;
			LAF = LFR + this.getWindowSize();
		}
	}

	private void sendAck(long seqNum) {
		System.out.print("sending ack ");
		byte[] frame = new byte[this.headerLength + this.messageLength];
		this.putSeqNumInFrame(frame, seqNum);
		printFrame(frame);
		try {
			socket.send(new DatagramPacket(frame, frame.length, address, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		RReceiveUDP receiver = new RReceiveUDP();
		receiver.setMode(1);
		receiver.setModeParameter(70);
		receiver.setFilename("receive file.txt");
		receiver.setLocalPort(32456);
		receiver.receiveFile();
	}


}
