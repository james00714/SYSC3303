/*
 * UI class for error simulator
 * Allow User to modify the packet to create different error codes 
*/
package errorSimulator;

import java.util.Scanner;

public class UI {

	RequestParser RP;
	String cmd;
	Scanner in;

	public UI(RequestParser RP) {
		this.RP = RP;
		in = new Scanner(System.in);
	}



	/*
	 * Menu for the Error simulator
	*/
	public int mainMenu() {

		int index = 3, num = 0;
		System.out.println("-------------------New Packet Received------------------");
		int type = RP.getType();
		System.out.println("\tPacket Type: " + RP.getTypeName());

		if(type == 1 || type == 2) {
			System.out.println("\tRequested File: " + RP.getFilename());
		}else if(type == 3 || type == 4){
			System.out.println("\tBlock#: " + RP.getBlockNum());
			if(type == 3) {
				System.out.println("\tData Length: " + RP.getFileData().length);
			}
		}else if(type == 5){
			System.out.println("\tErrorCode: " + RP.getErrCode());
			System.out.println("\tError Message: " + RP.getErrMsg());
		}

		System.out.println("\n\tAvailable Operations:");
		System.out.println("\t0. Normal Operation");
		System.out.println("\t1. Modify Op#");	

		if(type == 1 || type == 2) {
			System.out.println("\t2. Modify Filename");
			index = 2;
		}else if(type == 3 || type == 4){
			System.out.println("\t2. Modify Block#");
			index = 2;
			if(type == 3) {
				System.out.println("\t3. Modify Data");
				index = 3;
			}
		}else if(type == 5){
			System.out.println("\t2. Modify ErrorCode");
			System.out.println("\t3. Modify Error Message");
			index = 3;
		}
		cmd = in.next();
		while(true) {
			try{
				num = Integer.parseInt(cmd);
			} catch (NumberFormatException e) {
				System.out.println("\t\nInvalid input, please try again.");
				cmd = in.next();
				continue;
			}
			if(num < 0 || num > index) {
				System.out.println("\t\nInvalid input, please try again.");
				cmd = in.next();
				continue;
			}else {
				break;
			}

		}
		return num;
	}

	/*
	 * Helper method for UI
	*/
	public int type2 () {
		int second = -1, type = RP.getType();
		if (type == 1 || type ==2) {
			second = 1;
		}else if (type == 3 || type ==4) {
			second = 2;
		}else if (type == 5) {
			second = 3;
		}else {
			System.out.println("Error.");
		}

		return second; 
	}

	/*
	 * Helper method for UI
	*/
	public int type3() {
		int third = -1, type = RP.getType();
		if (type == 3) {
			third = 1;
		}else if (type == 5) {
			third = 2;
		}else {
			System.out.println("Error");
		}
		
		return third;
	}

	/*
	 * Ask user for new Opcode
	*/
	public int askOpCode() {
		int num = -1;
		System.out.println("Please enter new Opcode: ");
		cmd = in.next();
		while(true) {
			try{
				num = Integer.parseInt(cmd);
				return num;
			} catch (NumberFormatException e) {
				System.out.println("\t\nInvalid input, please try again.");
				cmd = in.next();
				continue;
			}

		}
	}
	
	/*
	 * Ask user for new file name
	*/
	public byte [] askFileName() {
		String newName = "";
		System.out.println("Please enter new Filename");
		newName = in.next();
		byte [] fileName = newName.getBytes();
		return fileName;
	}

	/*
	 * Ask user for new block number
	*/
	public int askBkNumber() {
		int Bk = -1;
		System.out.println("Please enter new Block number");
		cmd = in.next();
		while(true) {
			try {
				Bk = Integer.parseInt(cmd);
				if (Bk >= 0 && Bk <= 65535) {
					return Bk;
				}
			}catch (NumberFormatException e) {
				System.out.println("\t\nInvalid input, please try again.");
				cmd = in.next();
				continue;
			}
		}
	}

	/*
	 * Ask user for new error code
	*/
	public int askErrCode() {
		int code = -1;
		System.out.println("Please enter new Error code");
		cmd = in.next();
		while(true) {
			try {
				code = Integer.parseInt(cmd);
				if (code >= 0 && code <= 7) {
					return code;
				}
			}catch (NumberFormatException e) {
				System.out.println("\t\nInvalid input, please try again.");
				cmd = in.next();
				continue;
			}
		}
	}
	
	
	public byte [] askData() {
		String newData = "";
		System.out.println("Please enter new Data");
		newData = in.next();
		byte [] data = newData.getBytes();
		return data;
	}

	

	public byte [] askErrMSG() {
		String message = "";
		System.out.println("Please enter new Error message");
		message = in.next();
		byte [] MSG = message.getBytes();
		return MSG;
	}
}
