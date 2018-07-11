import java.io.*;
import java.net.*;
import java.util.*;

public class Client{
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket sendPacket, receivePacket;
	private static String mode = "";
	private static String fileName = "";
	private static String request = "";

	public Client()
	{
		try {			
			sendReceiveSocket = new DatagramSocket(30);
		} catch (SocketException se) {  
			se.printStackTrace();
			System.exit(1);
		}
	}

	
	
	public void normal (Send s, UI PKG)
	{
		
		System.out.println("Normal mode selected");

		
		byte msg[] = s.getRead();
		
		if (PKG.getRequest() == "WRQ"){
			msg = s.getWrite();
		}

		

		try {
			sendPacket = new DatagramPacket(msg, msg.length,
					InetAddress.getLocalHost(), 69);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"

	

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Packet sent.\n");

	

		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);

		try {
		
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		
		System.out.println("Client: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");

		
		String received = new String(data,0,len);   
		System.out.println(received);

		if (data[1] == 3) {
			msg = s.getAck();
			try {
				sendPacket = new DatagramPacket(msg, msg.length,
						InetAddress.getLocalHost(), 69);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		
		sendReceiveSocket.close();
	}

	public static void main(String[] args){
		Client c = new Client();
		
		UI PKG = new UI ();
		Send s = new Send ();
		
//		Quiet Q = new Quiet();
		Verbose V = new Verbose();
	//	Test T = new Test();
		
		PKG.IT1();

		mode = PKG.getMode();

		switch (mode) {
/*		case "Quiet":
			System.out.println("Quiet mode selected.");
			Q.happy(PKG);
			break;*/

		case "Normal":
			c.normal(s, PKG);
			break;

		case "Verbose":
			System.out.println("Verbose mode selected.");
			V.happy(PKG);
			break;	

	/*	case "Test":
			System.out.println("Test mode selected.");
			T.happy(PKG);
			break;	*/
		}

	}



}