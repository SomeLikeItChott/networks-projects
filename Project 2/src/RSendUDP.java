import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP extends RManageUDP implements edu.utulsa.unet.RSendUDPI {

	private InetSocketAddress receiver;
	private long timeout;
	private long LFS, LAR;
	private byte[][] frameBuffer;
	private ArrayList<SentFrame> sentFrames;
	private UDPSocket socket;


	public void addFrameToSent(byte[] frame){
		if (sentFrames.size() < this.getModeParameter())
			sentFrames.add(new SentFrame(frame, System.currentTimeMillis()));
		else
			System.out.println("TOO MANY DANG FRAMES, SWS ISN'T BIG ENOUGH");
	}
	
	public byte[] getTimedOutFrame(){
		for(int i = 0; i < sentFrames.size(); i++){
			if(sentFrames.get(i).isTimedOut(this.getTimeout()))
				return sentFrames.get(i).getFrame();
		}
		return null;
	}

	@Override
	public boolean sendFile() {
		try {
			socket = new UDPSocket(this.getLocalPort());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		frameBuffer = prepareFrames();
		
		printFrames(frameBuffer);
		
		System.out.println(this.getSeqNum(frameBuffer[2]));
		
		//new SenderThread(this).run();
		
		
		return false;
	}
	
	private byte[][] prepareFrames(){
		File infile = new File(getFilename());
		byte[][] frameBuffer = new byte[(int)(infile.length()/messageLength) + 1][];
		
		try {
			BufferedReader inputStream = new BufferedReader(new FileReader(infile));
			for(int i = 0; i < frameBuffer.length; i++) {
				frameBuffer[i] = new byte[headerLength + messageLength];
				putSeqNumInFrame(frameBuffer[i], i);
				for(int j = headerLength; j < messageLength + headerLength; j++){
					frameBuffer[i][j] = (byte)inputStream.read();
				}
			}
			inputStream.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return frameBuffer;
	}

	public boolean setReceiver(InetSocketAddress arg0) {
		this.receiver = arg0;
		if (getReceiver() == arg0)
			return true;
		else
			return false;
	}

	public InetSocketAddress getReceiver() {
		return receiver;
	}

	public boolean setTimeout(long arg0) {
		this.timeout = arg0;
		if (getTimeout() == arg0)
			return true;
		else
			return false;
	}

	public long getTimeout() {
		return timeout;
	}
	
	public long getLFS() {
		return LFS;
	}

	public void setLFS(long lFS) {
		LFS = lFS;
	}

	public long getLAR() {
		return LAR;
	}

	public void setLAR(long lAR) {
		LAR = lAR;
	}

	public byte[][] getFrameBuffer() {
		return frameBuffer;
	}

	public void setFrameBuffer(byte[][] frameBuffer) {
		this.frameBuffer = frameBuffer;
	}

	public UDPSocket getSocket() {
		return socket;
	}

	public void removeFromSent(byte[] frame) {
		for(int i = 0; i < sentFrames.size(); i++){
			if(getSeqNum(sentFrames.get(i).getFrame()) == getSeqNum(frame))
				sentFrames.remove(i);
		}
	}


}
