/*
 *SYSC3303 Project G11 Client
 */
package client;

import java.io.*;
import java.util.Scanner;

public class Client {
	private	String mode, request, fig;
	private	String fileName;
	private File check, space;

	private	boolean running = true;


	public Client (){}

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

	/*
	 * Method for the UI
	 * First ask user input for mode, then read/write request and finally output mode 
	 */
	public void menu (){
		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome to client V2 <Enter quit to quit anytime :(>");
		System.out.println("Please select your mode:");
		System.out.println("1. Normal  <Client, Server>");
		System.out.println("2. Test  <Client, Error Simulator, Server>");
		mode = sc.next();
		if (!mode.equals("quit")) {
			System.out.println("Please select your request");
			System.out.println("1. RRQ <Read Request>");
			System.out.println("2. WRQ <Write Request>");
			request = sc.next();
			System.out.println("Default Mode: netascii");
			if (!request.equals("quit")) {
				System.out.println("Please enter your file Name ");
				fileName = sc.next();

				if (request.equals("1")) {
					if (!this.checkDisk("\\")) {
						System.out.println("Disk full can't read.");
						System.out.println	("Please delete file and come again :)");
					}

					if (this.checkDisk("\\")) {
						while (this.checkFile(fileName)) {
							if (fileName.equals("quit")) {
								sc.close();
								break;
							}

							System.out.println("File already exist error.");
							System.out.println	("Please enter a new file Name");
							fileName = sc.next();
						}
					}

				}else if (request.equals("2")) {
					while(true) {
						if (fileName.equals("quit")) {
							sc.close();
							return;
						}
						if(!this.checkFile(fileName)) {
							System.out.println("File not found error.");
							System.out.println("Please re-enter your file Name ");
							fileName = sc.next();
							continue;
						}
						if(!this.permission(fileName)) {
							System.out.println("Please enter a new file Name ");
							fileName = sc.next();
							continue;
						}
						break;
					}
				}else{
					System.out.println("Error");
				}
				if(!fileName.equals("quit")) {
					System.out.println("Please enter your mode for data");
					System.out.println("1. Verbose");
					System.out.println("2. Quiet");
					fig = sc.next();		
				}else {
					sc.close();
				}
			}else {
				sc.close();
			}
		}else {
			sc.close();
		}
	}

	//file not found
	public boolean checkFile (String fileName) {
		check = new File ("src\\client\\files\\" + fileName);
		if (check.exists()) {
			System.out.println("File exist.");
			return true;
		}
		return false;
	}

	//access violation
	public boolean permission (String fileName) {
		//;
		/*	if (permit.canRead()) {
			System.out.println("Access granted");
			return true;
		}*/

		try {
			FileInputStream permit = new FileInputStream(new File ("src\\client\\files\\" + fileName));
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
		space = new File ("src\\client\\files\\" + fileName);
		if (space.getUsableSpace() > 0) {
			System.out.println("Disk available space: " +space.getUsableSpace());
			return true;
		}
		return false;
	}

	/*
	 * Open Normal/Test mode depends on the user input in UI, if user enter quit end the program
	 */
	public void start (Sender s) throws IOException{

		if (mode.equals("quit") || request.equals("quit") || fileName.equals("quit") || fig.equals("quit")) {
			running = false;
			System.out.println("Thank your for using our program. Goodbye!");
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
