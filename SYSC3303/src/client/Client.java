/*
 * Client class to store information of a valid connection
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client {
	
	private InetAddress myAddress;
	private int myPort, myBlockNum;
	private FileHandler myFH;
	
	//	Construct with connect information
	public Client(DatagramPacket receivePacket, int blockNum, FileHandler FH) {
		myAddress = receivePacket.getAddress();
		myPort = receivePacket.getPort();
		myBlockNum = blockNum;
		myFH = FH;
	}
	
	/*
	 * Public information getters
	 * */
	public InetAddress getAddress() {
		return myAddress;
	}
	
	public int getPort() {
		return myPort;
	}
	
	public int getBlockNum() {
		return myBlockNum;
	}
	
	public void incrementBlockNum() {
		myBlockNum++;
	}
	
	public FileHandler getFileHandler() {
		return myFH;
	}
	
	//	Cleaner
	public void remove() throws IOException {
		myFH.close();
	}
}
