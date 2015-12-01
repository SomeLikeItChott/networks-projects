import java.nio.ByteBuffer;

public class RManageUDP {
	private int mode = 0;
	private long modeParameter = 256;
	private String filename;
	private int localPort = 12987;
	
	final int headerLength = 10;
	int messageLength;
		
	protected void setMTU(int MTU){
		if(MTU > headerLength)
			messageLength = MTU - headerLength;
		else
			System.out.println("MTU must be at least " + headerLength + " bytes long.");
	}
	
	public int getWindowSize(){
		if(getMode() == 0)
			return 1;
		else
			return (int) (getModeParameter()/(headerLength + messageLength));
	}
	
	public void setFin(byte[] frame){
		frame[9] = (byte)'f';
	}
	
	public boolean isFin(byte[] frame){
		if(frame[9] == (byte)'f')
			return true;
		else
			return false;
	}

	public void setAck(byte[] frame){
		frame[8] = (byte)'a';
	}
	
	public boolean isAck(byte[] frame){
		if(frame[8] == (byte)'a')
			return true;
		else
			return false;
	}
	
	public byte[] getMessage(byte[] frame){
		byte[] mess = new byte[messageLength];
		for(int i = 0; i < messageLength; i++){
			mess[i] = frame[i + headerLength];
		}
		return mess;
	}
	
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
