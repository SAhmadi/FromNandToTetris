package vmtranslator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vmtranslator.Token.OpType;

/**
 * CodeWriter
 * Translates command parts into Hack-Assembly
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class CodeWriter {
    private String fileName;
    private String filePath;
    private int comparisonCount;
    List<String> translatedCommands;

    /**
     * Constructor
     * 
     * @param filename Output filename without extension
     */
    public CodeWriter(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.comparisonCount = 0;
        this.translatedCommands = new ArrayList<>();
    }


    /**
     * Translate the list of commands to Hack-Assembly
     * 
     * @param commands List of commands to translate
     */
    public void translate(List<Map<String, String>> commands) {
        String op;
        String segment;
        int index;

        for (Map<String, String> command : commands) {
            op = command.get("operation");
            segment = command.get("segment");
            index = (!command.get("index").equals("")) ? Integer.parseInt(command.get("index")) : -1;

            if (command.get("type").equals(OpType.PUSH.toString())) {
                resolveAddress(segment, index);
                translatePush(segment);
            }
            else if (command.get("type").equals(OpType.POP.toString())) {
                resolveAddress(segment, index);
                translatePop();
            }
            else if (command.get("type").equals(OpType.ARITHMETIC.toString()))
                translateArithmetic(op);
            else {
                System.out.println("[Error] Operation unknown!");
                System.exit(-1);
            }
        }
        
        // Append infinite loop, so it terminates properly
        translatedCommands.add("(END)");
        translatedCommands.add("@END");
        translatedCommands.add("0;JMP");
    }


    /**
     * Resolve the base address of the segment
     * 
     * @param segment The segment of the current command
     * @param index The index of the segment, to operate on
     */
    private void resolveAddress(String segment, int index) {
        if (segment.equals("constant")) translatedCommands.add("@" + index);
        else if (segment.equals("static")) translatedCommands.add("@" + fileName + "." + index);
        else if (segment.equals("pointer")) translatedCommands.add("@R" + (Token.POINTER_BASE+index));
        else if (segment.equals("temp")) translatedCommands.add("@R" + (Token.TEMP_BASE+index));
        else if (
            segment.equals("local") ||
            segment.equals("argument") ||
            segment.equals("this") ||
            segment.equals("that")
        ) {
            translatedCommands.add("@" + index);
            translatedCommands.add("D=A");

            if (segment.equals("local")) translatedCommands.add("@" + Token.LOCAL_CODE);
            else if (segment.equals("argument")) translatedCommands.add("@" + Token.ARGUMENT_CODE);
            else if (segment.equals("this")) translatedCommands.add("@" + Token.THIS_CODE);
            else translatedCommands.add("@" + Token.THAT_CODE);
            
            translatedCommands.add("A=M");
            translatedCommands.add("A=A+D");
        }
        else {
            System.out.println("[Error] Segment unknown!");
            System.exit(-1);
        }
    }


    /**
     * Translation for push command
     * 
     * @param segment The segment where push command operates on
     */
    private void translatePush(String segment) {
        if (segment.equals("constant")) translatedCommands.add("D=A");
        else translatedCommands.add("D=M");
        pushDToStack();
    }


    /**
     * Translation for pop command
     */
    private void translatePop() {
        translatedCommands.add("D=A");
        translatedCommands.add("@R13");
        translatedCommands.add("M=D");
        popStackToD();
        translatedCommands.add("@R13");
        translatedCommands.add("A=M");
        translatedCommands.add("M=D");
    }


    /**
     * Translation for arithmetic command
     * 
     * @param op The arithmetic operation (e.g. add, sub, ...)
     */
    private void translateArithmetic(String op) {
        // Pop first argument for non binary operations, store in D
        if (!op.equals("neg") && !op.equals("not")) popStackToD();

        decrementSP();
        setAToStack();

        if (op.equals("add")) translatedCommands.add("M=M+D");
        else if (op.equals("sub")) translatedCommands.add("M=M-D");
        else if (op.equals("neg")) translatedCommands.add("M=-M");

        else if (op.equals("and")) translatedCommands.add("M=M&D");
        else if (op.equals("or")) translatedCommands.add("M=M|D");
        else if (op.equals("not")) translatedCommands.add("M=!M");
        
        else if (op.equals("eq") || op.equals("gt") || op.equals("lt")) {
            // if (x==y) <=> if (x-y == 0)
            // if (x>y) <=> if (x-y > 0)
            // if (x<y) <=> if (x-y < 0)
            translatedCommands.add("D=M-D");
            translatedCommands.add("@COMPARISON_" + comparisonCount + "_WAS_TRUE");

            // if true jump to COMPARISON_x_WAS_TRUE
            if (op.equals("eq")) translatedCommands.add("D;JEQ");
            else if (op.equals("gt")) translatedCommands.add("D;JGT");
            else translatedCommands.add("D;JLT");

            // else jump to COMPARISON_x_WAS_FALSE
            setAToStack();
            translatedCommands.add("M=0");
            translatedCommands.add("@COMPARISON_" + comparisonCount + "_WAS_FALSE");
            translatedCommands.add("0;JMP");

            // write label: (COMPARISON_x_WAS_TRUE)
            translatedCommands.add("(COMPARISON_" + comparisonCount + "_WAS_TRUE)");
            setAToStack();
            translatedCommands.add("M=-1");

            // write label: (COMPARISON_x_WAS_FALSE)
            translatedCommands.add("(COMPARISON_" + comparisonCount + "_WAS_FALSE)");

            comparisonCount++;
        }
        else {
            System.out.println("[Error] Arithmetic operation unknown!");
            System.exit(-1);
        }

        incrementSP();
    }

    
    /**
     * Dereference the Stack-Pointer and store in A 
     */
    private void setAToStack() {
        // *SP
        translatedCommands.add("@SP");
        translatedCommands.add("A=M");
    }


    /**
     * Increment the Stack-Pointer
     */
    private void incrementSP() {
        // SP++
        translatedCommands.add("@SP");
        translatedCommands.add("M=M+1");
    }


    /**
     * Decrement the Stack-Pointer
     */
    private void decrementSP() {
        // SP--
        translatedCommands.add("@SP");
        translatedCommands.add("M=M-1");
    }


    /**
     * Push the value inside D onto the Stack
     * and increment the Stack-Pointer
     */
    private void pushDToStack() {
        // *SP=D
        // SP++
        setAToStack();
        translatedCommands.add("M=D");
        incrementSP();
    } 


    /**
     * Decremnt the Stack-Pointer and 
     * pop the value ontop of the Stack into D
     */
    private void popStackToD() {
        // SP--
        // D=*SP
        decrementSP();
        setAToStack();
        translatedCommands.add("D=M");
    }


    /**
     * * Write translated commands to output-file
     */
    public void writeToFile() {
        // Set .asm file-extension
        String outputFilename = filePath + fileName + ".asm";

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilename))) {
            for (String command : translatedCommands) {
                bufferedWriter.append(command.trim());
                bufferedWriter.append(System.lineSeparator());
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }


    /* GETTERS AND SETTERS */
    public String getFilename() { return fileName; }
    public void setFilename(String value) { fileName = value; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String value) { filePath = value; }

    public int getComparisonCount() { return comparisonCount; }
    public void setComparisonCount(int value) { comparisonCount = value; }

    public List<String> getTranslatedCommands() { return translatedCommands; }
    public void setTranslatedCommands(List<String> value) { translatedCommands = value; } 
}