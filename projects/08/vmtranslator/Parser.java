package vmtranslator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vmtranslator.Token.OpType;


/**
 * Parser
 * Parses through the input file and gets all the commands
 * 
 * @author Sirat Ahmadi
 * @version 1.1
 */
public class Parser {
    private String fileName;
    private String filePath;
    private List<Map<String, String>> commands;


    /**
     * Constructor
     * 
     * @param filename Input filename without extension
     */
    public Parser(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.commands = new ArrayList<Map<String, String>>();
    }


    /**
     * Parse the input-file
     * and store commands into list
     */
    public void parse() {
        String p = (filePath.endsWith("/")) ? filePath : filePath + "/";
        String n = (fileName.endsWith(".vm")) ? fileName : fileName + ".vm";
        String inputFilename = p + n;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilename))) {
            String line = bufferedReader.readLine().strip();
            String[] lineSplitted;
            ArrayList<String> tmpCommandParts = new ArrayList<>();

            while (line != null) {
                tmpCommandParts.clear();

                // Skip comments and empty lines
                if (
                    line.startsWith(Token.SINGLE_COMMENT) ||
                    line.equals(Token.EMPTY) || 
                    line.equals(Token.SPACE)
                ) {
                    line = bufferedReader.readLine();
                }
                else {
                    lineSplitted = line.split(Token.SPACE);
                    for (String token : lineSplitted) {
                        // Skip all parts after a comment starts,
                        // and skip all parts that are spaces
                        if (token.equals(Token.SINGLE_COMMENT)) break;
                        if (token.matches(Token.SPACE)) continue;

                        tmpCommandParts.add(token);
                    }

                    if (tmpCommandParts.size() > 0) {
                        // Commands of two different length are allowed
                        // Length 1: add, sub, eq, ...
                        // Length 2: push constant 1, pop static 1, ...
                        // Add all command-parts into the commands-list
                        if (tmpCommandParts.size() > 3) {
                            System.out.println("[Error] Invalid command in .vm file!");
                            System.exit(-1);
                        }
    
                        OpType operationType = getCommandsOperationType(tmpCommandParts.get(0));
                        commands.add(
                            Map.of(
                                "type", operationType.toString(),
                                "operation", tmpCommandParts.get(0),
                                "arg1", (tmpCommandParts.size() > 1) ? tmpCommandParts.get(1) : "",
                                "arg2", (tmpCommandParts.size() > 2) ? tmpCommandParts.get(2) : ""
                            )
                        );
                    }

                    line = bufferedReader.readLine();
                }
            }
        }
        catch (IOException ex) {
            System.out.println("[Error] Parsing failed!");
            System.exit(-1);
        }
    }


    /**
     * Get the Operation-Type of current command
     * 
     * @param commandOperationPart Only the opeartion part of the command
     * @return Operation-Type
     */
    private OpType getCommandsOperationType(String commandOperationPart) {
        if (
            commandOperationPart.equals("add") ||
            commandOperationPart.equals("sub") ||
            commandOperationPart.equals("neg") ||
            commandOperationPart.equals("eq") ||
            commandOperationPart.equals("gt") ||
            commandOperationPart.equals("lt") ||
            commandOperationPart.equals("and") ||
            commandOperationPart.equals("or") ||
            commandOperationPart.equals("not")
        ) return OpType.ARITHMETIC;
        else if (commandOperationPart.equals("pop"))
            return OpType.POP;
        else if (commandOperationPart.equals("push"))
            return OpType.PUSH;
        else if (commandOperationPart.equals("label"))
            return OpType.LABEL;
        else if (commandOperationPart.equals("goto"))
            return OpType.GOTO;
        else if (commandOperationPart.equals("if-goto"))
            return OpType.IF;
        else if (commandOperationPart.equals("function"))
            return OpType.FUNCTION;
        else if (commandOperationPart.equals("call"))
            return OpType.CALL;
        else if (commandOperationPart.equals("return"))
            return OpType.RETURN;
        else {
            System.out.println("[Error] Operation unknown!");
            System.exit(-1);
        }

        return null;
    }


    /* GETTER AND SETTERS */
    public String getFilename() { return fileName;}
    public void setFilename(String value) { fileName = value; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String value) { filePath = value; }

    public List<Map<String, String>> getCommands() { return commands; } 
    public void setCommands(List<Map<String, String>> value) { commands = value; }
}