import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes .jack lines
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class Tokenizer {
	
	/**
	 * Tokenizes lines of the given file and
	 * stores them inside a list
	 * 
	 * @param filePath The file-path of the file to tokenize
	 * @return List of all tokens
	 */
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

		return tokens;
	}

	
	/**
	 * Tokenize one line
	 * 
	 * @param line The line to tokenize
	 * @return List of all the tokens in that one line
	 */
	private List<Token> tokenizeLine(String line) {
		List<Token> tokens = new ArrayList<>();

		if (line.equals("")) 
			return null;
		if (line.startsWith("*") || line.startsWith("//") || line.startsWith("/*"))
			return null;

		line = line.split("//")[0];	// Trim comment if it exists

		String idPattern = "\\w+";
		String intPattern = "\\d+";
		String stringPattern = "\".*\"";
		String keywordPattern = "class|constructor|static|field|method|function|" 
				+ "int|boolean|char|void|var|let|do|if|else|while|return|true|false" 
				+ "null|this";
		String symbolPattern = "\\{|\\}|\\[|\\]|\\(|\\)|\\.|,|;|\\+|-|\\*|\\/|&|\\||<|>|=|~";

		String pattern = String.format(
			"(%s|%s|%s|%s|%s)", 
			idPattern, intPattern, stringPattern, keywordPattern, symbolPattern
		);
	
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(line);
		
		while (m.find()) {
			String t = m.group();
			if (t != null && !t.equals("")) 
				tokens.add(getToken(t));
		}

		return tokens;
	}


	/**
	 * Get the associated token for a given .jack keyword, symbol, ... 
	 * 
	 * @param value The .jack keyword, symbol, identifier, ...
	 * @return The associated token
	 */
	private Token getToken(String value) {
		if (Tokens.KEYWORDS.contains(value)) 
			return new Token(Tokens.KEYWORD_TYPE, value);
		else if (Tokens.SYMBOLS.contains(value))
			return new Token(Tokens.SYMBOL_TYPE, value);
		else if (value.startsWith("\"") && value.endsWith("\""))
			return new Token(Tokens.STRING_TYPE, value.replace("\"", ""));
		else if ( value.matches("^\\d+$") )
			return new Token(Tokens.INTEGER_TYPE, value);
		else if ( value.matches("^\\w+$") )
			return new Token(Tokens.IDENTIFIER_TYPE, value);
		else {
			System.out.println("[Error] Unable to recognize token '"+ value + "'");
			System.exit(-1);
		}

		return null;
	}
}