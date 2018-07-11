package Client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client{
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket sendPacket, receivePacket;
	private static String mode = "";
	private static String fileName = "";
	private static String request = "";
	public static int blockNum;
	public static FileHandler fileHandler;
	public static RequestParser RP;

	public Client()
	{
		try {			
			sendReceiveSocket = new DatagramSocket(30);
		} catch (SocketException se) {  
			se.printStackTrace();
			System.exit(1);
		}
	}



	public void normal (Send s, UI PKG) throws IOException
	{

		System.out.println("Normal mode selected");


		byte msg[];
		
		if (PKG.getRequest().equals ("WRQ")){
			s.WRQ(PKG);
			msg = s.getWrite();
		}else{
			s.RRQ(PKG);
			msg = s.getRead();
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


		
		listenAndHandle(s, msg);
		
		
		//		sendReceiveSocket.close();
	}
	
	public void listenAndHandle(Send s, byte[] msg) throws IOException{
		byte data[] = new byte[1024];
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
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");


		String received = new String(data,0,len);   
		System.out.println(received);
		
		RP = new RequestParser();
		RP.parseRequest(data, len);
		if (data[1] == 3) {
			int block = RP.getBlockNum();
			/*
			for(int i = 0;i < data.length; i++){
				System.out.println(data[i]);
			}*/
			if(block == blockNum){
				fileHandler.writeFile(RP.getFileData());
				if (len == 516){
					blockNum++;
					s.Ack(blockNum);
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
					listenAndHandle(s, msg);
				
			}else{
				
					fileHandler.close();
				}
			}else{
				// error
			}
			
		}
		
		if (data[1] == 4){
			System.out.println("ACK packet received.");
			int block = RP.getBlockNum();
			System.out.println(blockNum);
			System.out.println(block);

			if (block == blockNum){
				
				byte[] fileData = fileHandler.readFile();;
				System.out.print(fileData.length);
				byte[] sendData = new byte[4 + fileData.length];
				sendData[0] = 0;
				sendData[1] = 3;
				
				sendData[2] = (byte)(blockNum / 256);
				sendData[3] = (byte)(blockNum % 256);
				//	Save file data to data packet
				for(int i = 0; i < fileData.length; i++) {
					sendData[4 + i] = fileData[i];
				}
				
				
				try {
					sendPacket = new DatagramPacket(sendData, sendData.length,
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
				
				
				if (fileData.length == 512){
					blockNum++;
					listenAndHandle(s, sendData);	
				}else {
					fileHandler.close();
				}
			}else {
				//error
			}
		}
		
	}
	
	private int parseBlockNum(byte[] data) {
		int left = data[2];
		int right = data[3];
		if(left < 0) left += 256;
		if(right < 0) right += 256;
		return left * 256 + right;
	}

	public void quit(){

		sendReceiveSocket.close();
		System.exit(1);
	}	
	
	
	public void request(UI PKG, Send s) throws IOException{
		Verbose V = new Verbose();

		mode = PKG.getMode();
		switch (mode) {
		/*		case "Quiet":
			System.out.println("Quiet mode selected.");
			Q.happy(PKG);
			break;*/

		case "Normal":
			this.normal(s, PKG);
			break;

		case "Verbose":
			System.out.println("Verbose mode selected.");
			V.happy(s, PKG);
			break;	

			/*	case "Test":
			System.out.println("Test mode selected.");
			T.happy(PKG);
			break;	*/
		}
	}

	public static void main(String[] args) throws IOException{
		Client c = new Client();

		UI PKG = new UI ();
		Send s = new Send ();

		//		Quiet Q = new Quiet();
		//	Test T = new Test();


		UI.IT1(c, PKG, s);

	}



}