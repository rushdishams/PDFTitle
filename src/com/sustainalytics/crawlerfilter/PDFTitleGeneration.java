package com.sustainalytics.crawlerfilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.foxit.gsdk.PDFException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;

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
 * The title is then added with the creation date or custom date of the PDF. The contents
 * of the PDF are then extracted using Apache PDFBox and their digest is created using 
 * Apache Codec. Finally these three information are combined to generate the title. The PDF
 * file is renamed only if no other PDF with the same name exists. The .txt file is saved 
 * only if the PDF file is renamed and no other .txt file with the same name exists. 
 * 
 * 
 * @author rushdi.shams, 08/09/2015
 * @version 2.0.0
 * 
 * CHANGE:
 * - This version makes a title from pdf and rename the pdf and text file containing the pdf content
 * - it removes duplicate files in a folder system as well
 *
 */
public class PDFTitleGeneration {
	/*logger variable*/
	private static Logger logger = Logger.getLogger("MyLog");
	private static final int MAX_CHAR = 50;

	/**
	 * This method gets rid of the characters from the file title that are not
	 * supported by Windows in a file name. The method also chops off the title
	 * so that it stays within MAX_CHAR. If the title length is less than
	 * MAX_CHAR, then the maximum character is set to the length of the title.
	 * @param title String
	 * @return String without invalid characters in Windows file name system
	 */
	public static String cleanTitle(String title){
		title = title.replaceAll("[^a-zA-Z0-9.-]", "_").replace("_", " ");
		int maxLength = (title.length() <= MAX_CHAR)?title.length():MAX_CHAR;

		if(title.length() > MAX_CHAR){
			logger.info("The title generated was more than MAXIMUM CHARACTER. Chopping off!");
		}
		return title.substring(0, maxLength);
//		return title;
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
					creationMonth = calendar.get(Calendar.MONTH) + 1;
					creationDate = calendar.get(Calendar.DATE);

				}/*<---Date data extraction complete*/

				/*If creation date is not empty --->*/
				if(creationYear != 0){
					creationDateMetaData = creationYear + "-" + creationMonth + "-" + creationDate;
				}//<--- creation date found and the date part of the title is generated
				/*No creation date is found --->*/
				else{
					SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
					Date customDate = null;
					/*But we have custom date some times --->*/
					try {
						customDate = dateFormatter.parse(info.getCustomMetadataValue("customdate"));
					} catch (ParseException e) {
						logger.info("Error parsing date from custom date");
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
					}
				}//<--- work around if no creation date is found

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
	public static String makeTitle(String title, String creationDate, String digest){
		return title + "_" + creationDate + "_" + digest;
	}

	/**
	 * This method renames the given PDF
	 * @param file is a File object
	 * @param finalTitle is a String
	 */
	public static void renameFile(File pdfFile, File textFile, String finalTitle){
		String newTitle = pdfFile.getParentFile().getAbsolutePath()+ "/" + finalTitle;
		File pdfFileWithTitle = new File(newTitle + ".pdf");
		File textFileWithTitle = new File(newTitle + ".txt");
		if(!pdfFileWithTitle.exists()){
			logger.info("Renaming " + pdfFile.getName() + "-->" + pdfFileWithTitle.getName());
			logger.info("\n\n");
			pdfFile.renameTo(pdfFileWithTitle);
			textFile.renameTo(textFileWithTitle);
		}
		else{
			logger.info("Renaming failed. File " + pdfFileWithTitle + " already exists");
			try {
				logger.info("Deleting " + pdfFile.getName());
				FileUtils.forceDelete(pdfFile);
				FileUtils.forceDelete(textFile);
			} catch (IOException e) {
				logger.info(pdfFile.getName() + "could not be force deleted");
			}
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
	 * Method to extract text from PDF using Apache PDFBox
	 * @param file is a File object that indicates the PDF file
	 * @return the extracted content of the PDF file as String
	 */
	public static String extractPDFText(File file){
		PdfDataExtractor extractor = new PdfDataExtractor(file);
		String content = "";
		try {
			content = extractor.extractPlainText();
			logger.info("PDF text extracted from " + file.getName() + "\n");
		} catch (IOException e) {
			logger.info("Error extracting PDF text from " + file.getName() + "\n");
		}
		return content;
	}// Method to extract and return PDF content has ended.
	
	/**
	 * Method to extract text from PDF using Apache PDFBox
	 * @param file is a File object that indicates the PDF file
	 * @return the extracted content of the PDF file as String
	 */
	public static String extractFoxitText(String file){
		Parse test = new Parse();
		test.initLib();
		String PDFContent = "";
		try {
			PDFContent = test.pdfOperation(file);
		} catch (PDFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		test.release();
		return PDFContent;
	}// Method to extract and return PDF content has ended.
	
	public static String extractPDFExtremeText(String file){
		Document pdf = PDF.open(file);
	    StringBuilder text = new StringBuilder();
	    pdf.pipe(new OutputTarget(text));
	    try {
			pdf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return text.toString();
	}
	
	public static String extractTikaText(String file){
		InputStream is = null;
		ContentHandler contenthandler = null;
	    try {
	      is = new FileInputStream(file);
	      contenthandler = new BodyContentHandler();
	      Metadata metadata = new Metadata();
	      PDFParser pdfparser = new PDFParser();
	      pdfparser.parse(is, contenthandler, metadata, new ParseContext());
	      
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	    finally {
	        if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    }
	    return contenthandler.toString();
	}
	
	public static String extractITextText(String pdf){
		PdfReader reader = null;
		try {
			reader = new PdfReader(pdf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextExtractionStrategy strategy;
        String text = "";
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            try {
				strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
				text += strategy.getResultantText();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        reader.close();
        
        return text;
	}
	
	/**
	 * Method to generate and return digest of the PDF contents
	 * @param content in String which is actually the digest of the file
	 * @return String, the digest of the input file
	 */
	public static String getDocumentDigest(String content){
		logger.info("Returning digest");
		return DigestUtils.sha1Hex(content);
	}
	
	/**
	 * Method to write text files (the content of the PDF files)
	 * @param String fileAbsolutePath is the directory of the PDF files
	 * @param String finalTitle is the new name of the PDF
	 * @param String content of the PDF file in String format 
	 */
	public static void writeTextFile(String fileAbsolutePath, String finalTitle, String content){
		File textFile = new File(fileAbsolutePath + "/" + finalTitle + ".txt");
		if(!textFile.exists()){
			logger.info("Creating a text file-->" + finalTitle);
			logger.info("\n\n");
			try {
				FileUtils.writeStringToFile(textFile, content);
			} catch (IOException e) {
				logger.info("Error creating file " + textFile);
			}
		}
		else{
			logger.info("File " + textFile.getName() + " already exists");
		}
	}
	
	/**
	 * Method to display banner on cmd line
	 */
	public static void showBanner(){
		System.out.println("//----------------------------------------------------------------------------//");
		System.out.println("\tPDF Renaming and Duplicate Removal Filter v-2.0.0 08/09/2015");
		System.out.println("\t\t\tAuthor: Rushdi Shams");
		System.out.println("\tUSAGE: java -jar pdftitle-2.0.0.jar directorypath/filename.pdf");
		System.out.println("//----------------------------------------------------------------------------//");
	}

	/**
	 * Driver method for the class
	 * @param args contains the file path and name
	 */
	public static void main(String[] args) {
		showBanner();
		File pdfFile = new File(args[0]);
		initiateLogger(pdfFile);
	
		File textFile = new File(FilenameUtils.removeExtension(args[0]) + ".txt");
		String title = extractTitle(pdfFile);
		String creationDate = extractDate(pdfFile);
		String content = null;
		try {
			content = FileUtils.readFileToString(textFile);
		} catch (IOException e) {
			logger.info("Error reading PDF content\n");
		}
		String digest = getDocumentDigest(content.trim());
		String finalTitle = makeTitle(title, creationDate, digest);
		renameFile(pdfFile, textFile, finalTitle);
		
	}/*End of driver method*/
}/*End of class*/
