import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class ApacheConfToVanityURL {

	public static void main(String[] args) {
		
        //input conf file to read
		File file = new File("data.conf");
        
        try {
           
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                //read only line starting with RewriteRule which is active in 301 redirect
               if (line.startsWith("RewriteRule"))
               {

            	   //split in array for white or tab space
            String ar[]= line.split("\\s+");
            //printing in console comma seperated old,new url
            System.out.println( ar[1] +   "," + ar[2]);
              
        }
               
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        
	}
}
