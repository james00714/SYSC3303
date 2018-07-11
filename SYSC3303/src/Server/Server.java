package Server;

import java.io.*;

public class Server{
	
	private final int port = 69;
	private Controller myControl;
	
	public Server(){}


	public void start() throws IOException {
		myControl = new Controller(port);
		myControl.listenAndHandle();
	}

	public static void main(String[] args) throws IOException{
		Server server = new Server();
		server.start();
	}
}
