package vmtranslator;

/**
 * VMTranslator
 * Translates VM-Instructions into Hack-Assembly
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class VMTranslator {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("[Error] Execute: java HackAssembler filename.asm");
            System.exit(-1);
        }
        
        // Remove file-extension
        String inputFilename = args[0];
        int lastDotIndex = inputFilename.lastIndexOf(".");
        inputFilename = inputFilename.substring(0, lastDotIndex);

        // Create a parser and parse through file
        Parser parser = new Parser(inputFilename);
        parser.parse();

        // Create a codeWriter and translate commands
        // then write to file
        String outputFilename = inputFilename;
        CodeWriter codeWriter = new CodeWriter(outputFilename);
        codeWriter.translate(parser.getCommands());
        codeWriter.writeToFile();
    }
}