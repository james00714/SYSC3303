/*
 * Client class to store information of a valid connection
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Client {
	
	private static ArrayList<Client> activeClients = new ArrayList<>();
	
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
	
	public synchronized static boolean addToClients(Client c) {
		if(findSameClient(c) == false) {
			activeClients.add(c);
			return true;
		}else {
			return false;
		}
		
	}
	
	public synchronized void removeFromClients() {
		activeClients.remove(this);
		notifyAll();
	}
	
	public static boolean findSameClient(Client client) {
		for(Client c : activeClients) {
			if(c.getAddress().equals(client.getAddress()) && 
					c.getPort() == client.getPort()) return true;
		}
		return false;
	}
	
	public static int getClientsSize() {
		return activeClients.size();
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
	public void close() throws IOException {
		myFH.close();
		removeFromClients();
	}
}
