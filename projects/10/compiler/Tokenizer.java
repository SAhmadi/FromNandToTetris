import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tokenizer {
	public static final String IDENTIFIER = "identifier";
	public static final String INTEGER = "integerConstant";
	public static final String KEYWORD = "keyword";
	public static final String STRING = "stringConstant";
	public static final String SYMBOL = "symbol";

	private final List<String> KEYWORDS = Arrays.asList(
		"class", "constructor","static", "field", "method", "function", 
		"int", "boolean", "char", "void", "var", "let", "do", "if", "else", "while", 
		"return", "true", "false", "null", "this"
	);

	private final List<String> SYMBOLS = Arrays.asList(
		"{", "}", "[", "]", "(", ")", ".", ",", ";", 
		"+", "-", "*", "/", "&", "|", "<", ">", "=", "~"
	);

	public Tokenizer() {}

	public ArrayList<Token> tokenize(String filePath) {
		ArrayList<Token> tokens = new ArrayList<>();

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
			String line = bufferedReader.readLine();
			
			while (line != null) {
				line = line.strip();
				
				List<Token> tokensInLine = tokenizeLine(line);
				if (tokensInLine != null) {
					for (Token t : tokensInLine) {
						tokens.add(t);
					}
				}			
			
				line = bufferedReader.readLine();
			}
		}
		catch (IOException ex) {
			System.out.println("[Error] Tokenizing failed!");
			System.exit(-1);
		}

		// for (Token t : tokens) {
		// 	System.out.println("*** type: " + t.getType() + ", value: " + t.getValue());
		// }

		return tokens;
	}

	
	private List<Token> tokenizeLine(String line) {
		List<Token> tokens = new ArrayList<>();

		if (line.equals("")) return null;
		if (line.startsWith("*") || line.startsWith("//") || line.startsWith("/*"))
			return null;

		line = line.split("//")[0];	// Trim comment if it exists

		String idPattern = "\\w+";
		String intPattern = "\\d+";
		String stringPattern = "\".*\"";
		String keywordPattern = "class|constructor|static|field|method|function|" +
														"int|boolean|char|void|var|let|do|if|else|while|return|true|false" +
														"null|this";
		String symbolPattern = "\\{|\\}|\\[|\\]|\\(|\\)|\\.|,|;|\\+|-|\\*|\\/|&|\\||<|>|=|~";


		String pattern = "(" + idPattern + "|" + intPattern + "|" + stringPattern + "|" + 
										keywordPattern + "|" + symbolPattern + ")";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(line);
		
		while (m.find()) {
			String t = m.group();
			if (t != null && !t.equals("")) 
				tokens.add(getToken(t));
		}

		// for (Token t : tokens) {
		// 	System.out.println("*** type: " + t.getType() + ", value: " + t.getValue());
		// }

		return tokens;
	}


	private Token getToken(String value) {
		if ( KEYWORDS.contains(value) ) 
			return new Token(KEYWORD, value);
		else if ( SYMBOLS.contains(value) )
			return new Token(SYMBOL, value);
		else if ( value.startsWith("\"") && value.endsWith("\"") )
			return new Token(STRING, value.replace("\"", ""));
		else if ( value.matches("^\\d+$") )
			return new Token(INTEGER, value);
		else if ( value.matches("^\\w+$") )
			return new Token(IDENTIFIER, value);
		else {
			System.out.println("[Error] Unable to recognize token '"+ value + "'");
			System.exit(-1);
		}

		return null;
	}

}