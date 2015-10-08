

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.sf.classifier4J.summariser.ISummariser;
import net.sf.classifier4J.summariser.SimpleSummariser;

 
/**
 * The class to instantiate Simple Summariser object. Simple Summariser can be found in
 * Classify4j project
 * @author Rushdi Shams
 * @version 1.0 October 08, 2015.
 *
 */
public class SummarizationBatch {
    //--------------------------------------------------------------------------------
    //Instance variables
    //--------------------------------------------------------------------------------
    private static String summaryText;
    private static ISummariser summarizer;
	private static Logger logger = Logger.getLogger("MyLog");
    //--------------------------------------------------------------------------------
    //Methods
    //--------------------------------------------------------------------------------
    
    public static void initiateLogger(File file){
		FileHandler fileHandler;
		try {
			// This block configure the logger with handler and formatter
			fileHandler = new FileHandler(file.getParentFile().getAbsolutePath()+ "/"
					+ "log.txt", true);
			logger.addHandler(fileHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    /**
     * Method to summarize a given string
     * @param originalText is the text to be summarized
     * @param summarySize is in integer denotes the sentences in the summary
     * @return the summary of the sentence
     */
    public static String summarize(String originalText, int summarySize){
        summarizer = new SimpleSummariser();
        summaryText = summarizer.summarise(originalText, summarySize);
        return summaryText;
    }//end method
    
    /**
	 * Method to display banner on cmd line
	 */
	public static void showBanner(){
		System.out.println("//----------------------------------------------------------------------------//");
		System.out.println("\tText Summarizer v-1.0.0, 08/10/2015");
		System.out.println("\t\t\tAuthor: Rushdi Shams");
		System.out.println("\tUSAGE: java -jar textsummary-1.0.0.jar directorypath/ [digit]");
		System.out.println("\tUSAGE: [digit]: number of summary sentences to be generated.");
		System.out.println("//----------------------------------------------------------------------------//");
	}
    
    public static void main(String[] args){
    	showBanner();
    	
    	File folder = new File(args[0]);
    	if(!folder.isDirectory()){
    		System.out.println("Input must be a directory.");
    		System.exit(1);
    	}
		File[] listOfFiles = folder.listFiles();
		int summaryLength = Integer.parseInt(args[1]);
		
		for(int i = 0; i < listOfFiles.length; i ++){
			
			String fileContent = "";
			try {
				fileContent = FileUtils.readFileToString(new File (listOfFiles[i].getAbsolutePath()));
			} catch (IOException e) {
				logger.info("Error in reading the text file\n");
			}
			String summary = summarize(fileContent, summaryLength).trim();
			summary = summary.replaceAll("\r\n", " ");
			TextContent t = new TextContent(); //creating TextContent object
			t.setText(summary); //setting the text
			t.setSentenceBoundary(); //detecting the sentence boundaries
			String[] array = t.getSentence();//getting the sentences in an array
			
			String summaryToBeWritten = "";
			for (String str : array){
				summaryToBeWritten += str +"\n";
			}
			
			File summaryFile = new File(FilenameUtils.getFullPath(listOfFiles[i].toString()) + listOfFiles[i].getName() +  "-automatic.txt");
			try {
				FileUtils.write(summaryFile, summaryToBeWritten);
			} catch (IOException e) {
				logger.info("Error in writing summary");
			}
		}
    }
}//end class