package vmtranslator;

import java.io.BufferedWriter;
import java.io.File;
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
 * @version 1.1
 */
public class CodeWriter {
    private String fileName;
    private String filePath;
    private String fileNameForStatics;
    private String functionName;
    
    private int returnCounter;
    private int comparisonCount;

    List<String> translatedCommands;


    /**
     * Constructor
     * 
     * @param filename Output filename without extension
     */
    public CodeWriter(String fileName, String filePath, boolean addSysInit) {
        this.fileName = fileName;
        this.filePath = filePath;
        
        this.functionName = fileName;           // Will be overridden
        this.fileNameForStatics = functionName; // Will be overridden

        this.comparisonCount = 0;
        this.returnCounter = 1;
        
        this.translatedCommands = new ArrayList<>();

        if (addSysInit) writeSysInit();
    }


    /**
     * Call Sys.init at the beginning of the program
     */
    private void writeSysInit() {
        // SP = 256
        translatedCommands.add("@256");
        translatedCommands.add("D=A");
        translatedCommands.add("@SP");
        translatedCommands.add("M=D");

        // call Sys.init
        functionName = "Sys.init";
        fileNameForStatics = "Sys";
        translateCall(functionName, 0);
    }


    /**
     * Translate the list of commands to Hack-Assembly
     * 
     * @param commands List of commands to translate
     */
    public void translate(List<Map<String, String>> commands) {
        String op;
        String arg1;
        int arg2;

        for (Map<String, String> command : commands) {
            op = command.get("operation");
            arg1 = command.get("arg1");
            arg2 = (!command.get("arg2").equals("")) ? 
                Integer.parseInt(command.get("arg2")) : -1;

            if (command.get("type").equals(OpType.PUSH.toString())) {
                resolveAddress(arg1, arg2);
                translatePush(arg1);
            }
            else if (command.get("type").equals(OpType.POP.toString())) {
                resolveAddress(arg1, arg2);
                translatePop();
            }
            else if (command.get("type").equals(OpType.ARITHMETIC.toString()))
                translateArithmetic(op);
            else if (command.get("type").equals(OpType.LABEL.toString()))
                translateLabel(arg1);
            else if (command.get("type").equals(OpType.GOTO.toString()))
                translateGoto(arg1);
            else if (command.get("type").equals(OpType.IF.toString()))
                translateIf(arg1);
            else if (command.get("type").equals(OpType.FUNCTION.toString()))
                translateFunction(arg1, arg2);
            else if (command.get("type").equals(OpType.CALL.toString()))
                translateCall(arg1, arg2);
            else if (command.get("type").equals(OpType.RETURN.toString()))
                translateReturn();
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
     * Resolve the base address of the arg1
     * 
     * @param arg1 The arg1 of the current command
     * @param arg2 The arg2 of the arg1, to operate on
     */
    private void resolveAddress(String arg1, int arg2) {
        if (arg1.equals("constant")) translatedCommands.add("@" + arg2);
        else if (arg1.equals("static")) translatedCommands.add("@" + fileNameForStatics + "." + arg2);
        else if (arg1.equals("pointer")) translatedCommands.add("@R" + (Token.POINTER_BASE+arg2));
        else if (arg1.equals("temp")) translatedCommands.add("@R" + (Token.TEMP_BASE+arg2));
        else if (
            arg1.equals("local") ||
            arg1.equals("argument") ||
            arg1.equals("this") ||
            arg1.equals("that")
        ) {
            translatedCommands.add("@" + arg2);
            translatedCommands.add("D=A");

            if (arg1.equals("local")) translatedCommands.add("@" + Token.LOCAL_CODE);
            else if (arg1.equals("argument")) translatedCommands.add("@" + Token.ARGUMENT_CODE);
            else if (arg1.equals("this")) translatedCommands.add("@" + Token.THIS_CODE);
            else translatedCommands.add("@" + Token.THAT_CODE);
            
            translatedCommands.add("A=M");
            translatedCommands.add("A=A+D");
        }
        else {
            System.out.println("[Error] First argument unknown!");
            System.exit(-1);
        }
    }


    /**
     * Translation for push command
     * 
     * @param arg1 The first argument where the push-command operates on
     */
    private void translatePush(String arg1) {
        if (arg1.equals("constant")) 
            translatedCommands.add("D=A");
        else 
            translatedCommands.add("D=M");
        pushDToStack();
    }


    /**
     * Translation for pop-command
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
        if (!op.equals("neg") && !op.equals("not")) 
            popStackToD();

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
     * Translate label-command
     * 
     * @param arg1 The label name
     */
    private void translateLabel(String arg1) {
        translatedCommands.add("(" + functionName + "$" + arg1 + ")");
    }


    /**
     * Translate goto-command
     * 
     * @param arg1 The label to jump to
     */
    private void translateGoto(String arg1) {
        translatedCommands.add("@" + functionName + "$" + arg1);
        translatedCommands.add("0;JMP");
    }


    /**
     * Translate if-command
     * 
     * @param arg1 Boolean operation to perfom 
     */
    private void translateIf(String arg1) {
        popStackToD();
        translatedCommands.add("@" + functionName + "$" + arg1);
        translatedCommands.add("D;JNE");
    }


    /**
     * Translate function-command
     * 
     * @param arg1 The function name
     * @param arg2 The function parameter count
     */
    private void translateFunction(String arg1, int arg2) {
        functionName = arg1;
        fileNameForStatics = functionName.split("\\.")[0];

        translatedCommands.add("(" + arg1 + ")");

        for (int i = 0; i < arg2; i++) {
            resolveAddress("constant", 0);
            translatePush("constant");
        }
    }


    /**
     * Translate call-command
     * 
     * @param arg1 The function name being called
     * @param arg2 The function argument count
     */
    private void translateCall(String arg1, int arg2) {
        String retAddrLabel = "FUNC_RETURN_"+(returnCounter++);

        // Push retAddrLabel
        translatedCommands.add("@"+retAddrLabel);
        translatedCommands.add("D=A");
        // pushDToStack();
        translatedCommands.add("@SP");
        translatedCommands.add("A=M");
        translatedCommands.add("M=D");
        translatedCommands.add("@SP");
        translatedCommands.add("M=M+1");

        // Store prev LCL, ARG, THIS, THAT
        translatedCommands.add("@"+Token.LOCAL_CODE);
        translatePush("local");
        translatedCommands.add("@"+Token.ARGUMENT_CODE);
        translatePush("argument");
        translatedCommands.add("@"+Token.THIS_CODE);
        translatePush("this");
        translatedCommands.add("@"+Token.THAT_CODE);
        translatePush("that");

        // ARG = SP - 5 - nArgs
        translatedCommands.add("@SP");
        translatedCommands.add("D=M");
        translatedCommands.add("@"+arg2);
        translatedCommands.add("D=D-A");
        translatedCommands.add("@5");
        translatedCommands.add("D=D-A");
        translatedCommands.add("@"+Token.ARGUMENT_CODE);
        //translatedCommands.add("@ARG");
        translatedCommands.add("M=D");

        // Update LCL
        translatedCommands.add("@SP");
        translatedCommands.add("D=M");
        translatedCommands.add("@"+Token.LOCAL_CODE);
        //translatedCommands.add("@LCL");
        translatedCommands.add("M=D");

        // Write GOTO
        translatedCommands.add("@"+arg1);
        translatedCommands.add("0;JMP");

        // Write return label
        translatedCommands.add("("+ retAddrLabel + ")");
    }


    /**
     * Translate return-command
     */
    private void translateReturn() {
        // endFrame = LCL
        translatedCommands.add("@"+Token.LOCAL_CODE);
        translatedCommands.add("D=M");
        translatedCommands.add("@END_FRAME");
        translatedCommands.add("M=D");

        // retAddr = *(endFrame - 5)
        translatedCommands.add("@5");
        translatedCommands.add("A=D-A");   
        translatedCommands.add("D=M");
        translatedCommands.add("@RET_ADDR");
        translatedCommands.add("M=D");

        // *ARG = pop()
        translatedCommands.add("@SP");
        translatedCommands.add("A=M-1");
        translatedCommands.add("D=M");
        translatedCommands.add("@"+Token.ARGUMENT_CODE);
        translatedCommands.add("A=M");
        translatedCommands.add("M=D");

        // SP = ARG + 1
        translatedCommands.add("@"+Token.ARGUMENT_CODE);
        translatedCommands.add("D=M+1");
        translatedCommands.add("@SP");
        translatedCommands.add("M=D");

        // Restore THAT, THIS, ARG, LCL
        restoreForReturn("THAT", 1);
        restoreForReturn("THIS", 2);
        restoreForReturn("ARG", 3);
        restoreForReturn("LCL", 4);

        // GOTO retAddr
        translatedCommands.add("@RET_ADDR");
        translatedCommands.add("A=M");
        translatedCommands.add("0;JMP");
    }
    

    /**
     * Restore values for the return
     * @param segment [THIS | THAT | ARG | LCL] segment
     * @param offset The offset of the segment
     */
    private void restoreForReturn(String segment, int offset) {
        translatedCommands.add("@" + offset);
        translatedCommands.add("D=A");
        translatedCommands.add("@END_FRAME");
        translatedCommands.add("A=M-D");
        translatedCommands.add("D=M");
        translatedCommands.add("@" + segment);
        translatedCommands.add("M=D");
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
        String p = (filePath.endsWith(File.separator)) ? filePath : filePath + File.separator;
        String n = (fileName.endsWith(".asm")) ? fileName : fileName + ".asm";
        String outputFilename = p + n;

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilename))) {
            for (String command : translatedCommands) {
                bufferedWriter.append(command.trim());
                bufferedWriter.append(System.lineSeparator());
            }
        }
        catch (IOException ex) { 
            System.out.println("[Error] Writing to .asm file failed.");
            System.exit(-1);
        }
    }


    /* GETTERS AND SETTERS */
    public String getFilename() { return fileName; }
    public void setFilename(String value) { fileName = value; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String value) { filePath = value; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String value) { functionName = value; }

    public String getFileNameForStatics() { return fileNameForStatics; }
    public void setFileNameForStatics(String value) { fileNameForStatics = value; }

    public int getReturnCounter() { return returnCounter; }
    public void setReturnCounter(int value) { returnCounter = value; }

    public int getComparisonCount() { return comparisonCount; }
    public void setComparisonCount(int value) { comparisonCount = value; }

    public List<String> getTranslatedCommands() { return translatedCommands; }
    public void setTranslatedCommands(List<String> value) { translatedCommands = value; } 
}