
public class Send {
	private static String mode;
	private static String fileName;
	private static String request ;
	private static byte [] read;// = new byte [512+3];
	private static byte [] write;// = new byte [512+3];
	private static byte [] ack = new byte [4];
	private static byte [] error = new byte [5];
	private static byte [] data = new byte [512+4];


	
	public byte [] getRead () {
		return this.read;

	}
	
	public byte [] getWrite () {
		return this.write;

	}
	
	public byte [] getAck () {
		return this.ack;

	}
	
	public byte [] getData () {
		return this.data;

	}

	
	/*public byte [] getErr () {
		return this.error;

	}*/
	


	public static void RRQ(UI PKG){
		fileName = PKG.getFileName();
		byte[] length = fileName.getBytes();
		read = new byte [3+length.length];
		read[0] = 0;
		read[1] = 1;
		
		for (int i = 0; i < length.length; i++){
			read[2+i] = length[i];
			
		}
		
		read[3+length.length] = 0;
		

	}

	public static void WRQ(UI PKG){
		fileName = PKG.getFileName();
		byte[] length = fileName.getBytes();
		read = new byte [3+length.length];
		read[0] = 0;
		read[1] = 2;
		
		for (int i = 0; i < length.length; i++){
			read[2+i] = length[i];
			
		}
		
		read[3+length.length] = 0;

	}

	public static void Data(UI PKG){
		fileName = PKG.getFileName();


	}

	public static void Ack(UI PKG){
		int i = 0;
		ack[0] = 0;
		ack[1] = 4;
		ack[2] = (byte) i++;

	}

	public static void ERR (UI PKG){
		fileName = PKG.getFileName();


	}
	
	
	
	
	
	

	public static void main(String[] args) {
		UI PKG = new UI ();
		request = PKG.getRequest();
/*
		switch (request){
		case"RRQ":
			RRQ(PKG);
			break;
			
		case"WRQ":
			WRQ(PKG);
			break;
		
		case"ERR":
			ERR(PKG);
			break;		
		}
*/

	}

}
