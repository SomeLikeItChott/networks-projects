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
	boolean ackedFinalFrame;

	
	
	public boolean receiveFile() {
		try {
			socket = new UDPSocket(this.getLocalPort());
			this.setMTU(socket.getSendBufferSize());
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		long startTime = System.currentTimeMillis();
		System.out.print("local address is " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
		
		byte[] frame = new byte[this.getHeaderLength() + this.getMessageLength()];
		DatagramPacket packet;
		message = new ArrayList<byte[]>();
		long seqNum;
		LFR = -1;
		LAF = LFR + getWindowSize();
		ackedFinalFrame = false;
		//System.out.println("window size: " + getWindowSize());
		boolean gottenAnyFrames = false;

		
		while(!ackedFinalFrame){
			frame = new byte[this.getHeaderLength() + this.getMessageLength()];
			packet = new DatagramPacket(frame, frame.length);
			try {
				socket.receive(packet);
				if(!gottenAnyFrames){
					gottenAnyFrames = true;
					System.out.println(" receiving from " 
					+ packet.getAddress().getHostAddress() + ":" + packet.getPort());
					
					if(this.getMode() == 0)
						System.out.println("using stop-and-wait");
					else
						System.out.println("using sliding window");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			port = packet.getPort();
			address = packet.getAddress();
			//this.printFrame(frame);
			seqNum = getSeqNum(frame);
			System.out.println("message " + seqNum + " received with " + frame.length + " bytes of data");			
						
			if(LFR < seqNum && seqNum <= LAF){
				//System.out.println("And now we are storing the frame");
				storeFrame(seqNum, frame);

				sendAckMaybe(frame);
			}else{
				//System.out.println("not storing that frame");
				sendAckMaybe(frame);
			}
			
			//System.out.println("acked final frame: " + ackedFinalFrame);
		}		
		
		int fileSize = 0;
		try {
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(getFilename()));
			for(int i = 0; i < message.size(); i++){
				//printFrame(message.get(i));
				for(int j = headerLength; j < headerLength + messageLength; j++){
					if(message.get(i)[j] != -1){
						fileSize++;
						outputStream.write((char)message.get(i)[j]);
					}
				}
			}
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("received " + this.getFilename() + " (" + fileSize 
				+ " bytes) in " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds");

				
		return true;
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
		//System.out.println("We are stoirng " + seqNum);
		while(seqNum >= message.size()){
			message.add(null);
		}
		
		message.set((int)seqNum, frame);
		
	}
	
	private void printMessage(){
		System.out.println("---begin stored---");
		for(int i = 0; i < message.size(); i++)
			if(message.get(i) == null)
				System.out.println("blank");
			else
				printFrame(message.get(i));
		System.out.println("---end stored---");
	}

	private void sendAckMaybe(byte[] frame) {
		if(getSeqNum(frame) <= LFR + 1){
			//that means that the "next" frame has been received
			int latestCumFrame = getLatestCumulativeFrame();
			//System.out.println("latest cumulative frame: " + latestCumFrame);
			
			sendAck(latestCumFrame, frame);
			LFR = latestCumFrame;
			LAF = LFR + this.getWindowSize();
		}
	}

	private void sendAck(long seqNum, byte[] frame) {
		//System.out.print("we're looking at ");
		//printFrame(frame);
		System.out.println("sending ack for message " + seqNum);
		byte[] ackFrame = new byte[this.headerLength + this.messageLength];
		this.putSeqNumInFrame(ackFrame, seqNum);
		if(this.isFin(message.get(getLatestCumulativeFrame()))){
			//System.out.print("the last frame is ");
			//printFrame(message.get(getLatestCumulativeFrame()));
			setFin(ackFrame);
			ackedFinalFrame = true;
		}
		this.setAck(ackFrame);
		//printFrame(ackFrame);
		try {
			socket.send(new DatagramPacket(ackFrame, ackFrame.length, address, port));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args){
		RReceiveUDP receiver = new RReceiveUDP();
		//receiver.setMode(0);
		//receiver.setModeParameter(100);
		receiver.setFilename("receivefile.txt");
		receiver.receiveFile();
	}

}
