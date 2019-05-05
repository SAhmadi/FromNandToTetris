/**
 * Token
 * Hack-Assembly tokens
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */

import java.util.HashMap;
import java.util.Map;


public class Token {
	public static final String EMPTY = "";
	public static final String SPACE = "\\s+";
	public static final String SINGLE_COMMENT = "//";
	
	public static final String SCREEN = "SCREEN";
	public static final int SCREEN_VALUE = 16384;
	
	public static final String KBD = "KBD";
	public static final int KBD_VALUE = 24576;

	public static final String SP = "SP";
	public static final int SP_VALUE = 0;

	public static final String LCL = "LCL";
	public static final int LCL_VALUE = 1;
	
	public static final String ARG = "ARG";
	public static final int ARG_VALUE = 2;

	public static final String THIS = "THIS";
	public static final int THIS_VALUE = 3;

	public static final String THAT = "THAT";
	public static final int THAT_VALUE = 4;

	public static final Map<String, String> DEST = Map.of(
		"", "000",
		"M", "001",
		"D", "010",
		"MD", "011",
		"A", "100",
		"AM", "101",
		"AD", "110",
		"AMD", "111"
	);
	
	public static final Map<String, String> JUMP = Map.of(
		"", "000",
		"JGT", "001",
		"JEQ", "010",
		"JGE", "011",
		"JLT", "100",
		"JNE", "101",
		"JLE", "110",
		"JMP", "111"
	);

	public static final Map<String, String> COMP_0 = new HashMap<>() {{
		put("0", "0101010");
		put("1", "0111111");
		put("-1", "0111010");
		put("D", "0001100");
		put("A", "0110000");
		put("!D", "0001101");
		put("!A", "0110001");
		put("-D", "0001111");
		put("-A", "0110011");
		put("D+1", "0011111");
		put("A+1", "0110111");
		put("D-1", "0001110");
		put("A-1", "0110010");
		put("D+A", "0000010");
		put("D-A", "0010011");
		put("A-D", "0000111");
		put("D&A", "0000000");
		put("D|A", "0010101");
	}};
	
	public static final Map<String, String> COMP_1 = Map.of(
		"M", "1110000",
		"!M", "1110001",
		"-M", "1110011",
		"M+1", "1110111",
		"M-1", "1110010",
		"D+M", "1000010",
		"D-M", "1010011",
		"M-D", "1000111",
		"D&M", "1000000",
		"D|M", "1010101"
	);
}