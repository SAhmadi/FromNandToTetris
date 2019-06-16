import java.util.Arrays;
import java.util.List;


/**
 * Tokens utilities
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class Tokens {
	public static final String IDENTIFIER_TYPE = "identifier";
	public static final String INTEGER_TYPE = "integerConstant";
	public static final String KEYWORD_TYPE = "keyword";
	public static final String STRING_TYPE = "stringConstant";
	public static final String SYMBOL_TYPE = "symbol";

	public static final List<String> KEYWORDS = Arrays.asList(
		"class", "constructor","static", "field", "method", "function", 
		"int", "boolean", "char", "void", "var", "let", "do", "if", "else", "while", 
		"return", "true", "false", "null", "this"
	);

	public static final List<String> SYMBOLS = Arrays.asList(
		"{", "}", "[", "]", "(", ")", ".", ",", ";", 
		"+", "-", "*", "/", "&", "|", "<", ">", "=", "~"
	);

	public static final Token CLASS = new Token(KEYWORD_TYPE, "class");
	public static final Token CONSTRUCTOR = new Token(KEYWORD_TYPE, "constructor");
	public static final Token STATIC = new Token(KEYWORD_TYPE, "static");
	public static final Token FIELD = new Token(KEYWORD_TYPE, "field");
	public static final Token METHOD = new Token(KEYWORD_TYPE, "method");
	public static final Token FUNCTION = new Token(KEYWORD_TYPE, "function");
	public static final Token INT = new Token(KEYWORD_TYPE, "int");
	public static final Token BOOLEAN = new Token(KEYWORD_TYPE, "boolean");
	public static final Token CHAR = new Token(KEYWORD_TYPE, "char");
	public static final Token VOID = new Token(KEYWORD_TYPE, "void");
	public static final Token VAR = new Token(KEYWORD_TYPE, "var");
	public static final Token LET = new Token(KEYWORD_TYPE, "let");
	public static final Token DO = new Token(KEYWORD_TYPE, "do");
	public static final Token IF = new Token(KEYWORD_TYPE, "if");
	public static final Token ELSE = new Token(KEYWORD_TYPE, "else");
	public static final Token WHILE = new Token(KEYWORD_TYPE, "while");
	public static final Token RETURN = new Token(KEYWORD_TYPE, "return");
	public static final Token TRUE = new Token(KEYWORD_TYPE, "true");
	public static final Token FALSE = new Token(KEYWORD_TYPE, "false");
	public static final Token NULL = new Token(KEYWORD_TYPE, "null");
	public static final Token THIS = new Token(KEYWORD_TYPE, "this");

	public static final Token LEFT_CURLY_BRACKET = new Token(SYMBOL_TYPE, "{");
	public static final Token RIGHT_CURLY_BRACKET = new Token(SYMBOL_TYPE, "}");
	public static final Token LEFT_SQUARE_BRACKET = new Token(SYMBOL_TYPE, "[");
	public static final Token RIGHT_SQUARE_BRACKET = new Token(SYMBOL_TYPE, "]");
	public static final Token LEFT_BRACKET = new Token(SYMBOL_TYPE, "(");
	public static final Token RIGHT_BRACKET = new Token(SYMBOL_TYPE, ")");
	public static final Token DOT = new Token(SYMBOL_TYPE, ".");
	public static final Token COMMA = new Token(SYMBOL_TYPE, ",");
	public static final Token SEMICOLON = new Token(SYMBOL_TYPE, ";");
	public static final Token PLUS = new Token(SYMBOL_TYPE, "+");
	public static final Token MINUS = new Token(SYMBOL_TYPE, "-");
	public static final Token MULT = new Token(SYMBOL_TYPE, "*");
	public static final Token DIV = new Token(SYMBOL_TYPE, "/");
	public static final Token AND = new Token(SYMBOL_TYPE, "&");
	public static final Token OR = new Token(SYMBOL_TYPE, "|");
	public static final Token LT = new Token(SYMBOL_TYPE, "<");
	public static final Token GT = new Token(SYMBOL_TYPE, ">");
	public static final Token EQ = new Token(SYMBOL_TYPE, "=");
	public static final Token NOT = new Token(SYMBOL_TYPE, "~");
}