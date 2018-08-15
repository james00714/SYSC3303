/*
 * Vebose mode for the client
*/
package client;
import java.net.*;

public class Verbose {
	private RequestParser RP;

	public Verbose() {}
	
	/*
	 * verbose mode for received packets
	*/
	public void PrintReceiverV (DatagramPacket receivePacket) {
		RP = new RequestParser();
		RP.parseRequest(receivePacket.getData(), receivePacket.getLength());
		System.out.println("Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		if (RP.getType() == 3) {
			System.out.println("Type: DATA");
			System.out.println("DATA Length: " + (len-4));

		}
		if (RP.getType() == 4) System.out.println("Type: ACK");
		System.out.println("Packet Length: " + len);
		//System.out.print("Containing: ");

		if (RP.getType() == 3 || RP.getType() == 4) {
			int blockNum = RP.getBlockNum();
			System.out.println("BlockNumber: " +blockNum);


		}

		// Form a String from the byte array.
	//	String received = new String(receivePacket.getData(),0,len);   
	//	System.out.println(received);
	}

	/*
	 * Verbose mode for sended packets
	*/
	public void PrintSender(DatagramPacket sendPacket) {
		RP = new RequestParser();
		RP.parseRequest(sendPacket.getData(), sendPacket.getLength());
		System.out.println("Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		if (RP.getType() == 3) {
			System.out.println("Type: DATA");
			System.out.println("DATA Length: " + (len-4));

		}
		if (RP.getType() == 4) System.out.println("Type: ACK");
		System.out.println("Length: " + len);
	//	System.out.print("Containing: ");

		if (RP.getType() == 3 || RP.getType() == 4) {
			int blockNum = RP.getBlockNum();
			System.out.println("BlockNumber: " +blockNum);
		}

	//	System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"
	}
}
