import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP extends RManageUDP implements edu.utulsa.unet.RSendUDPI {

	private InetSocketAddress receiver = new InetSocketAddress("localhost", 12987);
	private long timeout = 1000;
	private long LFS, LAR;
	private byte[][] frameBuffer;
	private ArrayList<SentFrame> sentFrames;
	private UDPSocket socket;
	private int fileSize;


	@Override
	public boolean sendFile() {
		LFS = -1;
		LAR = -1;
		sentFrames = new ArrayList<SentFrame>();
		long startTime = System.currentTimeMillis();
		
		try {
			socket = new UDPSocket(this.getLocalPort());
			this.setMTU(socket.getSendBufferSize());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		System.out.println("sending from " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
		
		if(this.getMode() == 0)
			System.out.println("using stop-and-wait");
		else
			System.out.println("using sliding window");
		
		frameBuffer = prepareFrames();
		//printFrames(frameBuffer);
		
		SenderThread senderThread = new SenderThread(this);
		//GetAckThread getAckThread = new GetAckThread(this);
		
		senderThread.start();
		//System.out.println("Starting second thread");
		//getAckThread.start();
		
		boolean gottenAnyFrames = false;
		while(this.getLAR() < (this.getFrameBuffer().length - 1)){
			byte[] frame = new byte[this.getHeaderLength() + this.getMessageLength()];
			DatagramPacket packet = new DatagramPacket(frame, frame.length);
			try {
				this.getSocket().receive(packet);
				if(!gottenAnyFrames){
					gottenAnyFrames = true;
					System.out.println("sending to " 
							+ packet.getAddress().getHostAddress() + ":" + packet.getPort());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("message " + this.getSeqNum(frame) + " has been ACKed");
			//parent.printFrame(frame);
			if(this.getSeqNum(frame) > this.getLAR())
				this.setLAR(this.getSeqNum(frame));
			//System.out.println("LAR is now " + parent.getLAR());
			this.removeFromSentCumulative(this.getSeqNum(frame));
		}
		
		System.out.println("sent " + this.getFilename() + " (" + fileSize 
				+ " bytes) in " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds");
		
		return true;
	}

	public void addFrameToSent(byte[] frame){
		//System.out.println("adding frames");
		sentFrames.add(new SentFrame(frame, System.currentTimeMillis()));
		//System.out.println("Sent Frames . size: " + sentFrames.size());
		if (sentFrames.size() <= this.getWindowSize())
			return;
		else
			System.out.println("TOO MANY FRAMES, SWS ISN'T BIG ENOUGH");
	}

	public void removeFromSentCumulative(long seqNum) {
		//System.out.println(seqNum);
		for(int i = (sentFrames.size() - 1); i >= 0; i--){
			if(getSeqNum(sentFrames.get(i).getFrame()) <= seqNum){
				sentFrames.remove(i);
			}
		}
	}

	public void removeFromSent(byte[] frame) {
		for(int i = 0; i < sentFrames.size(); i++){
			if(getSeqNum(sentFrames.get(i).getFrame()) == getSeqNum(frame))
				sentFrames.remove(i);
		}
	}

	public byte[] getTimedOutFrame(){
		for(int i = 0; i < sentFrames.size(); i++){
			if(sentFrames.get(i) != null && sentFrames.get(i).isTimedOut(this.getTimeout()))
				return sentFrames.get(i).getFrame();
		}
		return null;
	}

	private byte[][] prepareFrames(){
		File infile = new File(getFilename());
		byte[][] frameBuffer = new byte[(int)(infile.length()/messageLength) + 2][];
		
		try {
			BufferedReader inputStream = new BufferedReader(new FileReader(infile));
			for(int i = 0; i < frameBuffer.length; i++) {
				frameBuffer[i] = new byte[headerLength + messageLength];
				putSeqNumInFrame(frameBuffer[i], i);
				for(int j = headerLength; j < messageLength + headerLength; j++){
					frameBuffer[i][j] = (byte)inputStream.read();
					if(frameBuffer[i][j] != -1)
						fileSize++;
				}
			}
			inputStream.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setFin(frameBuffer[frameBuffer.length - 1]);
		
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

	public static void main(String[] args){
		RSendUDP sender = new RSendUDP();
		//sender.setTimeout(1000);
		//sender.setMode(0);
		//sender.setModeParameter(100);
		sender.setFilename("veryimportant.txt");
		sender.setReceiver(new InetSocketAddress("129.244.178.192", 12987));
		sender.sendFile();
	}
}
