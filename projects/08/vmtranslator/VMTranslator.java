package vmtranslator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * VMTranslator
 * Translates VM-Instructions into Hack-Assembly
 * 
 * @author Sirat Ahmadi
 * @version 1.1
 */
public class VMTranslator {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("[Error] Execute: java VMTranslator [filename.vm | ./path/to/file/]");
            System.exit(-1);
        }
    
        Map<String, String> pathComponents = new HashMap<>();
        boolean addSysInit = false;

        // Trim trailing slash if it exists
        String input = args[0].trim();
        if (input.endsWith(File.separator)) 
            input = input.substring(0, input.length());

        Path inputPath = Paths.get(input);

        // Check if file-path or directory-path
        if (Files.isRegularFile(inputPath)) {
            String fileNameWithExtension = inputPath.getFileName().toString();
            String fileName = fileNameWithExtension.substring(
                0, fileNameWithExtension.lastIndexOf(".")
            );
            
            String filePath = inputPath.getParent().toString() + File.separator;
            
            pathComponents.put("name", fileName);
            pathComponents.put("path", filePath);
        }
        else if (Files.isDirectory(inputPath)) {
            String dirName = inputPath.getFileName().toString();
            // last part belongs to path, because merged-file belongs inside directory
            String dirPath = inputPath.toString();

            pathComponents.put("name", dirName);
            pathComponents.put("path", dirPath);

            // Get all .vm-files inside the directory
            File[] listOfFiles = inputPath.toFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".vm");
                }
            });

            // When Sys.vm file exists in directory
            // then add Sys.init in CodeWriter
            for (File f : listOfFiles)
                if (f.getName().equals("Sys.vm")) addSysInit = true;
            
            // When only one file inside directory with the same name as directory,
            // skip merge process
            boolean shouldMerge = true;
            if (listOfFiles.length == 1 && listOfFiles[0].getName().equals(dirName + ".vm")) 
                shouldMerge = false;

            // Merge all .vm-files into one, for CodeWriter to translate
            if (shouldMerge) 
                mergeFiles(listOfFiles, pathComponents.get("path"), pathComponents.get("name"), "vm");
        }
        else {
            System.out.println("[Error] Execution failed.");
            System.exit(-1);
        }

        // Create a parser and parse through file
        Parser parser = new Parser(pathComponents.get("name"), pathComponents.get("path"));
        parser.parse();

        // Create a codeWriter and translate commands
        // then write to file
        CodeWriter codeWriter = new CodeWriter(pathComponents.get("name"), pathComponents.get("path"), addSysInit);
        codeWriter.translate(parser.getCommands());
        codeWriter.writeToFile();
    }


    /**
     * Merge all .vm-files into one
     * 
     * @param files Files with .vm-extension inside the directory
     * @param path Directory-path of the new merged .vm file
     * @param fileName Filename of the new merged .vm file
     * @param extension .vm file-extension
     */
    private static void mergeFiles(File[] files, String path, String fileName, String extension) {
        ArrayList<File> vmFiles = new ArrayList<>(Arrays.asList(files));
        String mergedPathAndName = path + File.separator + fileName + "." + extension;

        File mergedFile = new File(mergedPathAndName);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(mergedFile))) { 
            // First file to copy into the merged-file is Sys.vm,
            // so we move it to the front of the list
            for (int i = 0; i < vmFiles.size(); i++) {
                if (vmFiles.get(i).getName().equals("Sys.vm")) {
                    File sysFile = vmFiles.get(i);
                    vmFiles.remove(i);
                    vmFiles.add(0, sysFile);
                }
            }

            // Copy files into merge-file
            for (File f : vmFiles) {                
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f))
                );

                String line;
                while ((line = in.readLine()) != null) {
                    out.write(line);
                    out.newLine();
                }

                in.close();
            }
        }
        catch (IOException ex) {
            System.out.println("[Error] File-merge failed!");
            System.exit(-1);
        }
    }
}