/*
 * RequestParser class to break down the request received
 * */
package server;

import java.util.ArrayList;

public class RequestParser {

	private int type, length, blockNum, errorCode;
	private byte[] fileData;
	private String filename, errorMsg;
	private ArrayList<Integer> positionOf0;  // Record the position of byte 0
	private boolean correctFormat = true;

	public RequestParser() {}
	
	public void parseRequest(byte[] data, int len) {
		length = len;
		type = data[1];
		positionOf0= new ArrayList<Integer>();
		if(type < 1 || type > 5) {
			System.out.println("Illegal Opeartion: Wrong Type");
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
	 * */
	private void split(byte[] data) {
		for(int i = 0; i < length; i++) {
			if(data[i] == 0) {
				positionOf0.add(i);
			}
		}
		if(type == 2 || type == 1) {
			if(positionOf0.size() != 2) {
				System.out.println("Illegal Opeartion: Wrong Format");
				correctFormat = false;
			}else {
				filename = parseFilename(data, positionOf0.get(1) - 2);
			}
		}else if(type == 3 || type == 4) {
			blockNum = parseBlockNum(data);
			if(type == 3) {
				if(length > 516) {
					System.out.println("Illegal Opeartion: Wrong DATA Size");
					correctFormat = false;
				}else {
					fileData = parseFileData(data);
				}	
			}else {
				if(length != 4) {
					System.out.println("Illegal Opeartion: Wrong ACK Size");
					correctFormat = false;
				}
			}
		}else if(type == 5){
			errorCode = parseErrorCode(data);
			errorMsg = parseErrorMsg(data);
		}
	}
	
	/*
	 *	Method to parse requested filename
	 *	In: request, filename end position
	 *	Out: filename
	 * */
	private String parseFilename(byte[] data, int len) {
		return new String(data, 2, len);
	}

	/*
	 *	Method to parse block number
	 *	In: request
	 *	Out: block number
	 * */
	private int parseBlockNum(byte[] data) {
		int left = data[2];
		int right = data[3];
		if(left < 0) left += 256;
		if(right < 0) right += 256;
		return left * 256 + right;
	}
	
	/*
	 *	Method to retrieve file data
	 *	In: request
	 *	Out: file data
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
	 *	In: request
	 *	Out: error code
	 * */
	private byte parseErrorCode(byte[] data) {
		return data[3];
	}
	
	/*
	 *	Method to retrieve error message
	 *	In: request
	 *	Out: error message
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
