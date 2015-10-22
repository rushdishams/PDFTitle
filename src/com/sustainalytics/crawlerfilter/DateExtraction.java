package com.sustainalytics.crawlerfilter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

/**
 * Class to extract the modification/creation/custom date from PDFs
 * 
 * CHANGE: A directory can have multiple file types. Only PDFs will be explored.
 * 
 * @author Rushdi Shams
 * @version 0.2.0 October 21 2015
 *
 */

public class DateExtraction {
	public static void main(String[] args) {
		File folder = new File(args[0]);
		if(!folder.isDirectory()){
			System.out.println("Input must be a directory.");
			System.exit(1);
		}
		File[] listOfFiles = folder.listFiles();
		for(int i = 0; i < listOfFiles.length; i ++){
			String extension = FilenameUtils.getExtension(listOfFiles[i].getAbsolutePath());
			if(StringUtils.equalsIgnoreCase(extension, "pdf")){
				System.out.println(listOfFiles[i].getName() + "\t" + extractDate(listOfFiles[i]));
			}
			else{
				System.out.println("Not PDF");
			}
		}

	}/*End of driver method*/

	/**
	 * This method extracts creation date/ custom date of a PDF file
	 * @param file is a File object
	 * @return String that contains the creation date/ custom date of the PDF
	 */
	public static String extractDate(File file){
		PDDocument document = null;
		boolean isDamaged = false; //to deal with damaged pdf
		String creationDateMetaData = "";
		try {
			document = PDDocument.load(file.toString());
			/*If the PDF file is not damanged --->*/
			if(!isDamaged){
				/*...but the file is encrypted --->*/
				if (document.isEncrypted()){
					//					System.out.println("File " + file.getName() + "is encrypted. Trying to decrypt...");
					try {
						/*...then decryptt it --->*/
						document.decrypt("");
						document.setAllSecurityToBeRemoved(true);
						//						System.out.println("File " + file.getName() + "successfully decrypted!");
					} catch (CryptographyException e) {
						System.out.println("Error decrypting file " + file.getName());
						isDamaged = true;
					}

				}/*<--work around to decrypt an encrypted pdf ends here*/

				/*Metadata extraction --->*/
				PDDocumentInformation info = document.getDocumentInformation();

				/*We are only interested in date data--->*/
				Calendar calendar = info.getModificationDate();
				int modYear = 0, modMonth = 0, modDate = 0;

				if(calendar != null){
					modYear = calendar.get(Calendar.YEAR);
					modMonth = calendar.get(Calendar.MONTH) + 1;
					modDate = calendar.get(Calendar.DATE);

				}/*<---Date data extraction complete*/

				if(modYear != 0){
					creationDateMetaData = modYear + "-" + modMonth + "-" + modDate;
					return creationDateMetaData;
				}

				calendar = info.getCreationDate();
				int creationYear = 0, creationMonth = 0, creationDate = 0;
				if(calendar != null){
					creationYear = calendar.get(Calendar.YEAR);
					creationMonth = calendar.get(Calendar.MONTH) + 1;
					creationDate = calendar.get(Calendar.DATE);

				}/*<---Date data extraction complete*/

				/*If creation date is not empty --->*/
				if(creationYear != 0){
					creationDateMetaData = creationYear + "-" + creationMonth + "-" + creationDate;
					return creationDateMetaData;
				}//<--- creation date found and the date part of the title is generated
				/*No creation date is found --->*/
				//				else{
				SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
				Date customDate = null;
				/*But we have custom date some times --->*/
				try {
					customDate = dateFormatter.parse(info.getCustomMetadataValue("customdate"));
				} catch (ParseException e) {
					System.out.println("Error parsing date from custom date");
				}
				calendar = Calendar.getInstance();
				calendar.setTime(customDate);
				if(calendar != null){
					creationYear = calendar.get(Calendar.YEAR);
					creationMonth = calendar.get(Calendar.MONTH) + 1;
					creationDate = calendar.get(Calendar.DATE);

				}/*<---Date data extraction complete from customdate*/
				if(creationYear != 0){
					creationDateMetaData = creationYear + "-" + creationMonth + "-" + creationDate;
					return creationDateMetaData;
				}
				//				}//<--- work around if no creation date is found

			} /*<--- Good to know that the PDF was not damaged*/
		} catch (IOException e) { /*If the PDF was not read by the system --->*/
			System.out.println("Error processing file " + file.getName());
			/*... then maybe it is damaged*/
			isDamaged = true;
		}
		finally{
			try {
				/*If the file was good, not damaged, then please close it --->*/
				if(!isDamaged){
					document.close();
					//					System.out.println("File " + file.getName() + " is closed successfully!");
				}
			} catch (IOException e) {
				System.out.println("Error closing file " + file.getName());				}
		} /*<--- PDF closing done!*/
		return creationDateMetaData;
	}/*<--- End method to extract creation date of the PDF*/
}
