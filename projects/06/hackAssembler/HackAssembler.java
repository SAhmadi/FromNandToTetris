/**
 * HackAssembler
 * Translates Hack-Assembly into Binary-Instructions
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class HackAssembler {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("[Error] Execute: java HackAssembler filename.asm");
			System.exit(-1);
		}

		String filename = args[0];

		// Create Parser and parse the file
		Parser parser = new Parser(filename);
		parser.parse();
	}
}