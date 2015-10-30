import java.io.IOException;
import java.net.DatagramPacket;

public class GetAckThread implements Runnable {
	
	private RSendUDP parent;
	
	public GetAckThread(RSendUDP arg){
		parent = arg;
	}
	
	public void run() {
		while(parent.getLAR() < parent.getFrameBuffer().length){
			byte[] frame = new byte[parent.getHeaderLength() + parent.getMessageLength()];
			DatagramPacket packet = new DatagramPacket(frame, frame.length);
			System.out.println("About to receive ack, hopefully this doesn't block");
			try {
				parent.getSocket().receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.print("just received ack ");
			parent.printFrame(frame);
			if(parent.getSeqNum(frame) > parent.getLAR())
				parent.setLAR(parent.getSeqNum(frame));
			parent.removeFromSent(frame);
		}
	}
}
