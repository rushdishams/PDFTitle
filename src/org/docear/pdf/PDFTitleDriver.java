package org.docear.pdf;

import java.io.File;
import java.io.IOException;

/**
 * Class to change titles of PDF files based on their contents
 * API used: docear
 * @version 0.1.0, 21/07/2015
 * @author rushdi.shams, Sustainalytics
 *
 */
public class PDFTitleDriver {

	/**
	 * @param args The 0th index contains the folder of PDFs
	 * 
	 */
	public static void main(String[] args) {

//		File directory = new File("C:/Users/rushdi.shams/eclipse/workspace/PDFTitle/test");
		File directory = new File(args[0]);
		//terminate if the user provides anything that is not directory
		if(!directory.isDirectory()){
			System.out.println("You must provide a directory");
			System.exit(1);
		}
		
		File[] files = directory.listFiles();
		String title = "";
		int number = 1;
		
		for (File file: files){
			if (!file.toString().endsWith(".pdf")){
				continue;
			}
			
			System.out.println("===" + file.getName() + "===");
			PdfDataExtractor extractor = new PdfDataExtractor(file);
			try {
				title = extractor.extractTitle();
			} catch (IOException e) {
				System.out.println("Error fetching Title");
			}
			
			if (title != null) {			
				
				System.out.println("Title: " + title);
				File fileWithTitle = new File(directory.getPath() + "/" + title + ".pdf");
				if(!fileWithTitle.exists()){
					file.renameTo(fileWithTitle);
				}
				else{

					fileWithTitle = new File(directory.getPath() + "/" + title + "-" + number + ".pdf");
					number++;
					file.renameTo(fileWithTitle);
				}
				
			}//check for null title
		}//end looping through files
	}//end method
}//end class