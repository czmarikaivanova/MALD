import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Map {
	
	Location[][] grid;
	private int width;
	private int height;
	
	public Map(File input) {
		super();
		readInputFile(input);
	}

	private void readInputFile(File input) {
		try {
			FileReader in = new FileReader("C:/test.txt");
		    BufferedReader br = new BufferedReader(in);
			while (br.readLine() != null) {
				System.out.println(br.readLine());
		    	in.close();
			}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
}
			