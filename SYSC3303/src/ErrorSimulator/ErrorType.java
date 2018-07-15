package ErrorSimulator;

public class ErrorType {
	
	public ErrorType() {
		
	}
	
	public void printErrorMenu() {
		System.out.println("------------------------Error Types Menu----------------------");
		System.out.println("\t >>Please Input number 0-7 to select");
		System.out.println("\t0. Not defined, see error message (if any).");
		System.out.println("\t1. File not found.");
		System.out.println("\t2. Access violation.");
		System.out.println("\t3. Disk full or allocation exceeded.");
		System.out.println("\t4. Illegal TFTP operation.");
		System.out.println("\t5. Unknown transfer ID."); 
		System.out.println("\t6. File already exists."); 
		System.out.println("\t7. No such user.");
		System.out.println("---------------------------------------------------------------");
	}
	
	public void printIllegalOperation() {
		System.out.println("-------------------Illegal TFTP Operations Types Menu------------------");
		System.out.println("\t >>Please Input number 0-2 to select");
		System.out.println("\t0. Invalid Mode.");
		System.out.println("\t1. Invalid TFTP Opcode on RRQ or WRQ.");
		System.out.println("\t2. Invalid Filename.");
		System.out.println("-----------------------------------------------------------------------");

	}
	
}