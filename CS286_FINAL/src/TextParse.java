import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class TextParse {
	public static boolean debug = false;
	
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
	
}
