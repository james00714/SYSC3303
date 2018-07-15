/*
 * ClientManager class to manage all valid connections
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class ClientManager {
	
	ArrayList<Client> myClients;

	public ClientManager() {
		myClients = new ArrayList<Client>();
	}
	
	/*
	 * Synchronized method to add new client and return size
	 * In: new client
	 * Out: new size
	 * */
	public int addClient(Client c) {
		myClients.add(c);
		return myClients.size();
	}
	
	/*
	 * Synchronized method to remove a client according to a packet
	 * In: received packet
	 * */
	public void removeClient(DatagramPacket packet) throws IOException {
		Client c = getClientByPacket(packet);
		if(c != null) {
			c.remove();
			myClients.remove(c);
		}
	}
	 
	/*
	 * Synchronized method to get amount of current connections
	 * Out: number of connections
	 * */
	public int getNumOfClients() {
		return myClients.size();
	}
	
	/*
	 * Synchronized method get a client by packet receive
	 * In: received packet
	 * Out: Client
	 * */
	public Client getClientByPacket(DatagramPacket packet) {
		Client result = null;
		for(Client c : myClients) {
			if(c.getAddress().equals(packet.getAddress())) {
				if(c.getPort() == packet.getPort()) {
					result = c;
				}
			}
		};
		return result;
	}
}
