package client;
import java.net.*;

public class Quiet {

	public Quiet() {}
	
	public void PrintReceiverQ (DatagramPacket receivePacket) {
		System.out.println("Client: Packet received:");
//		System.out.println("From host: " + receivePacket.getAddress());
//		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
//		System.out.println("Length: " + len);
		System.out.print("Containing: ");

		// Form a String from the byte array.
		String received = new String(receivePacket.getData(),0,len);   
		System.out.println(received);
	}
	
	public void PrintSenderQ (DatagramPacket sendPacket) {
		System.out.println("Client: Sending packet:");
//		System.out.println("To host: " + sendPacket.getAddress());
//		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
//		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"
	}
}
