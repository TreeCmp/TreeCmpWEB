package pl.gda.pg.eti.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;


public class NewickUtils {

	public static String WINDOW_COMPARISION_MODE = "-w";
	public static String MATRIX_COMPARISION_MODE = "-m";
	public static String OVERLAPPING_PAIR_COMPARISION_MODE = "-s";
	public static String REF_TO_ALL_COMPARISION_MODE = "-r";
	public static String STRING_INPUT = "stringInput";
	public static String FILE_INPUT = "fileInput";
	
	public static String GetNormalizedNewickString(String newick) {
		int openingBracketPosition = newick.indexOf("(");
		
		if(openingBracketPosition > -1) {
			return newick.substring(openingBracketPosition, newick.length());			
		}					
		
		return "";
	}
	
	public File createTempFileWithGivenContent(String fileContent) {
		BufferedWriter writer = null;
		File temp = null;
		try	{
			String filename = createName();
			temp = File.createTempFile(filename, ".newick");
			writer = new BufferedWriter(new FileWriter(temp));
			writer.write(fileContent);
			writer.close();
			return temp;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return temp;		
	}
	
	public File createEmptyTempFile() {
		BufferedWriter writer = null;
		File temp = null;
		try	{
			String filename = createName();
			temp = File.createTempFile(filename, ".newick.out");
			writer = new BufferedWriter(new FileWriter(temp));
			writer.close();
			return temp;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return temp;
	}
	
	private String createName() {
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
		
		return "trees" + currentTimestamp.toString().replace(':', '-');
	}
}
