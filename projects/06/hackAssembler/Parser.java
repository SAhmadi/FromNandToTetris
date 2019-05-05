/**
 * Parser
 * Parses the .hack file and translates instructions
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Parser {
	private String filename;
	private ArrayList<String> commands;
	private ArrayList<String> commandsTranslated;
	private SymbolTable symbolTable;

	/**
	 * Constructor:
	 * Create Symbol-Table, init Commands-List
	 */
	public Parser(String filename) {
		this.filename = filename;
		
		this.symbolTable = new SymbolTable();

		this.commands = new ArrayList<>();
		this.initCommands();

		this.commandsTranslated = new ArrayList<>();
	}

	/**
	 * Init Commands-List
	 */
	private void initCommands() {
		try (
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))
		) {
			String line = bufferedReader.readLine();
			String[] lineSplitted;
			String tmpCommand;

			while (line != null) {
				tmpCommand = "";

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
						// Skip all parts affter comments start,
						// and skip all parts that are spaces
						if (token.equals(Token.SINGLE_COMMENT)) break;
						if (token.equals(Token.SPACE)) continue;
						
						tmpCommand += token;
					}
					commands.add(tmpCommand);
					line = bufferedReader.readLine();
				}
			}
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * Parse Commands-List
	 */
	public void parse() {
		// First Pass:
		// Resolve all labels of form (xxx)
		// Add to SymbolTable pair (xxx, address) with
		// address = the index of label (xxx), delete label from commands
		ArrayList<Integer> linesToDelete = new ArrayList<>();
		int lineNumber = 0;
		for (String command : commands) {
			if (command.startsWith("(")) {
				// Mark current line for deletion
				linesToDelete.add(lineNumber);

				// Remove ( and ) from command
				String key = command.substring(1, command.length()-1).trim();

				// Important: When a line gets deleted, the line-numbers shift down
				symbolTable.addSymbol(key, lineNumber - linesToDelete.size() + 1);
			}
			lineNumber++;
		}

		// Delete all Label-Commands
		for (int i = 0; i < linesToDelete.size(); i++) {
			// After deleting first line, line numbers shift down
			commands.remove((int) linesToDelete.get(i) - i);
		}


		// Second Pass:
		// Translate all Instructions
		// and store in Translated-List
		int ramVariableIndex = 16;
		for (String command : commands) {
			if (isAInstruction(command)) {
				// Strip the @ at the beginning
				command = command.substring(1);

				int value = symbolTable.getSymbolValue(command);
				if (value != -1) translateAInstruction(value);
				else {
					// Check if command is an integer
					if(isInteger(command)) translateAInstruction(Integer.parseInt(command));
					else {
						symbolTable.addSymbol(command, ramVariableIndex);
						translateAInstruction(ramVariableIndex);

						ramVariableIndex++;
					}
				}
			}
			else translateCInstruction(command);
		}

		// Write translated commands to file
		writeToFile();
	}

	/**
	 * Checks if a string can be converted to an integer
	 * 
	 * @param s The string to be converted
	 * @return Can the string be converted
	 */
	private boolean isInteger(String s) {
		try { Integer.parseInt(s); }
		catch (NumberFormatException ex) { return false; }
		return true;
	}


	/**
	 * Check if an instruction is of type A
	 * @param instruction Instruction to check 
	 * @return Is the instruction of type A
	 */
	private boolean isAInstruction(String instruction) {
		return instruction.startsWith("@");
	}


	/**
	 * Translate A-Instruction into Binary
	 * @param valueAsDecimal Instruction as decimal
	 */
	private void translateAInstruction(int valueAsDecimal) {
		String valueAsBinary = Integer.toBinaryString(valueAsDecimal);	
		
		// A-Instructions have a leading 0
		String translatedCommand = "0";
		for (int i = 0; i < 16 - "0".length() - valueAsBinary.length(); i++) {
			// Fill leading places with 0s
			translatedCommand += "0";	
		}
		translatedCommand += valueAsBinary;
		commandsTranslated.add(translatedCommand);
	}
	

	/**
	 * Translate C-Instruction into Binary
	 * @param instruction The instruction to translate
	 */
	private void translateCInstruction(String instruction) {
		// Init with all 0s, if parts are not present in instruction
		String destinationTranslated = "000";
		String compareTranslated = "0000000";
		String jumpTranslated = "000";

		// Get Destination-Part
		if (instruction.contains("=")) {
			String destPart = instruction.split("=")[0].trim();
			if (Token.DEST.containsKey(destPart))
				destinationTranslated = Token.DEST.get(destPart);

			// Strip Dest-Part, to easily get the other parts
			instruction = instruction.split("=")[1].trim();
		}

		// Get Jump-Part
		if (instruction.contains(";")) {
			String jumpPart = instruction.split(";")[1].trim();
			if (Token.JUMP.containsKey(jumpPart))
				jumpTranslated = Token.JUMP.get(jumpPart);
			
			// Strip jump part, to easily get the other parts
			instruction = instruction.split(";")[0].trim();
		}

		// Get Comp-Part, has to be present!
		if (Token.COMP_0.containsKey(instruction))
			compareTranslated = Token.COMP_0.get(instruction);
		else if (Token.COMP_1.containsKey(instruction))
			compareTranslated = Token.COMP_1.get(instruction);
		else {
			System.out.println("[Error] Syntax error in the .asm file!");
			System.exit(-1);
		}

		commandsTranslated.add(
			"111" + 
			compareTranslated + 
			destinationTranslated + 
			jumpTranslated
		);
	}


	/**
	 * Write translated commands to output file
	 */
	private void writeToFile() {
		// Set .hack file-extension
		int lastDotIndex = filename.lastIndexOf(".");
		String outputFilename = filename.substring(0, lastDotIndex) + ".hack";

		try (
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilename))
		) {
			for (String command : commandsTranslated) {
				bufferedWriter.append(command.trim());
				bufferedWriter.append(System.lineSeparator());
			}
		}
		catch (IOException e) { e.printStackTrace(); }
	}


	/* GETTERS AND SETTERS */
	public String getFilename() { return filename; }
	public void setFilename(String value) { filename = value; }

	public ArrayList<String> getCommands() { return commands; }
	public void setCommands(ArrayList<String> value) { commands = value; }

	public ArrayList<String> getCommandsTranslated() { return commandsTranslated; }
	public void setCommandsTranslated(ArrayList<String> value) { commandsTranslated = value; }

	public SymbolTable getSymbolTable() { return symbolTable; }
	public void setSymbolTable(SymbolTable value) { symbolTable = value; }
}