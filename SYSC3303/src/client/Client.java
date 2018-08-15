/*
 *SYSC3303 Project G11 Client
 */
package client;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
  
	private	String mode, request, fig = "1";
	private	String fileName;
	private File check;

	private	boolean running = true;
	private String destination, location = "src\\client\\files";
	private InetAddress ipAddress;
	private Scanner sc = new Scanner(System.in);

	public Client (){
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public String getMode (){
		return this.mode;
	}

	public String getRequest (){
		return this.request;
	}

	public String getFileName (){
		return this.fileName;
	}

	public String getFig (){
		return this.fig;
	}

	public String getLocation () {
		return this.location;
	}

	public InetAddress getIpAddress () {
		return this.ipAddress;
	}


	/*
	 * Method for the UI
	 * First ask user input for mode, then read/write request and finally output mode 
	 */
	public void menu () throws UnknownHostException{
		
		System.out.println("------Welcome to TFTP Client System------");
		System.out.println("Please choose your operation: ");
		System.out.println("\t1. RRQ");
		System.out.println("\t2. WRQ");
		System.out.println("\t3. Set Target IP");
		System.out.println("\t4. Set Working Directory");
		System.out.println("\t5. Set Print Mode (Quiet/Verbose)");
		System.out.println("\t6. Quit");
		String cmd = sc.next();
		
		switch(cmd){
			case "1":
				request = "1";
				setRequest();
				break;
			case "2":
				request = "2";
				setRequest();
				break;
			case "3":
				setIP();
				menu();
				break;
			case "4":
				chooseDir();
				menu();
				break;
			case "5":
				setPrintMode();
				menu ();
				break;
			case "6":
				running = false;
				break;
		    default:
		    	System.out.println("Invalid Input, please try again.");
		    	menu();
		    	break;
		}
	}
	
	/*
	 * Method to generate a request
	 * */
	private void setRequest() {
		setTransferMode();
	}

	/*
	 * Method to set transfer mode
	 * Normal sends directly to server (69)
	 * Test sends to the Error Simulator (23)
	 * */
	private void setTransferMode() {
		System.out.println("Please select your mode:");
		System.out.println("\t1. Normal  <Client, Server>");
		System.out.println("\t2. Test  <Client, Error Simulator, Server>");
		System.out.println("\t3. Back to main menu");
		String cmd = sc.next();
		
		switch(cmd){
		
			case "1":
				mode = "1";
				System.out.println("Transfer mode set to Normal.");
				setFilename();
				break;
			case "2":
				mode = "2";
				System.out.println("Transfer mode set to Test.");
				setFilename();
				break;
			case "3":
				try {
					menu();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				break;
		    default:
		    	System.out.println("Invalid Input, please try again.");
		    	setTransferMode();
		    	break;
		}
	}
	
	/*
	 * Method to set and check the requested file 
	 */
	private void setFilename() {
		System.out.println("Please enter your file name, enter back to return");
		fileName = sc.next();
		if(fileName.equals("back")) {
			try {
				menu();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			// check READ request
			if (request.equals("1")) {
				if (!checkDisk(fileName)) {
					System.out.println("Disk full can't read.");
					System.out.println	("Please delete file and come :)");
					try {
						menu();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
				if (checkFile(fileName)) {
					System.out.println("File already exist.");
					setFilename();
				}
			}
			// check WRITE request
			else if (request.equals("2")) {
				if(!this.checkFile(fileName)) {
					System.out.println("File not found error.");
					setFilename();
				}
				if(!this.permission(fileName)) {
					System.out.println("Access denied.");
					setFilename();
				}			
			}
		}
		
	}
	
	/*
	 * Method to set print mode
	 * Verbose print all information
	 * */
	private void setPrintMode() {
		System.out.println("Please enter your mode for data");
		System.out.println("\t1. Verbose");
		System.out.println("\t2. Quiet");
		System.out.println("\t3. Back to main menu");
		String cmd = sc.next();
		
		switch(cmd){
		
			case "1":
				fig = "1";
				System.out.println("Print mode set to Verbose.");
				break;
			case "2":
				fig = "2";
				System.out.println("Print mode set to Quiet.");
				break;
			case "3":
				break;
		    default:
		    	System.out.println("Invalid Input, please try again.");
		    	setPrintMode();
		    	break;
		}
	}
	
	/*
	 * Method to select directory
	 * Default dir: src\client\files\
	 * */
	private void chooseDir(){
		System.out.println("Please select your location");
		System.out.println("\t1. src\\client\\files\\");
		System.out.println("\t2. I have my own directory");
		System.out.println("\t3. Back to main menu");
		String cmd = sc.next();
		
		switch(cmd){
		
		case "1":
			location = "src\\client\\files";
			System.out.println("Dir set to src\\client\\files\\");
			break;
		case "2":
			setDir();
			break;
		case "3":
			break;
	    default:
	    	System.out.println("Invalid Input, please try again.");
	    	chooseDir();
	    	break;
		}
		
	}
	
	/*
	 * Method to set a user defined directory and check
	 * */
	private void setDir() {
		System.out.println("Please enter your location, enter back to return.");
		System.out.println("");
		String dir = sc.next();
		if(dir.equals("back")) return;
		if(dir.charAt(dir.length()-1) != '\\')
			dir += '\\';
		File f = new File(dir);
		if(f.isDirectory()) {
			location = dir;
			System.out.println("Working directory set to " + dir);
		}else {
			System.out.println("Directory not exists, please try again.");
			setDir();
		}
	}
	
	/*
	 * Method to set destination IP address
	 * Default IP: localhost
	 * */
	private void setIP() throws UnknownHostException {
		System.out.println("Please select your destination");
		System.out.println("\t1. Local host");
		System.out.println("\t2. I have my own destination");
		System.out.println("\t3. Back to main menu");
		String cmd = sc.next();
		
		switch(cmd){
		
		case "1":	
			ipAddress = InetAddress.getLocalHost();
			System.out.println("IP set to " + ipAddress.getHostName());
			break;
		case "2":
			System.out.println("Please enter the destination IP address");
			destination = sc.next();
			ipAddress = InetAddress.getByName(destination);
			System.out.println("IP set to " + ipAddress.getHostName());
			break;
		case "3":
			break;
	    default:
	    	System.out.println("Invalid Input, please try again.");
	    	setIP();
	    	break;
		}
	}

	//file not found
	public boolean checkFile (String fileName) {
		check = new File (location + "\\" + fileName);
		if (check.exists()) {
			return true;
		}
		return false;
	}

	//access violation
	public boolean permission (String fileName) {
		try {
			FileInputStream permit = new FileInputStream(new File (location + "\\" + fileName));
			byte [] fileBuffer = new byte[512];	
			permit.read(fileBuffer);
			permit.close();
		}catch(IOException e) {
			// Access denied
			if(e.getMessage().contains("Access is denied")) {
				System.out.println("ERROR: Access Violation.");

			}else {
				System.out.print("IO Exception: likely:");
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	//check disk full
	public boolean checkDisk (String fileName) {
		File check = new File (location);
		if (check.getUsableSpace() > 0) {
			System.out.println("Disk available space: " + check.getUsableSpace());
			return true;
		}
		return false;
	}

	/*
	 * Open Normal/Test mode depends on the user input in UI, if user enter quit end the program
	 */
	public void start (Sender s) throws IOException{

		if (running == false) {
			System.out.println("Thank your for using our program. Goodbye!");
			sc.close();
			s.Close();
			//t.close();	
		}else {
			if (mode.equals("1")){
				s.start(this, 69);
			}else if(mode.equals("2")){
				s.start(this, 23);
			}else {
				System.out.println("Invalid mode input, please try again.");
			}
		}	
	}

	/*
	 * Client Starts
	 */
	public static void main(String[] args) throws IOException {
		Client c = new Client();
		Sender n = new Sender (c);
		while(c.running) {
			c.menu();
			c.start(n);	
		}	
		
	}
}
