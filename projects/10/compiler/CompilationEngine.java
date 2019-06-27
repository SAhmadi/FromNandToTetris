import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Compile tokens to xml-tags and write to file
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class CompilationEngine {
	private Stack<Token> tokenStack;
	private List<String> xmlTagList;
	private int indentation;


	/**
	 * Initialize the tokenStack and xml lines
	 */
	public CompilationEngine() {
		this.tokenStack = new Stack<>();
		this.xmlTagList = new ArrayList<>();
		this.indentation = 0;
	}


	/**
	 * Increase the indentation by 2 spaces
	 */
	private void indent() {
		indentation += 2;
	}


	/**
	 * Decrease the indentation by 2 spaces
	 */
	private void deindent() {
		indentation -= 2;
	}


	/**
	 * Compile the list of tokens into
	 * their corresponding xml-tag and
	 * write results to file
	 * 
	 * @param tokens The list of tokes to convert
	 * @param filePath The file to write the xml-tags to
	 */
	public void compileToFile(List<Token> tokens, String filePath) {
		// Store tokens in Stack, for easier handling
		tokenStack.clear();
		xmlTagList.clear();
		Collections.reverse(tokens);
		tokenStack.addAll(tokens);

		// Start compiling the file 
		try { 
			compileClass(); 
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(-1);
		}

		// Write xml-tags to file
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath))) {
			for (String l : xmlTagList) {
				// System.out.println(filePath + ": " + l);
				bufferedWriter.append(l);
				bufferedWriter.append(System.lineSeparator());
			}
		}
		catch (IOException ex) { 
			System.out.println("[Error] Failed to write to .xml file.");
			System.exit(-1);
		}
	}


	/**
	 * Adds xml-tag to xml-tag List
	 * 
	 * @param tag The xml tag to add to the list
	 */
	private void writeTag(String tag) {
		String spaces = "";
		for (int i = 0; i < indentation; i++) 
			spaces += " ";

		xmlTagList.add(spaces + tag);
	}


	/**
	 * Create a xml-tag from the Token values
	 * and add to xml-tag List
	 * 
	 * @param t The token to add to the xml-tag list
	 */
	private void writeToken(Token t) {
		String tokenType = t.getType();
		String tokenValue = t.getValue();

		writeTag(
			String.format("<%s> %s </%s>", tokenType, tokenValue, tokenType)
		);
	}


	/**
	 * Make sure that both token are the same
	 * else throw Exception
	 * 
	 * @param a First token
	 * @param b Second token
	 * @throws Exception
	 */
	private void throwOnNotEqual(Token a, Token b) throws Exception {
		if ( !a.equals(b) ) throw new Exception("Syntax error!");
	}


	/**
	 * Make sure that the token {@code a} has the type {@code type}
	 * else throw Exception
	 * 
	 * @param a The token
	 * @param type The type the token should have
	 * @throws Exception
	 */
	private void throwOnNotEqual(Token a, String type) throws Exception {
		if ( !a.getType().equals(type) ) throw new Exception("Syntax error!");
	}


	/**
	 * Compile the token on top of the {@code tokenStack},
	 * before compilation check token
	 * 
	 * @param compareToToken Token to compare with the token on top of {@code tokenStack}
	 * @return The previous top most token on {@code tokenStack}
	 * @throws Exception
	 */
	private Token compileToken(Token compareToTokens) throws Exception {
		Token t = tokenStack.peek();
		throwOnNotEqual(t, compareToTokens);
		writeToken(tokenStack.pop());
		return t;
	}


	/**
	 * Compile token on top of the {@code tokenStack},
	 * before compilation check token type
	 * 
	 * @param comapreToType Token type to compare with the token on top of {@code tokenStack}
	 * @return The previous top most token on {@code tokenStack}
	 * @throws Exception
	 */
	private Token compileToken(String comapreToType) throws Exception {
		Token t = tokenStack.peek();
		throwOnNotEqual(t, comapreToType);
		writeToken(tokenStack.pop());
		return t;
	}


	/**
	 * Compile the line where class-keyword occurs,
	 * e.g.: class Foo {...}
	 * 
	 * @throws Exception
	 */
	private void compileClass() throws Exception {
		throwOnNotEqual(tokenStack.peek(), Tokens.CLASS);
		writeTag("<class>");

		indent();
		writeToken(tokenStack.pop());
		
		compileToken(Tokens.IDENTIFIER_TYPE);

		compileToken(Tokens.LEFT_CURLY_BRACKET);

		compileClassVarDec();	// Compile all class vars

		compileSubroutine();	// Compile subroutine

		compileToken(Tokens.RIGHT_CURLY_BRACKET);
		
		deindent();
		writeTag("</class>");
	}


	/**
	 * Compile class variables at the top of the class declaration,
	 * e.g.: field int x, y, z;
	 * 
	 * @throws Exception
	 */
	private void compileClassVarDec() throws Exception {
		Token t = tokenStack.peek();
		if (!t.equals(Tokens.STATIC) && !t.equals(Tokens.FIELD)) 
			return;

		writeTag("<classVarDec>");
		
		indent();
		writeToken(tokenStack.pop());

		compileVarType(); // Compile int x or Foo y or ...

		// Compile possible multi declarations: field int x, y, z, ...
		t = tokenStack.peek();
		while ( t.equals(Tokens.COMMA) ) {
			writeToken(tokenStack.pop()); // Write ,
			compileToken(Tokens.IDENTIFIER_TYPE);
			t = tokenStack.peek();
		}

		compileToken(Tokens.SEMICOLON);
		
		deindent();
		writeTag("</classVarDec>");

		compileClassVarDec(); // Move to next class member
	}


	/**
	 * Compile variable type with its identifier,
	 * e.g.: int x
	 * 
	 * @throws Exception
	 */
	private void compileVarType() throws Exception {
		Token t = tokenStack.peek();
		if (!t.getType().equals(Tokens.IDENTIFIER_TYPE) && !t.getType().equals(Tokens.KEYWORD_TYPE))
			throw new Exception("Syntax error!");
		writeToken(tokenStack.pop());			// Write int or Foo or ...

		compileToken(Tokens.IDENTIFIER_TYPE);	// Write x or y or ...
	}


	/**
	 * Compile subroutine,
	 * e.g.: function int fooFunc(int a, int b) {...}
	 * 
	 * @throws Exception
	 */
	private void compileSubroutine() throws Exception {
		Token t = tokenStack.peek();
		while (t.equals(Tokens.CONSTRUCTOR) || t.equals(Tokens.FUNCTION) || t.equals(Tokens.METHOD)) {
			writeTag("<subroutineDec>");		// Write starting <subroutineDec>
			indent();
			writeToken(tokenStack.pop());		// Write function or method or constructor

			t = tokenStack.peek();
			if (!t.getType().equals(Tokens.IDENTIFIER_TYPE) && !t.getType().equals(Tokens.KEYWORD_TYPE))
				throw new Exception("Syntax error!");
			writeToken(tokenStack.pop());			// Write return type of subroutine

			compileToken(Tokens.IDENTIFIER_TYPE);	// Write name of subroutine

			compileToken(Tokens.LEFT_BRACKET);

			compileParams();

			compileToken(Tokens.RIGHT_BRACKET);

			writeTag("<subroutineBody>");
			indent();

			compileToken(Tokens.LEFT_CURLY_BRACKET);

			compileVarDec();						// Compile var declarations
			compileStatements();					// Compile statements

			compileToken(Tokens.RIGHT_CURLY_BRACKET);

			deindent();
			writeTag("</subroutineBody>");

			deindent();
			writeTag("</subroutineDec>");

			t = tokenStack.peek();
		}
	}


	/**
	 * Compile parameter list of subroutine-call
	 * e.g.: ...(Foo f, int i)...
	 * @throws Exception
	 */
	private void compileParams() throws Exception {
		writeTag("<parameterList>");
		indent();

		// Write all Parameters
		Token t = tokenStack.peek();	
		if (
			t.getType().equals(Tokens.IDENTIFIER_TYPE) ||
			t.getType().equals(Tokens.KEYWORD_TYPE)
		) {
			writeToken(tokenStack.pop());			// Write int or Foo ...

			compileToken(Tokens.IDENTIFIER_TYPE);	// Write x or y ...

			t = tokenStack.peek();
			while ( t.equals(Tokens.COMMA) ) {
				writeToken(tokenStack.pop());
				compileVarType();
				t = tokenStack.peek();
			}
		}

		deindent();
		writeTag("</parameterList>");
	}


	/**
	 * Compile a subroutine call,
	 * e.g.: Foo.fooFunc(...) or fooFunc(...)
	 * 
	 * @throws Exception
	 */
	private void compileSubroutineCall() throws Exception {
		compileToken(Tokens.IDENTIFIER_TYPE);

		Token t = tokenStack.peek();
		if ( t.equals(Tokens.DOT) ) {
			writeToken(tokenStack.pop());
			compileToken(Tokens.IDENTIFIER_TYPE);
		}

		compileToken(Tokens.LEFT_BRACKET);

		compileExpressionList();

		compileToken(Tokens.RIGHT_BRACKET);
	}


	/**
	 * Compile variable declaration,
	 * e.g.: Foo f, g;
	 * 
	 * @throws Exception
	 */
	private void compileVarDec() throws Exception {
		if (!tokenStack.peek().equals(Tokens.VAR)) 
			return;

		writeTag("<varDec>");
		indent();
		writeToken(tokenStack.pop());		// write var
		
		compileVarType();					// Compile var type and identifier

		// check for multi declaration e.g. int x, y, z, ...
		Token t = tokenStack.peek();
		while ( t.equals(Tokens.COMMA) ) {
			writeToken(tokenStack.pop());	// Write ,
			compileToken(Tokens.IDENTIFIER_TYPE);
			t = tokenStack.peek();
		}

		compileToken(Tokens.SEMICOLON);
		
		deindent();
		writeTag("</varDec>");
		
		compileVarDec();
	}


	/**
	 * Compile statement,
	 * e.g.: let ... or do ...
	 * 
	 * @throws Exception
	 */
	private void compileStatements() throws Exception {
		writeTag("<statements>");
		indent();

		Token t = tokenStack.peek();
		while (
			t.equals(Tokens.LET) ||
			t.equals(Tokens.DO) ||
			t.equals(Tokens.IF) ||
			t.equals(Tokens.WHILE) ||
			t.equals(Tokens.RETURN)
		) {
			if (t.equals(Tokens.LET)) 			compileLet();
			else if (t.equals(Tokens.DO)) 		compileDo();
			else if (t.equals(Tokens.IF)) 		compileIf();
			else if (t.equals(Tokens.WHILE))	compileWhile();
			else 								compileReturn();

			t = tokenStack.peek();
		}

		deindent();
		writeTag("</statements>");
	}


	/**
	 * Compile let statement,
	 * e.g.: let x = 42; or let arr[0] = 42;
	 * 
	 * @throws Exception
	 */
	private void compileLet() throws Exception {
		writeTag("<letStatement>");
		indent();

		compileToken(Tokens.LET);

		compileToken(Tokens.IDENTIFIER_TYPE);

		// Check if array e.g.: arr[...]
		Token t = tokenStack.peek();
		if ( t.equals(Tokens.LEFT_SQUARE_BRACKET) ) {
			compileToken(Tokens.LEFT_SQUARE_BRACKET);
			compileExpression();
			compileToken(Tokens.RIGHT_SQUARE_BRACKET);
		}

		compileToken(Tokens.EQ);

		compileExpression();

		compileToken(Tokens.SEMICOLON);

		deindent();
		writeTag("</letStatement>");
	}


	/**
	 * Compile do statement,
	 * e.g.: do foo(...);
	 * 
	 * @throws Exception
	 */
	private void compileDo() throws Exception {
		if ( !tokenStack.peek().equals(Tokens.DO) )
			throw new Exception("Syntax error!");
		writeTag("<doStatement>");
		indent();

		compileToken(Tokens.DO);

		compileSubroutineCall();

		compileToken(Tokens.SEMICOLON);

		deindent();
		writeTag("</doStatement>");
	}


	/**
	 * Compile if statement,
	 * e.g.: if (...) {...}
	 * 
	 * @throws Exception
	 */
	private void compileIf() throws Exception {
		if ( !tokenStack.peek().equals(Tokens.IF) ) 
			throw new Exception("Syntax error!");
		writeTag("<ifStatement>");
		indent();

		compileToken(Tokens.IF);

		compileToken(Tokens.LEFT_BRACKET);

		compileExpression();

		compileToken(Tokens.RIGHT_BRACKET);

		compileToken(Tokens.LEFT_CURLY_BRACKET);

		compileStatements();

		compileToken(Tokens.RIGHT_CURLY_BRACKET);

		if ( tokenStack.peek().equals(Tokens.ELSE) ) {
			compileToken(Tokens.ELSE);
			compileToken(Tokens.LEFT_CURLY_BRACKET);
			compileStatements();
			compileToken(Tokens.RIGHT_CURLY_BRACKET);
		}

		deindent();
		writeTag("</ifStatement>");
	}


	/**
	 * Compile while statement,
	 * e.g.: while (...) {...}
	 * 
	 * @throws Exception
	 */
	private void compileWhile() throws Exception {
		if ( !tokenStack.peek().equals(Tokens.WHILE) )
			throw new Exception("Syntax error!");
		writeTag("<whileStatement>");
		indent();

		compileToken(Tokens.WHILE);

		compileToken(Tokens.LEFT_BRACKET);

		compileExpression();

		compileToken(Tokens.RIGHT_BRACKET);

		compileToken(Tokens.LEFT_CURLY_BRACKET);

		compileStatements();

		compileToken(Tokens.RIGHT_CURLY_BRACKET);

		deindent();
		writeTag("</whileStatement>");
	}
	

	/**
	 * Compile return statement,
	 * e.g.: return foo; or return 5 > 6;
	 * 
	 * @throws Exception
	 */
	private void compileReturn() throws Exception {
		if ( !tokenStack.peek().equals(Tokens.RETURN) )
			throw new Exception("Syntax error!");

		writeTag("<returnStatement>");
		indent();

		compileToken(Tokens.RETURN);

		if ( !tokenStack.peek().equals(Tokens.SEMICOLON) ) 
			compileExpression();

		compileToken(Tokens.SEMICOLON);

		deindent();
		writeTag("</returnStatement>");
	}


	/**
	 * Compile term
	 * 
	 * @throws Exception
	 */
	private void compileTerm() throws Exception {
		List<String> termKeywords = Arrays.asList("true", "false", "null", "this");

		if (!isTerm(tokenStack.peek())) 
			return;
		
		writeTag("<term>");
		indent();

		Token t = tokenStack.peek();
		if ( Arrays.asList("-", "~").contains(t.getValue()) ) {
			compileToken(Tokens.SYMBOL_TYPE);
			compileTerm();
		}
		else if ( Arrays.asList(Tokens.STRING_TYPE, Tokens.INTEGER_TYPE).contains(t.getType()) ) {
			writeToken(tokenStack.pop());
		}
		else if ( termKeywords.contains(t.getValue()) ) {
			writeToken(tokenStack.pop());
		}
		else if ( t.equals(Tokens.LEFT_BRACKET) ) {
			writeToken(tokenStack.pop());
			compileExpression();
			compileToken(Tokens.RIGHT_BRACKET);
		}
		else {
			Token currentToken = tokenStack.pop();		// Take current Token from stack
			Token nextToken = tokenStack.peek();		// to get Token after it,
			tokenStack.push(currentToken);				// then push it back on

			if (nextToken.equals(Tokens.LEFT_SQUARE_BRACKET)) {
				// nextToken idicates an array-term like: arr[...]
				if ( !currentToken.getType().equals(Tokens.IDENTIFIER_TYPE) )
					throw new Exception("Syntax error!");

				writeToken(tokenStack.pop());				// Write arr
				writeToken(tokenStack.pop());				// Write [
				compileExpression();
				compileToken(Tokens.RIGHT_SQUARE_BRACKET);	// Write ]
			}
			else if (
				nextToken.equals(Tokens.LEFT_BRACKET) ||
				nextToken.equals(Tokens.DOT) 
			) {
				// nextToken indicates a subroutine-call,
				// tokenStack already in the right order because
				// we pushed the current token back on to the stack
				compileSubroutineCall();
			}
			else {
				// Current is just a simple identifier
				compileToken(Tokens.IDENTIFIER_TYPE);
			}
		}

		deindent();
		writeTag("</term>");
	}


	/**
	 * Check if Token {@code t} belongs to a term
	 * 
	 * @param t Token to check
	 * @return Does Token {@code t} belong to term
	 */
	private boolean isTerm(Token t) {
		List<String> keywords = Arrays.asList("true", "false", "null", "this");
		List<String> types = Arrays.asList(Tokens.IDENTIFIER_TYPE, Tokens.STRING_TYPE, Tokens.INTEGER_TYPE);
		List<String> symbols = Arrays.asList("(", ")", "-", "~");

		return (
			keywords.contains(t.getValue()) ||
			types.contains(t.getType()) ||
			symbols.contains(t.getValue())
		);
	}


	/**
	 * Compile expression
	 * 
	 * @throws Exception
	 */
	private void compileExpression() throws Exception {
		List<String> operators = Arrays.asList("+", "-", "*", "/", "=", "&", "|", "<", ">");
		writeTag("<expression>");
		indent();

		compileTerm();

		Token t = tokenStack.peek();
		if ( operators.contains(t.getValue()) ) {
			if ( Arrays.asList("&", "<", ">").contains(t.getValue()) ) {
				String esc = Map.of(
					"&", "&amp;",
					"<", "&lt;",
					">", "&gt;"
				).get(t.getValue());

				tokenStack.pop();
				writeToken(new Token(Tokens.SYMBOL_TYPE, esc));
			}
			else {
				writeToken(tokenStack.pop());
			}
			
			compileTerm();
		}

		deindent();
		writeTag("</expression>");
	}


	/**
	 * Compile expression list
	 * 
	 * @throws Exception
	 */
	private void compileExpressionList() throws Exception {
		writeTag("<expressionList>");
		indent();

		Token t = tokenStack.peek();
		if ( !t.equals(Tokens.RIGHT_BRACKET) ) {
			compileExpression();

			t = tokenStack.peek();
			while (t.equals(Tokens.COMMA)) {
				writeToken(tokenStack.pop());
				compileExpression();
				t = tokenStack.peek();
			}
		}

		deindent();
		writeTag("</expressionList>");
	}
}