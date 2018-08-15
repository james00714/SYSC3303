package errorSimulator;

import java.io.*;
import java.net.*;

public class ESListener extends Thread{
	
	private DatagramPacket receivedPacket;
	private DatagramSocket receiveSendSocket;
	private int errorType,errorChoice, packetChoice, blockChoice, delayChoice;
	private int errorOpcode, errorPacketSize, errorPacketFormat;
	private String errorMode,errorFilename;
	private int tempET,tempEC, tempPC, tempBC, tempDC;
	private int tempEO, tempEP, tempER;
	private String tempEM, tempEF;
	private InetAddress tempIP;
	private boolean lisRunning = false;
	private InetAddress desIP;
	
	public ESListener() {
		try {
			System.out.println("created a socket (port 23)");
			receiveSendSocket = new DatagramSocket(23);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		errorType = -1;

	}

	public void run() {
		System.out.println("Error Simulator: Listener now online.");
		while(true) {
			listen();
		}
	}
	
	public void listen() {
		byte[] data = new byte[1024];
		receivedPacket = new DatagramPacket(data, data.length);
		try {
			System.out.println("Error Simulator: waiting for a packet...");
			receiveSendSocket.receive(receivedPacket);
			System.out.println("received a packet");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
		Thread t = new ESThread(errorType, errorChoice, packetChoice,blockChoice,delayChoice,errorOpcode,
											errorMode, errorFilename, errorPacketSize, errorPacketFormat, receivedPacket, desIP);
		t.start();
	}
	
	public void setErrorType(int errorType) {
		this.tempET = errorType;
		System.out.println("Mode set to " + this.tempET);
	}
	
	public void setErrorChoice(int errorChoice) {
		this.tempEC = errorChoice;
		System.out.println("Error set to " + this.tempEC);
	}
	
	public void setPacketChoice(int packetChoice) {
		this.tempPC = packetChoice;
		System.out.println("Packet set to " + this.tempPC);
	}
	
	public void setBlockChoice(int blockChoice) {
		this.tempBC = blockChoice;
		System.out.println("Block number set to " + this.tempBC);
	}
	
	public void setDelayChoice(int delayChoice) {
		this.tempDC = delayChoice;
		System.out.println("Delay time set to " + this.tempDC + " ms.");
	}
	
	public void setErrorOpcode(int errorOpcode) {
		this.tempEO = errorOpcode;
		System.out.println("Error Opcode set to " + this.tempEO);
	}
	
	public void setErrorMode(String  errorMode) {
		this.tempEM = errorMode;
		System.out.println("Error Mode set to " + this.tempEM);
	}
	
	public void setErrorFilename(String errorFilename) {
		this.tempEF = errorFilename;
		System.out.println("Error Filename set to " + this.tempEF);
	}

	public void setErrorPacketSize(int errorPacketSize){
		this.tempEP = errorPacketSize;
		System.out.println("Error Packet Size set to " + this.tempEP);
	}
	
	public void setErrorPacketFormat(int errorPacketFormat) {
		this.tempER = errorPacketFormat;
		System.out.println("Error Packet Format set to " + this.tempER);
	}
	
	
	public void setDesIP(InetAddress desIP) {
		this.tempIP = desIP;
		System.out.println("Destination IP address set to " + this.tempIP);
	}
	
	public void confirmChange() {
		errorType = tempET;
		errorChoice = tempEC;
		packetChoice = tempPC;
		blockChoice = tempBC;
		delayChoice = tempDC;
		errorMode = tempEM;
		errorOpcode = tempEO;
		errorFilename = tempEF;
		errorPacketSize = tempEP;
		errorPacketFormat = tempER;
		desIP = tempIP;
		System.out.println("Error configration submited.");
		if(lisRunning == false) {
			lisRunning = true;
			start();
		}	
	}
	
	public void quit() {
		System.out.println("Quiting Error Simulator...");
	}
	
}
