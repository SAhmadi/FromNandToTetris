import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * Tokenize and compile .jack files to .xml files
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class Compiler {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("[Execute] java Compiler [DIRECTORY | FILE]");
			System.exit(-1);
		}

		// Get all .jack files
		String[] filePaths = getFilePaths(args[0]);

		// Tokenize all .jack files
		Tokenizer tokenizer = new Tokenizer();
		CompilationEngine compilationEngine = new CompilationEngine();

		for (String fp : filePaths) {
			List<Token> tokens = tokenizer.tokenize(fp);

			String xmlFilePath = fp.split(".jack")[0] + ".xml";			
			compilationEngine.compileToFile(tokens, xmlFilePath);
		}
	}

	
	/**
	 * Get file-paths of all the .jack files inside the directory
	 * or the file-path of the one .jack file if [inputArgument] is a file-path
	 * 
	 * @param inputArgument The file-path of the .jack file or directory
	 * @return Array of the file-paths
	 */
	private static String[] getFilePaths(String inputArgument) {
		String[] filePaths = null;

		// Trim trailing slash if it exists
		String p = inputArgument.trim();
		if (p.endsWith(File.separator)) p = p.substring(0, p.length());

		Path path = Paths.get(p);

		// Check if file-path or directory-path
		if (Files.isRegularFile(path))
			filePaths = new String[]{path.toString()};
		else if (Files.isDirectory(path)) {
			// Get all .jack-files inside the directory
			filePaths = path.toFile().list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jack");
				}
			});

			// Prepend current path to all .jack files inside the array
			for (int i = 0; i < filePaths.length; i++) {
				filePaths[i] = path + File.separator + filePaths[i];
			}
		}

		return filePaths;
	}
}