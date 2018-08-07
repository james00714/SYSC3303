/*
 * RequestParser class to break down the request received
 * */
package client;

import java.util.ArrayList;

public class RequestParser {

	private int type, length, blockNum, errorCode;
	private byte[] fileData;
	private String filename, mode, errorMsg;
	private ArrayList<Integer> positionOf0;  // Record the position of byte 0
	private boolean correctFormat = true;

	// Constructor
	public RequestParser() {}
	
	/*
	 * Method to parse a request received
	 * @param	data	packet data received
	 * @param	len		effective length
	 * */
	public void parseRequest(byte[] data, int len) {
		
		length = len;
		type = data[1];
		positionOf0= new ArrayList<Integer>();
		
		if(type < 1 || type > 5) {
			System.out.println("Illegal Opeartion: Wrong Type");	// Illegal opcode
			correctFormat = false;
		}else {
			if(data[0] != 0) {
				System.out.println("Illegal Opeartion: Wrong Format");
				correctFormat = false;
			}else {
				split(data);
			}
		}	
	}

	/*
	 *	Method to parse request and verify
	 *	@param	data	data received
	 * */
	private void split(byte[] data) {
		for(int i = 0; i < length; i++) {
			if(data[i] == 0) {
				positionOf0.add(i);
			}
		}
		
		// RRQ/WRQ
		if(type == 2 || type == 1) {
			
			// number of 0
			if(positionOf0.size() != 3) {
				System.out.println("Illegal Opeartion: Wrong Format");
				correctFormat = false;
			}else {
				if(positionOf0.get(1) == 2) {
					System.out.println("Illegal Opeartion: No Filename");	// empty filename
					correctFormat = false;
					return;
				}
				filename = parseFilename(data, positionOf0.get(1) - 2);
				mode = parseMode(data, positionOf0.get(1) + 1, positionOf0.get(2) - positionOf0.get(1) - 1);
				
				// verify mode
				if(!mode.toLowerCase().equals("netascii") && !mode.toLowerCase().equals("octet")) {
					System.out.println("Illegal Opeartion: Wrong Mode");
					correctFormat = false;
				}
			}
			
		// ACK/DATA
		}else if(type == 3 || type == 4) {	
			if(type == 3) {
				if(length > 516) {
					System.out.println("Illegal Opeartion: Wrong DATA Size");
					correctFormat = false;
				}else {
					fileData = parseFileData(data);
					blockNum = parseBlockNum(data);
				}	
			}else {
				if(length != 4) {
					System.out.println("Illegal Opeartion: Wrong ACK Size");
					correctFormat = false;
				}else {
					blockNum = parseBlockNum(data);
				}		
			}
			
		}else if(type == 5){
			if(data[data.length - 1] != 0 ||
							 data[2] != 0 ||
							  data[3] < 0 ||
							  data[3] > 7) {
				System.out.println("Illegal Opeartion: Wrong Format");
				correctFormat = false;
			}
			errorCode = parseErrorCode(data);
			errorMsg = parseErrorMsg(data);
		}
	}
	
	/*
	 *	Method to parse requested filename
	 *	@param	data	data received
	 *	@param	len		effective length
	 *	@return			filename
	 * */
	private String parseFilename(byte[] data, int len) {
		return new String(data, 2, len);
	}
	

	/*
	 *	Method to parse mode
	 *	@param	data	data received
	 *	@param	len		effective length
	 *	@param	start	start offset
	 *	@return			mode
	 * */
	public String parseMode(byte[] data, int start, int len) {
		return new String(data, start, len);
	}

	/*
	 *	Method to parse block number
	 *	@param	data	data received
	 *	@return			block number
	 * */
	private int parseBlockNum(byte[] data) {
		return parseBlockNum(data[2], data[3]);
	}
	
	/*
	 *	Static method to parse block number
	 *	@param	a	b
	 *	@return		block number
	 * */
	public static int parseBlockNum(int a, int b) {
		if(a < 0) a += 256;
		if(b < 0) b += 256;
		return a * 256 + b;
	}
	
	/*
	 *	Method to parse file data
	 *	@param	data	data received
	 *	@return			file data
	 * */
	private byte[] parseFileData(byte[] data) {
		byte[] fileData = new byte[length - 4];
		for(int i = 4; i < length; i++) {
			fileData[i - 4] = data[i];
		}
		return fileData;
	}
	
	/*
	 *	Method to retrieve ErrorCode
	 *	@param	data	data received
	 *	@return			error code
	 * */
	private byte parseErrorCode(byte[] data) {
		return data[3];
	}
	
	/*
	 *	Method to retrieve ErrorMsg
	 *	@param	data	data received
	 *	@return			error message
	 * */
	private String parseErrorMsg(byte[] data) {
		return new String(data, 4, length - 5);
	}
	
	/*
	 * Public information getters
	 * */
	public int getType() {
		return type;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int getBlockNum() {
		return blockNum;
	}
	
	public byte[] getFileData() {
		return fileData;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public boolean ifCorrect() {
		return correctFormat;
	}
}
