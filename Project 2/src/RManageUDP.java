import java.nio.ByteBuffer;

public class RManageUDP {
	private int mode = 0;
	private long modeParameter = 1;
	private String filename;
	private int localPort;
	
	final int headerLength = 10;
	final int messageLength = 20;
	
	public long getSeqNum(byte[] frame){ 
		ByteBuffer b = ByteBuffer.allocate(8);
		b.put(frame, 0, 8);
		long a = b.getLong(0);
		return a;
	}
	
	public void putSeqNumInFrame(byte[] frame, long seqNum) {
		ByteBuffer b = ByteBuffer.allocate(8);
		b.putLong(seqNum);
		byte[] arr = b.array();
		for(int i = 0; i < arr.length; i++){
			frame[i] = arr[i];
		}
	}
	
	public void printFrame(byte[] frame){
		for (int j = 0; j < headerLength; j++)
			System.out.print(frame[j] + " ");
		for (int j = headerLength; j < headerLength + messageLength; j++)
			System.out.print((char)frame[j]);
		System.out.println();
	}
	
	public void printFrames(byte[][] frames){
		for(int i = 0; i < frames.length; i++){
			printFrame(frames[i]);
		}
	}
	
	public int getMode() {
		return mode;
	}
	
	public boolean setMode(int mode) {
		this.mode = mode;
		if(this.mode == mode){
			if(this.mode == 1){
				this.setModeParameter(256);
			}else if(this.mode == 0){
				this.setModeParameter(1);
			}
			return true;
		}else
			return false;
	}
	
	public long getModeParameter() {
		return modeParameter;
	}
	
	public boolean setModeParameter(long n) {
		if(getMode() == 0)
			return false;
		else{
			this.modeParameter = n;
			return true;
		}
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String fname) {
		this.filename = fname;
	}

	public int getLocalPort() {
		return localPort;
	}

	public boolean setLocalPort(int port) {
		this.localPort = port;
		if(this.localPort == port)
			return true;
		else
			return false;
	}
	
	public int getHeaderLength() {
		return headerLength;
	}

	public int getMessageLength() {
		return messageLength;
	}

}
