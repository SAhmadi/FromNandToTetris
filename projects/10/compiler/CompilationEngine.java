import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


public class CompilationEngine {
	private Stack<Token> tokenStack;
	private List<String> lines;
	private int indentation;

	public CompilationEngine() {
		this.tokenStack = new Stack<>();
		this.lines = new ArrayList<>();
		this.indentation = 0;
	}

	public void compileToFile(List<Token> tokens, String filePath) {
		// Store tokens in Stack, for easier handling
		Collections.reverse(tokens);
		tokenStack.addAll(tokens);

		try { compileClass(); }
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}


	private void writeTag(String tag) {
		String spaces = "";
		for (int i = 0; i < indentation; i++) spaces += " ";

		lines.add(spaces + tag);
	}


	private void writeToken(Token t) {
		String tokenType = t.getType();
		String tokenValue = t.getValue();

		writeTag(
			String.format("<%s> %s </%s>", tokenType, tokenValue, tokenType)
		);
	}


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


		for (String l : lines) {
			System.out.println("*** " + l);
		}
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

		compileVarType();						// Compile int x or Foo y or ...

		// Compile possible multi declarations: field int x, y, z, ...
		t = tokenStack.peek();
		while ( t.equals(new Token(Tokenizer.SYMBOL, ",")) ) {
			writeToken(tokenStack.pop());		// Write <symbol> , </symbol>

			t = tokenStack.peek();
			if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) 
				throw new Exception("Syntax error");
			writeToken(tokenStack.pop());		// Write identifier

			t = tokenStack.peek();					// Move to next token
		}

		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) 
			throw new Exception("Syntax error");
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
		while (
			t.equals(new Token(Tokenizer.KEYWORD, "constructor")) ||
			t.equals(new Token(Tokenizer.KEYWORD, "function")) ||
			t.equals(new Token(Tokenizer.KEYWORD, "method"))
		) {
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
		}
	}


	private void compileParams() {
		writeTag("<parameterList>");
		indentation += 2;

		// Write all Parameters
		Token t = tokenStack.peek();	
		if (
			t.getType().equals(Tokenizer.IDENTIFIER) ||
			t.getType().equals(Tokenizer.KEYWORD)
		) {
			writeToken(tokenStack.pop());

			t = tokenStack.peek();
			while ( t.equals(new Token(Tokenizer.SYMBOL, ",")) ) {
				writeToken(tokenStack.pop());
				compileVarType();
				t = tokenStack.peek();
			}
		}

		indentation -= 2;
		writeTag("</parameterList>");
	}


	private void compileVarDec() {
		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "var")) ) return;

		writeTag("<varDec>");					// write starting <varDec>
		indentation += 2;

		writeToken(tokenStack.pop());	// write <keyword> var </keyword>
		
		compileVarType();							// Compile var type and identifier

		t = tokenStack.peek();
		while ( t.equals(new Token(Tokenizer.SYMBOL, ",")) ) {
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
		
		// Move to next var declaration
		compileVarDec();
	}


	private void compileStatements() {
		writeTag("<statements>");				// Write starting <statements>
		indentation += 2;

		Token t = tokenStack.peek();
		while (
			t.equals(new Token(Tokenizer.KEYWORD, "let")) ||
			t.equals(new Token(Tokenizer.KEYWORD, "do")) ||
			t.equals(new Token(Tokenizer.KEYWORD, "if")) ||
			t.equals(new Token(Tokenizer.KEYWORD, "while")) ||
			t.equals(new Token(Tokenizer.KEYWORD, "return"))
		) {
			if ( t.equals(new Token(Tokenizer.KEYWORD, "let")) )
				compileLet();
			else if ( t.equals(new Token(Tokenizer.KEYWORD, "do")) )
				compileDo();
			else if ( t.equals(new Token(Tokenizer.KEYWORD, "if")) )
				compileIf();
			else if ( t.equals(new Token(Tokenizer.KEYWORD, "while")) )
				compileWhile();
			else 
				compileReturn();
		}

		indentation -= 2;
		writeTag("</statements>");
	}


	private void compileLet() throws Exception {
		writeTag("<letStatement>");
		indentation += 2;

		Token t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.KEYWORD, "let")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());		// Write let

		t = tokenStack.peek();
		if ( !t.getType().equals(Tokenizer.IDENTIFIER) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());		// Write identifier

		// Check if array
		t = tokenStack.peek();
		if ( t.equals(new Token(Tokenizer.SYMBOL, "[")) ) {
			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "[")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());

			compileExpression();

			t = tokenStack.peek();
			if ( !t.equals(new Token(Tokenizer.SYMBOL, "]")) ) throw new Exception("Syntax error");
			writeToken(tokenStack.pop());
		}

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, "=")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		compileExpression();

		t = tokenStack.peek();
		if ( !t.equals(new Token(Tokenizer.SYMBOL, ";")) ) throw new Exception("Syntax error");
		writeToken(tokenStack.pop());

		indentation -= 2;
		writeTag("</letStatement>");
	}

	
}