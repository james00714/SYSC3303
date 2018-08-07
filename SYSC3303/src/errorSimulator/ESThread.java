package errorSimulator;

import java.io.IOException;
import java.net.*;

public class ESThread extends Thread{

	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket receiveSendSocket;

	private int errorType, errorChoice, errorPacket, blockChoice, delayChoice;
	private int errorOpcode, errorPacketSize, errorPacketFormat;
	private String errorMode, errorFilename;
	private InetAddress clientAddress;
	private int clientPort;
	private int serverPort = 69;
	private RequestParser rp;
	private int ID;
	private boolean continueListen = true;
	private InetAddress serverAddress;
	
	public ESThread(int errorType, int errorChoice, int errorPacket, int blockChoice, int delayChoice, 
			        int errorOp, String errorMode, String errorFilename, int errorPS,
			        int errorPF, DatagramPacket received, InetAddress serverAddress) {

		//		byte[] sendData = new byte[1024];

		//		sendPacket = new DatagramPacket(sendData, sendData.length);

		this.errorType = errorType;
		this.errorChoice = errorChoice;
		this.errorPacket = errorPacket;
		this.blockChoice = blockChoice;
		this.delayChoice = delayChoice;
		this.errorOpcode = errorOp;
		this.errorMode = errorMode;
		this.errorFilename = errorFilename;
		this.errorPacketSize = errorPS;
		this.errorPacketFormat = errorPF;
		this.receivedPacket = received;
		this.clientAddress = receivedPacket.getAddress();
		this.clientPort = receivedPacket.getPort();
		this.serverAddress = serverAddress;

		try {
			receiveSendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		rp = new RequestParser();
		ID = receiveSendSocket.getLocalPort();

		System.out.println("Created a thread");

	}

	public void run() {
		System.out.println("Thread is running...");
		
		if(!ifClient(receivedPacket)) {
			if(serverPort == -1) 
				this.serverPort = receivedPacket.getPort();
		}
		
		System.out.println("Packet received:");
		while(continueListen) {
			tryError(receivedPacket);
			receive();
		}

	}

	private void tryError(DatagramPacket receivedPacket){
		rp.parseRequest(receivedPacket.getData(), receivedPacket.getLength());

		
		
		if(ifError(receivedPacket)) {
			System.out.println("Target packet received, making error...");
			if(errorType == 1) {
				System.out.println("Making Transimission error...");
				makeTransmissionError(receivedPacket);
				errorType = 0;
			}else if(errorType == 2) {
				System.out.println("Making Error Code error...");
				makeErrorCodeError(receivedPacket);
				errorType = 0;
			}

		}else {	
			transferPacket(receivedPacket);		
		}
	}

	private boolean ifError(DatagramPacket receivedPacket) {
		if(errorType == 0) {
			System.out.println("No Error");
			return false;
		}
		if(errorPacket == rp.getType()) {
			if(errorPacket != 3 && errorPacket != 4) {
				return true;
			}else {
				if(blockChoice == rp.getBlockNum()) {
					return true;
				}
			}
		}
		return false;
	}
  
	public void sendPacket(DatagramPacket sendPacket) {

		try {
			receiveSendSocket.send(sendPacket);
			System.out.println("Error Simulator: Packet sent:");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	} 

	public void receive() {
		try {
			receiveSendSocket.setSoTimeout(10000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println("Receving a packet...");
			receiveSendSocket.receive(receivedPacket);
			
			if(!ifClient(receivedPacket)) {
				if(serverPort == 69) 
					this.serverPort = receivedPacket.getPort();
			}

			System.out.println("Error Simulator: Packet received:");
		}catch(SocketTimeoutException e1) {
			System.out.println(ID + ": Timeout, closing thread.");
			continueListen = false;

			return;
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	

	private void makeTransmissionError(DatagramPacket receivedPacket) {
		if(errorChoice == 1) {
			if(rp.getType() == 3 || rp.getType() == 4) {
				System.out.println("Target " + parsePacketName(rp.getType()) + " Packet Block# : " + rp.getBlockNum() + "has lost.");
			}else {
				System.out.println("Target " + parsePacketName(rp.getType()) + " Packet has lost.");
			}
		}
		else if(errorChoice == 2) {
			if(rp.getType() == 3 || rp.getType() == 4) {
				System.out.println("Delaying Target" + parsePacketName(rp.getType()) + " Packet Block# : " + rp.getBlockNum() + " " + delayChoice + "ms");
			}else {
				System.out.println("Delaying Target" + parsePacketName(rp.getType()) + " Packet " + delayChoice + "ms");
			}
			
			try {
				Thread.sleep(delayChoice);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			transferPacket(receivedPacket);
		}else if(errorChoice == 3) {
			if(rp.getType() == 3 || rp.getType() == 4) {
				System.out.println("Duplicating Target" + parsePacketName(rp.getType()) + " Packet Block# : " + rp.getBlockNum());
			}else {
				System.out.println("Duplicating Target" + parsePacketName(rp.getType()) + " Packet");
			}
			
			transferPacket(receivedPacket);
			transferPacket(receivedPacket);
		}else {
			System.out.println("invalid error choice");
		}
	}

	public void transferPacket(DatagramPacket receivedPacket) {
		System.out.println("Passing packet received...");
		if(ifClient(receivedPacket)) {
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.serverAddress, this.serverPort);
			sendPacket(sendPacket);
		}else {
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.clientAddress, this.clientPort);
			sendPacket(sendPacket);
		}
	}
	
	public void makeErrorCodeError(DatagramPacket receivedPacket) {
		System.out.println("Parsing error code choice...");
		
		
		if(errorPacket == 1 || errorPacket == 2) {
			switch(errorChoice) {
				case 1: 
					System.out.println("Modify Mode for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyMode(receivedPacket, errorMode)); 
					break;
				case 2: 
					System.out.println("Modify Opcode for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyOpcode(receivedPacket, errorOpcode)); 
					break;
				case 3: 
					System.out.println("Modify Filename for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyFilename(receivedPacket, errorFilename)); 
					break;
				case 4: 
					System.out.println("Modify Packet size for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyPacketSize(receivedPacket, errorPacketSize)); 
					break;
				case 5: 
					System.out.println("Modify Packet format for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyPacketFormat(receivedPacket, errorPacketFormat)); 
					break;
				case 6: 
					System.out.println("Making Error Code 5555555555 for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferErrorFivePacket(receivedPacket); 
					break;
				default: 
					System.out.println("invalid error choice"); 
					break;
			}

		}else if(errorPacket == 3 || errorPacket == 4) {
			
			switch(errorChoice) {
				case 1: 
					System.out.println("Modify Opcode for target" + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferPacket(modifyOpcode(receivedPacket, errorOpcode));
					break;
				case 2: 
					System.out.println("Modify Block number for target " + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferPacket(modifyBlockNum(receivedPacket));
					break;
				case 3: 
					System.out.println("Modify Packet size for target " + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferPacket(modifyPacketSize(receivedPacket, errorPacketSize)); 
					break;
				case 4: 
					System.out.println("Making Error Code 5555555555 for target " + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferErrorFivePacket(receivedPacket); 
					transferPacket(receivedPacket);
					break;
				default: 
					System.out.println("invalid error choice"); 
					break;
			}
		}else {
			System.out.println("Error Packet received, transferring...");
			transferPacket(receivedPacket);
		}
	}
	
	public DatagramPacket modifyOpcode(DatagramPacket receivedPacket, int opcode) {
		byte[] sendData = new byte[receivedPacket.getLength()];
		for(int i = 0; i < receivedPacket.getLength(); i++) {
			sendData[i] = receivedPacket.getData()[i];
		}
		
		sendData[1] = (byte) opcode;
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyMode(DatagramPacket receivedPacket, String mode) {
		byte[] errorMode = mode.getBytes();
		byte[] sendData = new byte[4 + rp.getFilename().getBytes().length + errorMode.length];
		System.out.println(rp.getFilename().getBytes().length);
		System.out.println(errorMode.length);
		
		sendData[0] = 0;
		sendData[1] = (byte)rp.getType();
		
		for(int i = 0; i < rp.getFilename().getBytes().length; i++) {
			sendData[2+i] = rp.getFilename().getBytes()[i];
		}
		
		sendData[2+rp.getFilename().getBytes().length] = 0;    //0 1 2 3 4 5 6 7 8 9 10
		
		for(int i = 0; i < errorMode.length; i++) {
			sendData[3+rp.getFilename().getBytes().length + i] = errorMode[i]; 
		}
		
		sendData[3 +rp.getFilename().getBytes().length + errorMode.length] = 0;
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyFilename(DatagramPacket receivedPacket, String filename) {
		byte[] fileName = filename.getBytes();
		byte[] sendData = new byte[4 + fileName.length + rp.getMode().getBytes().length];
		
		sendData[0] = 0;
		sendData[1] = (byte)rp.getType();
		
		for(int i = 0; i < fileName.length; i++) {
			sendData[2+i] = fileName[i];
		}
		
		sendData[2+fileName.length] = 0;
		
		for(int i = 0; i < rp.getMode().getBytes().length; i++) {
			sendData[3+fileName.length + i] = rp.getMode().getBytes()[i]; 
		}
		
		sendData[3 + fileName.length + rp.getMode().getBytes().length] = 0;
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyPacketSize(DatagramPacket receivedPacket, int packetSize) {
		byte[] sendData = new byte[packetSize];
		byte[] data = receivedPacket.getData();
		
		System.arraycopy(data, 0, sendData, 0, packetSize);
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyPacketFormat(DatagramPacket receivedPacket, int packetFormat) {
		byte[] filename = rp.getFilename().getBytes();
		byte[] mode = rp.getMode().getBytes();
		byte[] sendData = new byte[4+filename.length+mode.length];
		
		sendData[0] = 0;
		sendData[1] = (byte)rp.getType();
		
		for(int i = 0; i < filename.length; i++) {
			sendData[2+i] = filename[i];
		}
		
		sendData[2+filename.length] = (byte)packetFormat;
		
		for(int i = 0; i < mode.length;i++) {
			sendData[3+filename.length + i] = mode[i];
		}
		
		sendData[3+filename.length+mode.length] = (byte) packetFormat;
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyBlockNum(DatagramPacket receivedPacket) {
		
		System.out.println("receivedPacket.getLength(): " + receivedPacket.getLength());
		
		byte[] sendData = new byte[receivedPacket.getLength()-2];
		
		sendData[0] = 0;
		sendData[1] = (byte) rp.getType();
		
		if(errorPacket == 3) {
			for(int i = 0; i <rp.getFileData().length; i++) {
				sendData[2+i]= rp.getFileData()[i];
			}
		}
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}

	public void transferErrorFivePacket(DatagramPacket receivedPacket) {

		if(ifClient(receivedPacket)) {
			System.out.println("Creating a new socket (Error code 5)...");
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.serverAddress, this.serverPort);
			
			Thread err5C = new ESThread(0, 0, errorPacket, blockChoice, delayChoice, errorOpcode, errorMode, errorFilename, errorPacketSize, errorPacketFormat, sendPacket, serverAddress);

			err5C.start();
			
		}else {
			System.out.println("Creating a new socket (Error code 5)...");
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.clientAddress, this.clientPort);
			
			Thread err5S = new ESThread(0, 0, errorPacket, blockChoice, delayChoice, errorOpcode, errorMode, errorFilename, errorPacketSize, errorPacketFormat, sendPacket, clientAddress);

			err5S.start();
		}
		
	}

	public boolean ifClient(DatagramPacket receivedPacket) {
		if(receivedPacket.getPort() == clientPort && receivedPacket.getAddress().equals(clientAddress)) return true;
		return false;
	}

	public String parsePacketName(int packetType) {
		
		String packetName;
		
		if(packetType == 1) {
			packetName = "RRQ";
		}else if(packetType == 2) {
		    packetName = "RRQ";
		}else if(packetType == 3) {
		    packetName = "DATA";
		}else if(packetType == 4) {
			packetName = "ACK";
		}else {
			packetName = "ERROR";
		}
		return packetName;	
	}
}
