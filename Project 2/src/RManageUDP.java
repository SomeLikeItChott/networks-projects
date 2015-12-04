import java.nio.ByteBuffer;

public class RManageUDP {
	private int mode = 0;
	private long modeParameter = 256;
	private String filename;
	private int localPort = 12987;
	
	final int headerLength = 12;
	private int maxMessageLength;
	int MTU;
		
	protected void setMTU(int mt){
		if(mt > headerLength)
			this.MTU = mt;
		else
			System.out.println("MTU must be at least " + headerLength + " bytes long.");
	}
	
	public int getWindowSize(){
		if(getMode() == 0)
			return 1;
		else
			return (int) Math.max(1, (this.getModeParameter()/this.MTU));
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
		byte[] mess = new byte[frame.length - headerLength];
		for(int i = 0; i < mess.length; i++){
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
	
	public int getLength(byte[] frame){
		ByteBuffer b = ByteBuffer.allocate(2);
		b.put(frame, 10, 2);
		long a = b.getShort(0);
		return (int)a;
	}
	
	public void putLengthInFrame(byte[] frame, int length){
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort((short)length);
		byte[] arr = b.array();
		for(int i = 0; i < arr.length; i++){
			frame[10 + i] = arr[i];
		}
	}
	
	public void printFrame(byte[] frame){
		for (int j = 0; j < headerLength; j++)
			System.out.print(frame[j] + " ");
		for (int j = headerLength; j < frame.length; j++)
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

	public int getMaxMessageLength() {
		return (int)Math.min(MTU - headerLength, getModeParameter());
	}

}
