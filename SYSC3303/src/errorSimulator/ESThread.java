package errorSimulator;

import java.io.IOException;
import java.net.*;

public class ESThread extends Thread{

	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket receiveSendSocket;

	private int errorType, errorChoice, errorPacket, blockChoice, delayChoice;
	private int errorOpcode, errorBlkNum, errorPacketSize, errorPacketFormat, errorTID;
	private String errorMode, errorFilename, errorAddress;
	private InetAddress clientAddress;
	private int clientPort;
	private int serverPort = 69;
	private RequestParser rp;
	private int ID;
	private boolean continueListen = true;
	private DatagramSocket errorSocket;
	
	public ESThread(int errorType, int errorChoice, int errorPacket, int blockChoice, int delayChoice, 
			        int errorOp, String errorMode, String errorFilename, int errorBlkN, int errorPS,
			        int errorPF, int errorTID, String errorAddr, DatagramPacket received) {

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
		this.errorBlkNum = errorBlkN;
		this.errorPacketSize = errorPS;
		this.errorPacketFormat = errorPF;
		this.errorTID = errorTID;
		this.errorAddress = errorAddr;
		this.receivedPacket = received;
		this.clientAddress = receivedPacket.getAddress();
		this.clientPort = receivedPacket.getPort();

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
		printPacketInfo(receivedPacket);
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
		if(errorType == 0) return false;
		
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

	public void errorCode() {

		switch(errorChoice) {
		case 1: System.out.println("File not found"); break;
		case 2: System.out.println("Access violation"); break;
		case 3: System.out.println("Disk full or allocation exceeded"); break;
		case 4: System.out.println("Illegal TFTP operation"); break;
		case 5: System.out.println("Unknown transfer ID"); break;
		case 6: System.out.println("File already exists"); break;
		default: System.out.println("Oops, something is wrong"); break;
		}

		switch(errorPacket) {
		case 1: System.out.println("Modify RRQ"); break;
		case 2: System.out.println("Modify WRQ"); break;
		case 3: System.out.println("Modify DATA"); break;
		case 4: System.out.println("Modify ACK"); break;
		case 5: System.out.println("Modify ERROR"); break;
		default: System.out.println("Oops, something is wrong"); break;
		}
	}
  
	public void sendPacket(DatagramPacket sendPacket) {

		try {
			receiveSendSocket.send(sendPacket);
			System.out.println("Error Simulator: Packet sent:");
			printPacketInfo(sendPacket);
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
			printPacketInfo(receivedPacket);
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
			System.out.println("Target packet has lost.");
		}
		else if(errorChoice == 2) {
			System.out.println("Delaying... " + delayChoice + "ms");
			try {
				Thread.sleep(delayChoice);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			transferPacket(receivedPacket);
		}else if(errorChoice == 3) {
			System.out.println("Duplicating...");
			transferPacket(receivedPacket);
			transferPacket(receivedPacket);
		}else {
			System.out.println("invalid error choice");
		}
	}

	public void transferPacket(DatagramPacket receivedPacket) {
		System.out.println("Passing packet received...");
		if(ifClient(receivedPacket)) {
			try {
				sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), InetAddress.getLocalHost(), this.serverPort);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			sendPacket(sendPacket);
		}else {
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.clientAddress, this.clientPort);
			sendPacket(sendPacket);
		}
	}
	
	public void makeErrorCodeError(DatagramPacket receivedPacket) {
		System.out.println("Parsing error code choice...");
		if(errorPacket == 1 || errorPacket == 2) {
			if(errorChoice == 1) {
				System.out.println("Modify Mode...");
				transferPacket(modifyMode(receivedPacket, errorMode));
			}else if(errorChoice == 2) {
				transferPacket(modifyOpcode(receivedPacket, errorOpcode));
			}else if(errorChoice == 3) {
				transferPacket(modifyFilename(receivedPacket, errorFilename));
			}else if(errorChoice == 4) {
				transferPacket(modifyPacketSize(receivedPacket, errorPacketSize));
			}else if(errorChoice == 5) {
				transferPacket(modifyPacketFormat(receivedPacket, errorPacketFormat));
			}else if(errorChoice == 6) {
				transferErrorFivePacket(receivedPacket, 1);
			}else {
				transferErrorFivePacket(receivedPacket, 2);
			}

		}else if(errorPacket == 3 || errorPacket == 4) {
			if(errorChoice == 1) {
				transferPacket(modifyOpcode(receivedPacket, errorOpcode));
			}else if(errorChoice == 2) {
				transferPacket(modifyBlockNum(receivedPacket, errorBlkNum));
			}else if(errorChoice == 3) {
				transferPacket(modifyPacketSize(receivedPacket, errorPacketSize));
			}else if(errorChoice == 4) {
				transferErrorFivePacket(receivedPacket, 1);
			}else{
				transferErrorFivePacket(receivedPacket, 2);
			}
		}else {
			////////Error Packet
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
		
		for(int i = 0; i < data.length; i++) sendData[i] = data[i];
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
	
	public DatagramPacket modifyBlockNum(DatagramPacket receivedPacket, int blkNum) {
		byte[] sendData = new byte[receivedPacket.getLength()];
		
		for(int i = 0; i < receivedPacket.getLength(); i++) {
			sendData[i]= receivedPacket.getData()[i];
		}
		
		sendData[2] = (byte) (blkNum / 256);
		sendData[3] = (byte) (blkNum % 256);
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}

	public void transferErrorFivePacket(DatagramPacket receivedPacket, int error) {
		System.out.println("Passing packet received...");

		if(error == 1) {
			
		}else {
			
		}
	}

	public boolean ifClient(DatagramPacket receivedPacket) {
		if(receivedPacket.getPort() == clientPort && receivedPacket.getAddress().equals(clientAddress)) return true;
		return false;
	}

	public void printPacketInfo(DatagramPacket received){
		System.out.println("Address: " + received.getAddress());
		System.out.println("Port: " + received.getPort());
		int len = received.getLength();
		System.out.println("Length: " + len);
	}
}
