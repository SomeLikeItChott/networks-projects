import java.io.IOException;
import java.net.DatagramPacket;

public class SenderThread implements Runnable {

	private RSendUDP parent;
	
	public SenderThread(RSendUDP arg){
		parent = arg;
	}
	
	@Override
	public void run() {
		while(parent.getLAR() < parent.getFrameBuffer().length){
			if(parent.getLFS() < parent.getLAR() + parent.getModeParameter()){
				parent.setLFS(parent.getLFS() + 1);
				byte[] frame = parent.getFrameBuffer()[(int)parent.getLFS()];
				sendFrame(frame);
				parent.addFrameToSent(frame);
			}
			if(parent.getTimedOutFrame() != null){
				byte[] frame = parent.getTimedOutFrame();
				sendFrame(frame);
			}
		}
	}

	private void sendFrame(byte[] frame) {
		System.out.print("sending frame ");
		parent.printFrame(frame);
		try {
			parent.getSocket().send(new DatagramPacket(frame, frame.length, parent.getReceiver()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
