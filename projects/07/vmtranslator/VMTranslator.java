package vmtranslator;

import java.util.HashMap;
import java.util.Map;

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
            System.out.println("[Error] Execute: java VMTranslator filename.vm");
            System.exit(-1);
        }
        
        // Remove file-extension
        String inputFilePathWithName = args[0];
        int lastDotIndex = inputFilePathWithName.lastIndexOf(".");
        inputFilePathWithName = inputFilePathWithName.substring(0, lastDotIndex);

        // Remove leading file-path
        Map<String, String> fileComponents = getFileComponents(inputFilePathWithName);

        // Create a parser and parse through file
        Parser parser = new Parser(fileComponents.get("name"), fileComponents.get("path"));
        parser.parse();

        // Create a codeWriter and translate commands
        // then write to file
        CodeWriter codeWriter = new CodeWriter(fileComponents.get("name"), fileComponents.get("path"));
        codeWriter.translate(parser.getCommands());
        codeWriter.writeToFile();
    }


    /**
     * Get the file-name and file-path of the .vm file to translate
     * @param filepathWithName Total file-path with file-name
     * @return File-Components of the .vm file (e.g. name and path)
     */
    private static Map<String, String> getFileComponents(String filepathWithName) {
        Map<String, String> components = new HashMap<>();
        
        // Seperate into leading file-path and only file-name
        int lastIndexOfSlash = filepathWithName.lastIndexOf("/"); 
        if (lastIndexOfSlash > -1) {
            components.put(
                "name", filepathWithName.substring(lastIndexOfSlash + 1, filepathWithName.length())
            );
            components.put(
                "path", filepathWithName.substring(0, lastIndexOfSlash + 1)
            );
        }
        else {
            components.put("name", filepathWithName);
            components.put("path", "./");
        }

        return components;
    }
}