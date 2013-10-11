import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FindUnix {

    public static class Finder extends SimpleFileVisitor<Path> {
        
		CopyOption[] options =  new CopyOption[] { REPLACE_EXISTING } ;
		private final PathMatcher matcher;
        private int numMatches = 0;

        Finder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        //Compares the glob pattern against the file or directory name.
        void find(Path file) {
            Path name = file.getFileName();

            if (name != null && matcher.matches(name)) {
                numMatches++;
                
				//System.out.println("Path is"+file.getParent());
				//System.out.println("Name is"+file.getName());
			//	System.out.println();
				try {
				    File inputFile = new File(file.toString());
				    File outputFile = new File(file.getParent().toString()+ "\\"+
							file.getFileName().toString().substring(0,file.getFileName().toString().lastIndexOf("."))+".sh");

				    FileReader in = new FileReader(inputFile);
				    FileWriter out = new FileWriter(outputFile);
				    int c;

				    while ((c = in.read()) != -1)
				      out.write(c);

				    in.close();
				    out.close();
				    convertShellScript(outputFile);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				

            }
        }

        

		private void convertShellScript(File outputFile) {
        	try
            {
            File file = new File(outputFile.toString());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", oldtext = "";
            while((line = reader.readLine()) != null)
                {
                oldtext += line + "\r\n";
            }
            reader.close();
            
            //To replace a line in a file
            String newtext = oldtext.replaceAll("SET", "export");
            newtext = newtext.replaceAll("rem", "###");
            newtext = newtext.replaceAll("REM", "###");
            newtext = newtext.replaceAll("set", "export");
            newtext = newtext.replaceAll("\\%", "\\$");
            newtext = newtext.replace("\\", "/");
            newtext = newtext.replaceAll("@echo OFF", "#!/bin/bash");
            newtext = newtext.replaceAll("@echo off", "#!/bin/bash");
            newtext = newtext.replaceAll("d:", "");
            newtext = newtext.replaceAll("D:", "");
            newtext = newtext.replaceAll("c:", "");
            newtext = newtext.replaceAll("C:", "");           
            newtext = newtext.replace("$CLASSPATH$", "${CLASSPATH}");
            newtext = newtext.replace("$JAVA_HOME$", "${JAVA_HOME}");
            newtext = newtext.replace("$APP_PATH$", "${APP_PATH}");
            newtext = marketliveSpecific(newtext);
           // newtext = newtext.replaceAll(".bat", ".sh");
            newtext = newtext.replaceAll(";", ":");
            newtext = newtext.replaceAll("exporttings", "settings");
          
            
            
            FileWriter writer = new FileWriter(outputFile.toString());
            writer.write(newtext);writer.close();
        }
        catch (IOException ioe)
            {
            ioe.printStackTrace();
        }
			
		}

		private String marketliveSpecific(String newtext) {
			
            newtext = newtext.replace("$BIN_DIR$", "${BIN_DIR}");
            newtext = newtext.replace("$CONF_DIR$", "${CONF_DIR}");
            newtext = newtext.replace("$SITE_HOME$", "${SITE_HOME}");
            newtext = newtext.replace("$PATH$", "${PATH}");
            newtext = newtext.replace("$OUTPUT_FOLDER$", "${OUTPUT_FOLDER}");
            newtext = newtext.replace("$RESIN_HOME$", "${RESIN_HOME}");         
            newtext = newtext.replace("$COMPUTERNAME$", "${COMPUTERNAME}");
			return newtext;
		}

		//Prints the total number of matches to standard out.
        void done() {
            System.out.println("Matched: " + numMatches);
        }

        //Invoke the pattern matching method on each file.
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            find(file);
            return CONTINUE;
        }

        //Invoke the pattern matching method on each directory.
        public FileVisitResult preVisitDirectory(Path dir) {
            find(dir);
            return CONTINUE;
        }

        //If there is some error accessing the file system, let the
        //user know the precise error.
   
        public FileVisitResult preVisitDirectoryFailed(Path dir, IOException exc) {
            if (exc instanceof AccessDeniedException) {
                System.err.println(dir + ": cannot access directory");
            } else {
                System.err.println(exc);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }

    static void usage() {
        System.err.println("java Find <path> -name \"<glob_pattern>\"");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {

        if (args.length < 3 || !args[1].equals("-name"))
            usage();

        Path startingDir = Paths.get(args[0]);
        String pattern = args[2];

        Finder finder = new Finder(pattern);
        Files.walkFileTree(startingDir, finder);
        finder.done();
    }
}

