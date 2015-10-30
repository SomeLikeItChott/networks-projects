
public class SentFrame {
	private byte[] frame;
	private long timeSent;
	
	public SentFrame(byte[] frame, long timeSent){
		this.frame = frame;
		this.timeSent = timeSent;
	}
	
	public boolean isTimedOut(long timeout){
		if(this.timeSent + timeout < System.currentTimeMillis())
			return true;
		else
			return false;
	}
	
	public byte[] getFrame() {
		return frame;
	}

	public void setFrame(byte[] frame) {
		this.frame = frame;
	}

	public long getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(long timeSent) {
		this.timeSent = timeSent;
	}
}
