import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PhysLayerClient {

	static Socket socket = null;
	static InputStream in = null;
	static OutputStream out = null;
	
	static double baseline = 0;
	static int[] encodedBits = new int[320];
	static byte[] decodedBytes = new byte[32];
	
	public static void main(String[] args){
		
		//establish connection to server
		try {
			socket = new Socket("codebank.xyz", 38002);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Catch preamble
		for(int i = 0; i < 64; i++){
			try {
				baseline += in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		baseline /= 64;
		System.out.println("Baseline: " + baseline);
		
		//Catch bits sent and decode nrzi
		int last = 0;
		int current = 0;
		for(int i = 0; i < encodedBits.length; i++){
			int input = 0;
			try {
				input = in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(input < baseline) current = 0;
			else current = 1;
			if(last != current) encodedBits[i] = 1;
			else encodedBits[i] = 0;
			last = current;
		}
		
		//5b to 4b translation
		for(int i = 0; i < decodedBytes.length; i++){
			int upper = fiveBitToFourBit(create5BitNumber(i * 5 * 2)) << 4;
			int lower = fiveBitToFourBit(create5BitNumber(i * 5 * 2 + 5));
			decodedBytes[i] = (byte) (upper + lower);
			System.out.printf("%02x ",decodedBytes[i]);
		}
		System.out.println();
		
		//Send to server
		for(int i = 0; i < decodedBytes.length; i++){
			try {
				out.write(decodedBytes[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//read result
		try {
			System.out.println("Server Result: " + in.read());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//close connection
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int create5BitNumber(int startIndex){
		int result = 0;
		result += encodedBits[startIndex] << 4;
		result += encodedBits[startIndex + 1] << 3;
		result += encodedBits[startIndex + 2] << 2;
		result += encodedBits[startIndex + 3] << 1;
		result += encodedBits[startIndex + 4];
		return result;
	}
	
	public static byte fiveBitToFourBit(int fiveBit){
		switch(fiveBit){
		case 30: return 0;
		case 9 : return 1;
		case 20: return 2;
		case 21: return 3;
		case 10: return 4;
		case 11: return 5;
		case 14: return 6;
		case 15: return 7;
		case 18: return 8;
		case 19: return 9;
		case 22: return 10;
		case 23: return 11;
		case 26: return 12;
		case 27: return 13;
		case 28: return 14;
		case 29: return 15;
		default: return 0;
		}
	}
}
