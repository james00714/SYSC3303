/*
 * UI class for error simulator
 * Allow User to modify the packet to create different error codes 
*/
package errorSimulator;

import java.util.Scanner;



public class UI {

	private Scanner scan;
	private String input; 
	private int errorChoice;
	private int transError;
	private int ecChoice;
	private boolean status;
	
	public  UI() {
		scan = new Scanner(System.in);
	}

	 
	
	public void errorMainMenu() {
		
		System.out.println("----------Error Selinputtion----------");
		System.out.println("    0. Normal Operation");
		System.out.println("    1. Transmission Error");
		System.out.println("    2. Error Codes (1-6)");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		status = true;
		while(status) {
			input = scan.next();
			
			if(input.equals("0")) {
				errorChoice = 0;
				status = false;
			}else if (input.equals("1")) {
				errorChoice = 1;
				status = false;
				transmissionError();
			}else if (input.equals("2")) {
				errorChoice = 2;
				status = false;
				System.out.println("Error Code");
			}else if (input.equals("quit")) {
				System.out.println("Thank your for using our program. Goodbye!");
				System.exit(0);
			}else {
				System.out.println("Invalid Input, Try Again!");
				status = true;
			}
		}
	}
	
	public void transmissionError(){
		System.out.println("---------- Transmission Error ----------");
		System.out.println("    1. Lose a packet");
		System.out.println("    2. Delay a packet");
		System.out.println("    3. Duplicate a packet");
		System.out.println("    4. Back to Error main menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		status = true;
		
		while(status){
			input = scan.next();
			
			switch (input) {
				case "1": 
					transError = 1; 
					status = false;
					break;
				case "2": 
					transError = 2;
					status = false;
					break;
				case "3": 
					transError = 3;
					status = false;
					break;
				case "4": 
					status = false;
					errorMainMenu(); 
					break;
				case "quit": 
					System.out.println("Thank your for using our program. Goodbye!");
					System.exit(0);
					break;
				default: 
					System.out.println("Oops, something is wrong"); 
					status = true;
			}
		}
	}
	
	public void errorCodeError1() {
		System.out.println("---------- Error Code Error ----------");
		System.out.println("    1. Invalid Opcode (Error Code 4)");
		System.out.println("    2. Invalid Mode (Error Code 4)");
		System.out.println("    2. Invalid Data Size (Error Code 4)");
		System.out.println("    5. Back to Error Main Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		status = true;
		
		while(status){
			input = scan.next();
			
			switch (input) {
				case "1": 
					ecChoice = 1; 
					status = false;
					break;
				case "2": 
					ecChoice = 2;
					status = false;
					break;
				case "5": 
					status = false;
					errorMainMenu(); 
					break;
				case "quit": 
					System.out.println("Thank your for using our program. Goodbye!");
					System.exit(0);
					break;
				default: 
					System.out.println("Oops, something is wrong"); 
					status = true;
			}
		}
	}
	
	public void errorCodeError2() {
		System.out.println("---------- Error Code Error ----------");
		System.out.println("    1. Invalid Opcode (Error Code 4)");
		System.out.println("    2. Invalid Mode (Error Code 4)");
		System.out.println("    2. Invalid Data Size (Error Code 4)");
		System.out.println("    5. Back to Error Main Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		status = true;
		
		while(status){
			input = scan.next();
			
			switch (input) {
				case "1": 
					ecChoice = 1; 
					status = false;
					break;
				case "2": 
					ecChoice = 2;
					status = false;
					break;
				case "5": 
					status = false;
					errorMainMenu(); 
					break;
				case "quit": 
					System.out.println("Thank your for using our program. Goodbye!");
					System.exit(0);
					break;
				default: 
					System.out.println("Oops, something is wrong"); 
					status = true;
			}
		}
	}
	public void errorCodeError3() {
		System.out.println("---------- Error Code Error ----------");
		System.out.println("    1. Invalid Opcode (Error Code 4)");
		System.out.println("    2. Invalid Mode (Error Code 4)");
		System.out.println("    2. Invalid Data Size (Error Code 4)");
		System.out.println("    5. Back to Error Main Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		status = true;
		
		while(status){
			input = scan.next();
			
			switch (input) {
				case "1": 
					ecChoice = 1; 
					status = false;
					break;
				case "2": 
					ecChoice = 2;
					status = false;
					break;
				case "5": 
					status = false;
					errorMainMenu(); 
					break;
				case "quit": 
					System.out.println("Thank your for using our program. Goodbye!");
					System.exit(0);
					break;
				default: 
					System.out.println("Oops, something is wrong"); 
					status = true;
			}
		}
	}
	
	/*public void packetSelinputtion() {
		System.out.println("---------- Transmission Error ----------");
		System.out.println("    1. RRQ");
		System.out.println("    2. WRQ");
		System.out.println("    3. DATA");
		System.out.println("    4. ACK");
		System.out.println("    5. ERROR" );
		System.out.println("    6. Back to Transmission Menu");
		System.out.println(">>>>>>>> input quit to exit this program");
		
		pc = scan.next();
		
		swiinputh (pc) {
			case "1": 
				packeinputhoice = 1;
				listener.handleNetworkError(transError, packeinputhoice);
				break;
			case "2": 
				packeinputhoice = 2;
				listener.handleNetworkError(transError, packeinputhoice);
				break;
			case "3": 
				packeinputhoice = 3;
				listener.handleNetworkError(transError, packeinputhoice);
				break;
			case "4": 
				packeinputhoice = 4;
				listener.handleNetworkError(transError, packeinputhoice);
				break;
			case "5": 
				packeinputhoice = 5;
				listener.handleNetworkError(transError, packeinputhoice);
				break;
			case "6": 
				packeinputhoice = 6;
				listener.handleNetworkError(transError, packeinputhoice);
				break;
			case "quit": 
				listener.quit(); 
				break;
			default: 
				System.out.println("Oops, something is wrong"); 
				break;
		}
	}*/
	
	/*public byte getPackeinputhoice() {
		return packeinputhoice;
	}*/
	
	public int getErrorType() {
		return errorChoice;
	}
	
	public int getTransError() {
		return transError;
	}
}
