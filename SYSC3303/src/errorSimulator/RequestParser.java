package errorSimulator;


/*
 * RequestParser class to break down the request received
 * */

import java.util.ArrayList;



public class RequestParser {


	private String[] types = {"", "RRQ", "WRQ", "DATA", "ACK", "ERROR"};
	
	private int type, length, blockNum, errorCode;

	private byte[] fileData;

	private String filename, mode, errorMsg;

	private ArrayList<Integer> positionOf0;  // Record the position of byte 0



	public RequestParser() {}

	

	public void parseRequest(byte[] data, int len) {

		length = len;

		type = data[1];
		positionOf0 = new ArrayList<Integer>();

		split(data);
		
	}



	/*
	 *	Method to parse request 
	 * */

	private void split(byte[] data) {

		for(int i = 0; i < length; i++) {

			if(data[i] == 0) {

				positionOf0.add(i);

			}

		}

		if(type == 1 || type == 2) {

			filename = parseFilename(data, positionOf0.get(1) - 1);

			//mode = parseMode(data,positionOf0.get(1)+1);

		}else if(type == 3 || type == 4) {

			blockNum = parseBlockNum(data);

			if(type == 3) {

				parseFileData(data);

			}

		}else if (type == 5){
			errorCode = data[3];
			errorMsg = new String(data, 4, length-5);

		}

	}

	

	/*
	 *	Method to parse requested filename
	 *	In: request, filename end position
	 *	Out: filename
	 * */

	private String parseFilename(byte[] data, int endFlag) {

		return new String(data, 2, endFlag);

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

		fileData = new byte[length - 4];

		for(int i = 4; i < length; i++) {

			fileData[i - 4] = data[i];

		}

		return fileData;

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

	

	public String getMode() {

		return mode;

	}

	

	public int getBlockNum() {

		return blockNum;

	}

	

	public byte[] getFileData() {

		return fileData;

	}
	
	public String getTypeName() {
		return types[type];
	}

	public int getErrCode() {
		return errorCode;
	}
	public String getErrMsg() {
		return errorMsg;
	}

}