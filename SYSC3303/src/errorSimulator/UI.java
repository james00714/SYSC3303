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
			System.out.println("\tError Message: " + RP.getErrCode());
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

}
