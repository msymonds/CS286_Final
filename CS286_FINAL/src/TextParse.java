/*
 * Michael Symonds
 * CS286 FINAL
 * 
 * Utility class for reading in text
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class TextParse {
	
	// toggle to get/hide additional output status messages
	public static boolean debug = false; 
	
	
	/*
	 * Reads in the text file from the Brown corpus
	 * and builds a vector of the given size with 
	 * chars from that text. If space == true, will 
	 * also include spaces as characters as well.
	 */
	public static char[] getText(String file, int size, boolean space){
		File f = new File(file);
		String input = "";
		char[] result = new char[size];
		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			while( (input = reader.readLine()) != null && count < size){
				char[] text = input.toCharArray();
				for(int i = 0; i < text.length; i++){
					if(space){
						if(count < size){
							result[count] = text[i];
							count++;
						}
					}  else {
						if(text[i] != ' ' && count < size){
							result[count] = text[i];
							count++;
						}
					}
				}
			}
			reader.close();
			if(debug){
				System.out.println("Chars collected: " + count);
				
				System.out.println("Sample input:");
				for(int i = 0; i < 100; i++){
					System.out.print(result[i] + ((i!=0 && i%20 == 0) ? "\n":" "));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	 * creates a new file called results.txt
	 * if file already exists, it will 
	 * overwrite all content with a blank
	 * to reset the file for new results
	 */
	public static void initializeResultsFile(){
		File f = new File("results.txt");
		try {
			FileWriter writer = new FileWriter(f, false);
			writer.write("");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * appends the given string to the results file
	 */
	public static void appendToFile(String s){
		File f = new File("results.txt");
		try {
			FileWriter writer = new FileWriter(f, true);
			writer.write(s + System.getProperty( "line.separator" ));
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
