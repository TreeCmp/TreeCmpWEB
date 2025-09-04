package pl.edu.pg.eti.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;


public class NewickUtils {

	public static final String WINDOW_COMPARISION_MODE = "-w";
	public static final String MATRIX_COMPARISION_MODE = "-m";
	public static final String OVERLAPPING_PAIR_COMPARISION_MODE = "-s";
	public static final String REF_TO_ALL_COMPARISION_MODE = "-r";
	public static final String STRING_INPUT = "stringInput";
	public static final String FILE_INPUT = "fileInput";
	
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
        File temp = null;
        try {
            String filename = createName();
            // Pobierz katalog tymczasowy z ENV lub systemowy
            String tmpDirPath = System.getenv("TMPDIR");
            if (tmpDirPath == null || tmpDirPath.isEmpty()) {
                tmpDirPath = System.getProperty("java.io.tmpdir");
            }

            File tmpDir = new File(tmpDirPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            temp = File.createTempFile(filename, ".newick.out", tmpDir);

            // Upewnij się, że plik został utworzony
            if (!temp.exists()) {
                temp.createNewFile();
            }

            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }
	
	private String createName() {
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
		
		return "trees" + currentTimestamp.toString().replace(':', '-').replace(' ','_');
	}
}
