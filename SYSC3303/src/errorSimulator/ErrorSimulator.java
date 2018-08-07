package errorSimulator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ErrorSimulator {
	
	private Scanner scan;
	private String ic,ec, tc, pc, bc, dc,cc;  
	private ESListener listener;
	private int errorType,errorChoice;
	private int packetChoice, blockChoice, delayChoice;
	private int  errorOpcode, errorPacketSize, errorPacketFormat;
	private String errorMode, errorFilename;
	private InetAddress desIP;

	
	public ErrorSimulator() {
		
		scan = new Scanner(System.in);
		listener = new ESListener();
	}

	public void promptForDestIP() {
		System.out.println("---------- Please Input Destination IP Address ----------");
		System.out.println("    0. Local host (same ip)");
		System.out.println("    1. Other IP address");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		ic = scan.next();
		if(ic.equals("quit")) {
			stop();
			return;
		}
		
		// TODO: type checking 
		
		if(Integer.valueOf(ic) == 0) {
			try {
				desIP = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		else if (Integer.valueOf(ic) == 1) { 
			System.out.println("---------- Please Input Destination IP Address ----------");
			System.out.println(">>>>>>>> input quit to exit this program");
			
			ic = scan.next();
			if(ic.equals("quit")) {
				stop();
				return;
			}
			
			try {
				desIP = InetAddress.getByName(ic);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		
		listener.setDesIP(desIP);
		
		errorMainMenu();
	}
	
	public void errorMainMenu() {
		
		System.out.println("----------Error Selection----------");
		System.out.println("    0. Normal Operation");
		System.out.println("    1. Transmission Error");
		System.out.println("    2. Error Codes (4 or 5)");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		ec = scan.next();
		if(ec.equals("quit")) {
			stop();
			return;
		}
		try {
			errorType = Integer.valueOf(ec);
			switch(errorType) {
				case 0: 
					listener.setErrorType(errorType); 
					listener.confirmChange();
					break;
				case 1: 		
					listener.setErrorType(errorType); 
					transmissionError();
					break;
				case 2: 
					listener.setErrorType(errorType); 
					packetSelection();
					break;
				default: 
					System.out.println("Invalid input, please try again.");
					break;
			}
		}catch(NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
		}
	}
	
	public void transmissionError(){
		System.out.println("---------- Transmission Error ----------");
		System.out.println("    1. Lose a packet");
		System.out.println("    2. Delay a packet");
		System.out.println("    3. Duplicate a packet");
		System.out.println("    4. Back to Error main menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		tc = scan.next();
		if(tc.equals("quit")) {
			stop();
			return;
		}
		try {
			errorChoice = Integer.valueOf(tc);
			if(errorChoice < 0 || errorChoice > 4) {
				System.out.println("Invalid input, please try again."); 
				transmissionError();
			}else {		
				if (errorChoice == 4) return;
				listener.setErrorChoice(errorChoice);
				packetSelection(); 
			}
		}catch(NumberFormatException e) {
				System.out.println("Invalid input, please try again.");
				transmissionError();
		}
	}
	
	public void errorCodeError() {

		if(packetChoice == 1 || packetChoice == 2) {
			System.out.println("---------- Error Code Error ----------");
			System.out.println("    1. Invalid Mode 		 (Error Code 4)");
			System.out.println("    2. Invalid Opcode 		 (Error Code 4)");
			System.out.println("    3. Invalid Filename 	 (Error Code 4)");
			System.out.println("    4. Invalid Packet Size   (Error Code 4)");
			System.out.println("    5. Invalid Packet Format (Error Code 4)");
			System.out.println("    6. Unknown User TID   	 (Error Code 5)");
			System.out.println("    7. Back to Error main menu");
			System.out.println(">>>>>>>> input quit to exit this program");
			
			ec = scan.next();
			if(ec.equals("quit")) {
				stop();
				return;
			}
			
			try {
				errorChoice = Integer.valueOf(ec);
				switch(errorChoice) {
					case 1:
						listener.setErrorChoice(errorChoice);
						askErrorMode();
						break;
					case 2:
						listener.setErrorChoice(errorChoice);
						askErrorOpcode();
						break;
					case 3:
						listener.setErrorChoice(errorChoice);
						askErrorFilename();
						break;
					case 4:
						listener.setErrorChoice(errorChoice);
						askInvalidPacketSize();
						break;
					case 5:
						listener.setErrorChoice(errorChoice);
						askInvalidPacketFormat();
						break;
					case 6:
						listener.setErrorChoice(errorChoice);
						listener.confirmChange();
						break;
					case 7:
						break;
					default:
						System.out.println("Invalid input, please try again.");
						errorCodeError();
						break;
				}
			}catch(NumberFormatException e) {
				System.out.println("Invalid input, please try again.");
				errorCodeError();
			}
		}else if(packetChoice == 3 || packetChoice == 4) {
			System.out.println("---------- Error Code Error ----------");
			System.out.println("    1. Invalid Opcode 		 (Error Code 4)");
			System.out.println("    2. Invalid Packet Format (Error Code 4)");
			System.out.println("    3. Invalid Packet Size   (Error Code 4)");
			System.out.println("    4. Unknown User TID   	 (Error Code 5)");
			System.out.println("    5. Back to Error main menu");
			System.out.println(">>>>>>>> input quit to exit this program");
			
			ec = scan.next();
			if(ec.equals("quit")) {
				stop();
				return;
			}
			
			try {
				errorChoice = Integer.valueOf(ec);
				
				switch(errorChoice) {
				case 1:
					listener.setErrorChoice(errorChoice);
					askErrorOpcode();
					break;
				case 2:
					listener.setErrorChoice(errorChoice);
					listener.confirmChange();
					break;
				case 3:
					listener.setErrorChoice(errorChoice);
					askInvalidPacketSize();
					break;
				case 4:
					listener.setErrorChoice(errorChoice);
					listener.confirmChange();
					break;
				case 5:
					break;
				default:
					System.out.println("Invalid input, please try again.");
					errorCodeError();
					break;
				}
			}catch(NumberFormatException e) {
				System.out.println("Invalid input, please try again.");
				errorCodeError();
			}
			
		}else {
			System.out.println("---------- Error Code Error ----------");
			System.out.println("    1. Invalid Opcode 		 (Error Code 4)");
			System.out.println("    2. Invalid Packet Format (Error Code 4)");
			System.out.println("    3. Unknown User TID   	 (Error Code 5)");
			System.out.println("    4. Back to Error main menu");
			System.out.println(">>>>>>>> input quit to exit this program");
			
			ec = scan.next();
			if(ec.equals("quit")) {
				stop();
				return;
			}
			
			try {
				errorChoice = Integer.valueOf(ec);
				
				switch(errorChoice) {
				case 1:
					listener.setErrorChoice(errorChoice);
					askErrorOpcode();
					break;
				case 2:
					listener.setErrorChoice(errorChoice);
					listener.confirmChange();
					break;
				case 3:
					listener.setErrorChoice(errorChoice);
					listener.confirmChange();
					break;
				case 4:
					break;
				default:
					System.out.println("Invalid input, please try again.");
					errorCodeError();
					break;
				}
			}catch(NumberFormatException e) {
				System.out.println("Invalid input, please try again.");
				errorCodeError();
			}
			
		}
		
	}
	public void delaySelection() {
		System.out.println("---------- Delay Selection ----------");
		System.out.println("    Please enter delay time (ms)...");;
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		dc = scan.next();
		
		if(dc.equals("quit")) {
			stop();
			return;
		}
		try {
			delayChoice = Integer.valueOf(dc);
			if (delayChoice < -1) {
				System.out.println("Invalid input, please try again."); 
				delaySelection();
			}else if (delayChoice == -1){
				return;
			}else {
				listener.setDelayChoice(delayChoice);
				listener.confirmChange();
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
			delaySelection();
		}
		
	}
	
	public void packetSelection() {
		System.out.println("---------- Packet Selection ----------");
		System.out.println("    1. RRQ");
		System.out.println("    2. WRQ");
		System.out.println("    3. DATA");
		System.out.println("    4. ACK");
		System.out.println("    5. ERROR" );
		System.out.println("    6. Back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		pc = scan.next();
		
		if(pc.equals("quit")) {
			stop();
			return;
		}
		
		try {
			packetChoice = Integer.valueOf(pc);
			if (packetChoice < 1 || packetChoice > 6) {
				System.out.println("Invalid input, please try again."); 
				packetSelection();
			}else {
				if(packetChoice == 3 || packetChoice == 4) {
					listener.setPacketChoice(packetChoice);
					blockSelection();
				}else if(packetChoice == 6){
					return;
				}else {
					listener.setPacketChoice(packetChoice);
					if(errorChoice == 2 && errorType == 1) {
						delaySelection();
					}else if(errorType == 2){
						errorCodeError();	
					}else {
						listener.confirmChange();
					}
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
			packetSelection();
		}
	
	}
	
	public void blockSelection() {
		System.out.println("---------- Block Selection ----------");
		System.out.println("    Please enter block number...");;
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		bc = scan.next();
		
		if(bc.equals("quit")) {
			stop();
			return;
		}
		try {
			blockChoice = Integer.valueOf(bc);
			if (blockChoice < -1) {
				System.out.println("Invalid input, please try again."); 
				blockSelection();
			}else if (blockChoice == -1){
				return;
			}else {
				listener.setBlockChoice(blockChoice);
				if(errorChoice == 2 && errorType == 1) {
					delaySelection();
				}else if(errorType ==2){
					errorCodeError();
				}else {
					listener.confirmChange();
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
			blockSelection();
		}	
	}
	
	public void askErrorOpcode() {
		System.out.println("---------- Please Input a new Opcode ----------");
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		cc = scan.next();
		
		if(cc.equals("quit")) {
			stop();
			return;
		}
		
		try {
			errorOpcode = Integer.valueOf(cc);
			if(errorOpcode < -1) {
				System.out.println("Invalid input, please try again.");
				askErrorOpcode();
			}else if(errorOpcode == -1) {
				return;
			}else {
				listener.setErrorOpcode(errorOpcode);
				listener.confirmChange();
			}
		}catch(NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
			askErrorOpcode();
		}
	}
	
	public void askErrorMode() {
		System.out.println("---------- Please Input a new Mode ----------");
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		errorMode = scan.next();
		
		if(errorMode.equals("quit")) {
			stop();
			return;
		}
		
		if(errorMode.equals("-1")) {
			return;
		}
		
		listener.setErrorMode(errorMode);
		listener.confirmChange();
	}
	
	public void askErrorFilename() {
		System.out.println("---------- Please Input a new Filename ----------");
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println("    Enter 0 to delete Filename");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		errorFilename = scan.next();
		
		if(errorFilename.equals("quit")) {
			stop();
			return;
		}
		
		if(errorFilename.equals("-1")) {
			return;
		}
		
		if(errorFilename.equals("0")) {
			errorFilename = "";
		}
		
		listener.setErrorFilename(errorFilename);
		listener.confirmChange();
	}
	
	
	public void askInvalidPacketSize() {
		System.out.println("---------- Please Input a new Packet Size----------");
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		cc = scan.next();
		
		if(cc.equals("quit")) {
			stop();
			return;
		}
		
		try {
			errorPacketSize = Integer.valueOf(cc);
			if(errorPacketSize < -1) {
				System.out.println("Invalid input, please try again.");
				askInvalidPacketSize();
			}else if(errorPacketSize == -1) {
				return;
			}else {
				listener.setErrorPacketSize(errorPacketSize);
				listener.confirmChange();
			}
		}catch(NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
			askInvalidPacketSize();
		}
		
	}
	
	public void askInvalidPacketFormat() {
		System.out.println("---------- Please Input a new Packet Format ----------");
		System.out.println("(This error will modify the zero padding betweem Filename and Mode in RRQ/WRQ)");
		System.out.println("    Enter -1 to go back to Error Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		cc = scan.next();
		
		if(cc.equals("quit")) {
			stop();
			return;
		}
		
		try {
			errorPacketFormat = Integer.valueOf(cc);
			if(errorPacketFormat < -1) {
				System.out.println("Invalid input, please try again.");
				askInvalidPacketFormat();
			}else if(errorPacketFormat == -1) {
				return;
			}else {
				listener.setErrorPacketFormat(errorPacketFormat);
				listener.confirmChange();
			}
		}catch(NumberFormatException e) {
			System.out.println("Invalid input, please try again.");
			askInvalidPacketFormat();
		}
	}
	
	public void stop() {
		listener.quit();
	}
	
	public static void main(String[] args) {
		
		ErrorSimulator es = new ErrorSimulator();
		while(true) {
			System.out.println("in main while loop");
			es.promptForDestIP();
		}
	}

}
