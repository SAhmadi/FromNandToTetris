package vmtranslator;

/**
 * Token
 * Store Hack-Assembly tokens
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class Token {
    public static final String SINGLE_COMMENT = "//";
    public static final String EMPTY = "";
    public static final String SPACE = "\\s+";

    public static enum OpType {
        PUSH,
        POP,
        ARITHMETIC
    }

    public static final String LOCAL_CODE = "LCL";
    public static final int LOCAL_BASE = 1;
    
    public static final String ARGUMENT_CODE = "ARG";
    public static final int ARGUMENT_BASE = 2;

    public static final int POINTER_BASE = 3;
    public static final String THIS_CODE = "THIS";
    public static final int THIS_BASE = 3;
    public static final String THAT_CODE = "THAT";
    public static final int THAT_BASE = 4;

    public static final int TEMP_BASE = 5;

    public static final int STATIC_BASE = 16;
}