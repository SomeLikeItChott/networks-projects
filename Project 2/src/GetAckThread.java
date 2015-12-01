import java.io.IOException;
import java.net.DatagramPacket;

public class GetAckThread extends Thread {
	
	private RSendUDP parent;
	
	public GetAckThread(RSendUDP arg){
		parent = arg;
	}
	
	public void run() {
		boolean gottenAnyFrames = false;
		while(parent.getLAR() < (parent.getFrameBuffer().length - 1)){
			byte[] frame = new byte[parent.getHeaderLength() + parent.getMessageLength()];
			DatagramPacket packet = new DatagramPacket(frame, frame.length);
			try {
				parent.getSocket().receive(packet);
				if(!gottenAnyFrames){
					gottenAnyFrames = true;
					System.out.println("sending to " 
							+ packet.getAddress().getHostAddress() + ":" + packet.getPort());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("message " + parent.getSeqNum(frame) + " has been ACKed");
			//parent.printFrame(frame);
			if(parent.getSeqNum(frame) > parent.getLAR())
				parent.setLAR(parent.getSeqNum(frame));
			//System.out.println("LAR is now " + parent.getLAR());
			parent.removeFromSentCumulative(parent.getSeqNum(frame));
		}
	}
}
