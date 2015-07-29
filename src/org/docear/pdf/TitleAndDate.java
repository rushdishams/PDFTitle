package org.docear.pdf;

import java.io.File;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

/**
 * This program takes a PDF file name as input and uses DocEar API to generate its title.
 * The title is then added with the creation date of the PDF.
 * @author rushdi.shams, 28/07/2015
 * @version 0.1.0
 *
 */
public class TitleAndDate {
	/*logger variable*/
	private static Logger logger = Logger.getLogger("MyLog");
	
	/**
	 * This method gets rid of the characters from the file title that are not
	 * supported by Windows in a file name
	 * @param title String
	 * @return String without invalid characters in Windows file name system
	 */
	public static String cleanTitle(String title){
		return title.replaceAll("[^a-zA-Z0-9.-]", "_");
	}

	/**
	 * This method extracts title from a PDF using DocEar API
	 * @param file File object
	 * @return String which is the title found by the API
	 */
	public static String extractTitle(File file){
		PdfDataExtractor extractor = new PdfDataExtractor(file);
		/*If no title is found then it will be titled as unnamed*/
		String title = "unnamed";
		try {
			title = extractor.extractTitle();
			/*Send the title to get rid of unwanted Windows file system characters in it*/
			title = cleanTitle(title);
			logger.info("Title extracted from " + file.getName() + "-->" + title);
		} catch (IOException e) {
			logger.info("Error extracting title from " + file.getName());
			/*If we cannot extract title, no need to proceed*/
			System.exit(1);
		}
		return title;
	}

	/**
	 * This method extracts creation date of a PDF file
	 * @param file is a File object
	 * @return String that contains the creation date of the PDF
	 */
	public static String extractCreationDate(File file){
		PDDocument document = null;
		boolean isDamaged = false; //to deal with damaged pdf
		String creationDateMetaData = "";
		try {
			document = PDDocument.load(file.toString());
			/*If the PDF file is not damanged --->*/
			if(!isDamaged){
				/*...but the file is encrypted --->*/
				if (document.isEncrypted()){
					logger.info("File " + file.getName() + "is encrypted. Trying to decrypt...");
					try {
						/*...then decryptt it --->*/
						document.decrypt("");
						document.setAllSecurityToBeRemoved(true);
						logger.info("File " + file.getName() + "successfully decrypted!");
					} catch (CryptographyException e) {
						logger.info("Error decrypting file " + file.getName());
						isDamaged = true;
					}

				}/*<--work around to decrypt an encrypted pdf ends here*/
				/*Metadata extraction --->*/
				PDDocumentInformation info = document.getDocumentInformation();
				/*We are only interested in date data--->*/
				Calendar calendar = info.getCreationDate();
				int creationYear = 0, creationMonth = 0, creationDate = 0;
				if(calendar != null){
					creationYear = calendar.get(Calendar.YEAR);
					creationMonth = calendar.get(Calendar.MONTH);
					creationDate = calendar.get(Calendar.DATE);

				}/*<---Date data extraction complete*/
				
				creationDateMetaData = creationYear + "-" + creationMonth + "-" + creationDate;
			} /*<--- Good to know that the PDF was not damaged*/
		} catch (IOException e) { /*If the PDF was not read by the system --->*/
			logger.info("Error processing file " + file.getName());
			/*... then maybe it is damaged*/
			isDamaged = true;
		}
		finally{
			try {
				/*If the file was good, not damaged, then please close it --->*/
				if(!isDamaged){
					document.close();
					logger.info("File " + file.getName() + " is closed successfully!");
				}
			} catch (IOException e) {
				logger.info("Error closing file " + file.getName());				}
		} /*<--- PDF closing done!*/
		return creationDateMetaData;
	}/*<--- End method to extract creation date of the PDF*/

	/**
	 * This method combines the title of the PDF and the creation date
	 * @param title is a String
	 * @param creationDate is a String
	 * @return a String that is the final title of the PDF
	 */
	public static String makeTitle(String title, String creationDate){
		return title + "_" + creationDate;
	}

	/**
	 * This method renames the given PDF
	 * @param file is a File object
	 * @param finalTitle is a String
	 */
	public static void renameFile(File file, String finalTitle){
		File fileWithTitle = new File(file.getParentFile().getAbsolutePath()+ "/" + finalTitle + ".pdf");
		if(!fileWithTitle.exists()){
			logger.info("Renaming " + file.getName() + "-->" + fileWithTitle.getName());
			logger.info("\n\n");
			file.renameTo(fileWithTitle);
		}
	}

	/**
	 * Method to initiate logger
	 * @param file is a File object. The log file will be placed in this file's folder
	 */
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
	 * Driver method for the class
	 * @param args contains the file path and name
	 */
	public static void main(String[] args) {


		File file = new File(args[0]);
		initiateLogger(file);
		if(!args[0].endsWith(".pdf")){
			logger.info("Input must be a PDF. Exiting!");
			System.exit(1);
		}
		String title = extractTitle(file);
		String creationDate = "";
		creationDate = extractCreationDate(file);
		String finalTitle = makeTitle(title, creationDate);
		renameFile(file, finalTitle);

	}

}/*End of class*/
