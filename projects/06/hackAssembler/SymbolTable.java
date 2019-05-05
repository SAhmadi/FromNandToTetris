/**
 * SymbolTable
 * Stores Hack-Assembly symbols
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	private Map<String, Integer> table;


	/**
	 * Constructor:
	 * Init Symbol-Table with default symbols
	 */
	public SymbolTable() {
		this.table = new HashMap<String, Integer>();

		// Insert R0: 0, R1: 1, ..., R15: 15
		for (int i = 0; i <= 15; i++) { this.table.put("R" + i, i); }

		this.table.put("SCREEN", 16384);
		this.table.put("KBD", 24576);

		this.table.put("SP", 0);
		this.table.put("LCL", 1);
		this.table.put("ARG", 2);		
		this.table.put("THIS", 3);
		this.table.put("THAT", 4);
	}


	/**
	 * Add a symbol to the table
	 * @param key
	 * @param value
	 */
	public void addSymbol(String key, int value) {
		table.put(key, value);
	}


	/**
	 * Get value of symbol
	 * @param key
	 * @return Value of the given key
	 */
	public int getSymbolValue(String key) {
		if (table.containsKey(key)) return table.get(key);
		return -1;
	}

	
	/* GETTERS AND SETTERS */
	public Map<String, Integer> getTable() { return table; }
	public void setTable(Map<String, Integer> value) { table = value; }
}