package client;
import java.net.*;

import server.RequestParser;


public class Verbose {
	private RequestParser RP;

	public Verbose() {}

	public void PrintReceiverV (DatagramPacket receivePacket) {
		RP = new RequestParser();
		RP.parseRequest(receivePacket.getData(), receivePacket.getLength());
		System.out.println("Client: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");

		if (RP.getType() == 3 || RP.getType() == 4) {
			int blockNum = RP.getBlockNum();
			System.out.println("BlockNumber: " +blockNum);
		}

		// Form a String from the byte array.
		String received = new String(receivePacket.getData(),0,len);   
		System.out.println(received);
	}

	public void PrintSender(DatagramPacket sendPacket) {
		RP = new RequestParser();
		RP.parseRequest(sendPacket.getData(), sendPacket.getLength());
		System.out.println("Client: Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");

		if (RP.getType() == 3 || RP.getType() == 4) {
			int blockNum = RP.getBlockNum();
			System.out.println("BlockNumber: " +blockNum);
		}

		System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"
	}
}
