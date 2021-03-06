import java.io.IOException;
import java.net.DatagramPacket;

public class SenderThread extends Thread {

	private RSendUDP parent;
	
	public SenderThread(RSendUDP arg){
		parent = arg;
	}
	
	@Override
	public void run() {
		int timesFinAcked = 0;
		//System.out.println("window size: " + parent.getWindowSize());
		while(parent.getLAR() < (parent.getFrameBuffer().length - 1)){
			if(parent.getLFS() < parent.getLAR() + parent.getWindowSize() && parent.getLFS() < (parent.getFrameBuffer().length - 1)){
				//System.out.println("LFS == " + parent.getLFS());
				parent.setLFS(parent.getLFS() + 1);
				//System.out.println("just change lfs to " + parent.getLFS());
				byte[] frame = parent.getFrameBuffer()[(int)parent.getLFS()];
				sendFrame(frame);
				parent.addFrameToSent(frame);
			}
			if(parent.getTimedOutFrame() != null){
				byte[] frame = parent.getTimedOutFrame();

				if(parent.isFin(frame)){
					System.out.println("acking fin");
					timesFinAcked++;
				}
				parent.removeFromSent(frame);
				parent.addFrameToSent(frame);
				System.out.println("resending message " + parent.getSeqNum(frame));
				sendFrame(frame);
				
			}
			if(timesFinAcked > 10){
				//System.out.println("time to give up");
				parent.setLAR(parent.getLAR() + 1);
				timesFinAcked = 0;
			}
		}
	}

	private void sendFrame(byte[] frame) {
		System.out.println("message " + parent.getSeqNum(frame) + " sent with " 
	+ (frame.length - parent.getHeaderLength()) + " bytes of actual data");
		//parent.printFrame(frame);
		try {
			parent.getSocket().send(new DatagramPacket(frame, frame.length, parent.getReceiver()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}