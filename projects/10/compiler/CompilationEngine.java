import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class CompilationEngine {
	private Stack<Token> tokenStack;
	private List<String> xmlTagList;
	private int indentation;

	/**
	 * Constructor
	 * Initialize the tokenStack and
	 * the xml lines
	 */
	public CompilationEngine() {
		this.tokenStack = new Stack<>();
		this.xmlTagList = new ArrayList<>();
		this.indentation = 0;
	}


	/**
	 * Compile the list of tokens into
	 * their corresponding xml-tag and
	 * write results to file
	 * @param tokens The list of tokes to convert
	 * @param filePath The file to write the xml-tags to
	 */
	public void compileToFile(List<Token> tokens, String filePath) {
		// Store tokens in Stack, for easier handling
		Collections.reverse(tokens);
		tokenStack.addAll(tokens);

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
	 * Compile the line where class-keyword occurs
	 * @throws Exception
	 */
	private void compileClass() throws Exception {
		// Make sure class-keyword is on top of stack
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "class")) ) throw new Exception("Syntax error");
		writeTag("<class>");					// Write starting <class>

		indentation += 2;
		writeToken(tokenStack.pop());	// Write <keyword>class</keyword>
		
		t = tokenStack.peek();
		if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());	// Write <identifier>SomeClassName</identifier>

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "{")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());	// Write <symbol> { </symbol>

		// Compile all class members
		compileClassVarDec();

		// Compile
		compileSubroutine();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "}")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());	// Write <symbol> } </symbol>
		
		indentation -= 2;
		writeTag("</class>");
	}


	private void compileClassVarDec() throws Exception {
		Token t = tokenStack.peek();
		if (
			!t.equals(new Token(Tokenizer.KEYWORD, "static")) &&
			!t.equals(new Token(Tokenizer.KEYWORD, "field"))
		) return;

		writeTag("<classVarDec>");				// Write starting <classVarDec>
		indentation += 2;

		writeToken(tokenStack.pop());			// Write <keyword> field or static </keyword>

		compileVarType();									// Compile int x or Foo y or ...

		// Compile possible multi declarations: field int x, y, z, ...
		t = tokenStack.peek();
		Token comma = new Token(Tokenizer.SYMBOL, ",");
		while ( t.equals(comma) ) {
			writeToken(tokenStack.pop());		// Write <symbol> , </symbol>

			t = tokenStack.peek();
			if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write identifier

			t = tokenStack.peek();					// Move to next token
		}

		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());			// Write <symbol> ; </symbol>
		
		indentation -= 2;
		writeTag("</classVarDec>");				// Write ending </classVarDec>

		// Move to next class member
		compileClassVarDec();
	}


	private void compileVarType() throws Exception {
		Token t = tokenStack.peek();
		if (
			!t.getType().equals(Tokenizer.IDENTIFIER) &&
			!t.getType().equals(Tokenizer.KEYWORD)
		) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());		// Write <keyword> int or Foo or ... </keyword>

		t = tokenStack.peek();
		if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());		// Write <identifier> x or y or ... </identifier>
	}


	private void compileSubroutine() throws Exception {
		Token t = tokenStack.peek();
		Token c = new Token(Tokenizer.KEYWORD, "constructor");
		Token f = new Token(Tokenizer.KEYWORD, "function"); 
		Token m = new Token(Tokenizer.KEYWORD, "method");
		while (t.equals(c) || t.equals(f) || t.equals(m)) {
			writeTag("<subroutineDec>");		// Write starting <subroutineDec>
			indentation += 2;
			writeToken(tokenStack.pop());		// Write <keyword> function or method or constructor </keyword>

			t = tokenStack.peek();
			if (
				!t.getType().equals(Tokenizer.IDENTIFIER) &&
				!t.getType().equals(Tokenizer.KEYWORD)
			) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write return type of subroutine

			t = tokenStack.peek();
			if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write name of subroutine

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "(")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write (

			compileParams();								// Compile Parameters

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, ")")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write )

			writeTag("<subroutineBody>");
			indentation += 2;

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "{")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write {

			compileVarDec();								// Compile var declarations
			compileStatements();						// Compile statements

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "}")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write }

			indentation -= 2;
			writeTag("</subroutineBody>");

			indentation -= 2;
			writeTag("</subroutineDec>");

			t = tokenStack.peek();
		}
	}


	private void compileSubroutineCall() throws Exception {
		//System.out.println("*** Now @ subroutineCall");
		Token t = tokenStack.peek();
		if (!t.getType().equals(Tokenizer.IDENTIFIER)) throw new Exception("Syntax error");
		// System.out.println("*** foo14 " + tokenStack.peek());
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if ( t.equals(new Token(Tokenizer.SYMBOL, ".")) ) {
			// System.out.println("*** foo15 " + tokenStack.peek());
			writeToken(tokenStack.pop());
			
			t = tokenStack.peek();
			if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
			// System.out.println("*** foo16 " + tokenStack.peek());
			writeToken(tokenStack.pop());
		}

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "(")) ) throw new Exception("Syntax error");
		// System.out.println("*** foo17 " + tokenStack.peek());
		writeToken(tokenStack.pop());

		compileExpressionList();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ")")) ) throw new Exception("Syntax error");
		// System.out.println("*** foo18 " + tokenStack.peek());
		writeToken(tokenStack.pop());
	}


	private void compileParams() throws Exception {
		writeTag("<parameterList>");
		indentation += 2;

		// Write all Parameters
		Token t = tokenStack.peek();	
		if (
			t.getType().equals(Tokenizer.IDENTIFIER) ||
			t.getType().equals(Tokenizer.KEYWORD)
		) {
			writeToken(tokenStack.pop());		// Write type int, boolean, ...

			t = tokenStack.peek();
			if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write identifer x, y, ...

			t = tokenStack.peek();
			Token comma = new Token(Tokenizer.SYMBOL, ",");
			while ( t.equals(comma) ) {
				writeToken(tokenStack.pop());
				compileVarType();
				t = tokenStack.peek();
			}
		}

		indentation -= 2;
		writeTag("</parameterList>");
	}


	private void compileVarDec() throws Exception {
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "var")) ) return;

		writeTag("<varDec>");					// write starting <varDec>
		indentation += 2;

		writeToken(tokenStack.pop());	// write <keyword> var </keyword>
		
		compileVarType();							// Compile var type and identifier

		t = tokenStack.peek();
		Token comma = new Token(Tokenizer.SYMBOL, ",");
		while ( t.equals(comma) ) {
			writeToken(tokenStack.pop());	// Write ,

			t = tokenStack.peek();
			if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());	// Write identifier
			
			t = tokenStack.peek();
		}

		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());		// Write ;
		
		indentation -= 2;
		writeTag("</varDec>");					// Write ending </varDec>
		
		compileVarDec();								// Move to next var declaration
	}


	private void compileStatements() throws Exception {
		writeTag("<statements>");				// Write starting <statements>
		indentation += 2;

		Token t = tokenStack.peek();
		Token letToken = new Token(Tokenizer.KEYWORD, "let");
		Token doToken = new Token(Tokenizer.KEYWORD, "do");
		Token ifToken = new Token(Tokenizer.KEYWORD, "if");
		Token whileToken = new Token(Tokenizer.KEYWORD, "while");
		Token returnToken = new Token(Tokenizer.KEYWORD, "return");
		while (
			t.equals(letToken) ||
			t.equals(doToken) ||
			t.equals(ifToken) ||
			t.equals(whileToken) ||
			t.equals(returnToken)
		) {
			if ( t.equals(letToken) )
				compileLet();
			else if ( t.equals(doToken) )
				compileDo();
			else if ( t.equals(ifToken) )
				compileIf();
			else if ( t.equals(whileToken) )
				compileWhile();
			else 
				compileReturn();

			t = tokenStack.peek();
		}

		indentation -= 2;
		writeTag("</statements>");
	}


	private void compileLet() throws Exception {
		// System.out.println("@Let");
		writeTag("<letStatement>");
		indentation += 2;

		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "let")) ) throw new Exception("Syntax error");
		// System.out.println("*** " + tokenStack.peek());
		writeToken(tokenStack.pop());		// Write let

		t = tokenStack.peek();
		if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
		// System.out.println("*** " + tokenStack.peek());
		writeToken(tokenStack.pop());		// Write identifier

		// Check if array
		t = tokenStack.peek();
		if ( t.equals(new Token(Tokenizer.SYMBOL, "[")) ) {
			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "[")) ) throw new Exception("Syntax error");
			// System.out.println("*** " + tokenStack.peek());
			writeToken(tokenStack.pop());

			compileExpression();

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "]")) ) throw new Exception("Syntax error");
			// System.out.println("*** " + tokenStack.peek());
			writeToken(tokenStack.pop());
		}

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "=")) ) throw new Exception("Syntax error");
		// System.out.println("*** " + tokenStack.peek());
		writeToken(tokenStack.pop());

		compileExpression();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) throw new Exception("Syntax error");
		// System.out.println("*** foo " + tokenStack.peek());
		writeToken(tokenStack.pop());

		indentation -= 2;
		writeTag("</letStatement>");
	}


	private void compileDo() throws Exception {
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "do")) ) throw new Exception("Syntax error");
		writeTag("<doStatement>");
		indentation += 2;

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "do")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		compileSubroutineCall();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		indentation -= 2;
		writeTag("</doStatement>");
	}


	private void compileIf() throws Exception {
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "if")) ) throw new Exception("Syntax error");
		writeTag("<ifStatement>");
		indentation += 2;

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "if")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "(")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		compileExpression();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ")")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "{")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		compileStatements();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "}")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if (t.equals(new Token(Tokenizer.KEYWORD, "else"))) {
			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.KEYWORD, "else")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "{")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());

			compileStatements();

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "}")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());
		}

		indentation -= 2;
		writeTag("</ifStatement>");
	}


	private void compileWhile() throws Exception {
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "while")) ) throw new Exception("Syntax error");
		writeTag("<whileStatement>");
		indentation += 2;

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "while")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "(")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		compileExpression();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ")")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "{")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		compileStatements();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "}")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		indentation -= 2;
		writeTag("</whileStatement>");
	}
	

	private void compileReturn() throws Exception {
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "return")) ) throw new Exception("Syntax error");
		writeTag("<returnStatement>");
		indentation += 2;

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "return")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) 
			compileExpression();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		indentation -= 2;
		writeTag("</returnStatement>");
	}


	private void compileTerm() throws Exception {
		// System.out.println("@Term");
		List<String> termKeywords = Arrays.asList("true", "false", "null", "this");
		List<String> termTypes = Arrays.asList(Tokenizer.IDENTIFIER, Tokenizer.STRING, Tokenizer.INTEGER);
		List<String> termSymbols = Arrays.asList("(", ")", "-", "~");

		Token t = tokenStack.peek();
		
		if (!isTerm(t, termKeywords, termTypes, termSymbols)) return;
		
		writeTag("<term>");
		indentation += 2;

		if (Arrays.asList("-", "~").contains(t.getValue())) {
			if ( !t.getType().equals(Tokenizer.SYMBOL) ) throw new Exception("Syntax error");
			// System.out.println("*** foo5 " + tokenStack.peek());
			writeToken(tokenStack.pop());
			compileTerm();
		}
		else if (Arrays.asList(Tokenizer.STRING, Tokenizer.INTEGER).contains(t.getType())) {
			// System.out.println("*** foo6 " + tokenStack.peek());
			writeToken(tokenStack.pop());
		}
		else if (termKeywords.contains(t.getValue())) {
			// System.out.println("*** foo7 " + tokenStack.peek());
			writeToken(tokenStack.pop());
		}
		else if ( t.equals(new Token(Tokenizer.SYMBOL, "(")) ) {
			// System.out.println("*** foo8 " + tokenStack.peek());
			writeToken(tokenStack.pop());
			compileExpression();

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, ")")) ) throw new Exception("Syntax error");
			// System.out.println("*** foo9 " + tokenStack.peek());
			writeToken(tokenStack.pop());
		}
		else {
			Token currentToken = tokenStack.pop();	// Take current Token from stack
			Token nextToken = tokenStack.peek();		// to get Token after it,
			tokenStack.push(currentToken);					// then push it back on

			if (nextToken.equals(new Token(Tokenizer.SYMBOL, "["))) {
				// nextToken idicates an arrayTerm like: arr[...]
				if ( !currentToken.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
				// System.out.println("*** foo10 " + tokenStack.peek());
				writeToken(tokenStack.pop());					// Write arr
				// System.out.println("*** foo11 " + tokenStack.peek());
				writeToken(tokenStack.pop());					// Write [
				
				compileExpression();

				t = tokenStack.peek();
				if (!t.equals(new Token(Tokenizer.SYMBOL, "]"))) throw new Exception("Syntax error");
				// System.out.println("*** foo 12" + tokenStack.peek());
				writeToken(tokenStack.pop());					// Write ]
			}
			else if (
				nextToken.equals(new Token(Tokenizer.SYMBOL, "(")) ||
				nextToken.equals(new Token(Tokenizer.SYMBOL, ".")) 
			) {
				// nextToken indicates a subroutineCall,
				// tokenStack already in the right order because
				// we pushed the current Token back on to the stack
				compileSubroutineCall();
			}
			else {
				// Current is just a simple identifier
				// System.out.print("@term last else: ");
				t = tokenStack.peek();
				if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
				// System.out.println("*** foo13 " + tokenStack.peek());
				writeToken(tokenStack.pop());
			}
		}

		indentation -= 2;
		writeTag("</term>");
	}

	private boolean isTerm(Token t, List<String> keywords, List<String> types, List<String> symbols) {
		return (
			keywords.contains(t.getValue()) ||
			types.contains(t.getType()) ||
			symbols.contains(t.getValue())
		);
	}


	private void compileExpression() throws Exception {
		// System.out.println("@Expression");
		List<String> operators = Arrays.asList("+", "-", "*", "/", "=", "&", "|", "<", ">");
		writeTag("<expression>");
		indentation += 2;

		compileTerm();

		Token t = tokenStack.peek();
		// System.out.println("*** foo2 " + tokenStack.peek());
		if (operators.contains(t.getValue())) {
			if (Arrays.asList("&", "<", ">").contains(t.getValue())) {
				String esc = Map.of(
					"&", "&amp;",
					"<", "&lt;",
					">", "&gt;"
				).get(t.getValue());

				// System.out.println("*** foo3 " + tokenStack.peek());
				tokenStack.pop();
				writeToken(new Token(Tokenizer.SYMBOL, esc));
			}
			else {
				// System.out.println("*** foo4 " + tokenStack.peek());
				writeToken(tokenStack.pop());
			}
			
			compileTerm();
		}

		indentation -= 2;
		writeTag("</expression>");
	}


	private void compileExpressionList() throws Exception {
		writeTag("<expressionList>");
		indentation += 2;

		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ")")) ) {
			compileExpression();

			t = tokenStack.peek();
			Token comma = new Token(Tokenizer.SYMBOL, ",");
			while (t.equals(comma)) {
				// System.out.println("*** " + tokenStack.peek());
				writeToken(tokenStack.pop());
				compileExpression();
				t = tokenStack.peek();
			}
		}

		indentation -= 2;
		writeTag("</expressionList>");
	}

}