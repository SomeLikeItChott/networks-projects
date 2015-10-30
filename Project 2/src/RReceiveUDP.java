import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP extends RManageUDP implements edu.utulsa.unet.RReceiveUDPI {

	@Override
	public boolean receiveFile() {
		byte[] buffer = new byte[200];
		
		
		try {
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(getFilename()));
			outputStream.write("jfdsakljfsdaklfdsal");
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return false;
	}


}
