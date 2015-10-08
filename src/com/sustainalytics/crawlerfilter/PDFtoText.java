package com.sustainalytics.crawlerfilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
 * - PDF data extraction using 5 different parser options. This functionality previously was
 * part of one filter, now separated.
 *
 */
public class PDFtoText {
	/*logger variable*/
	private static Logger logger = Logger.getLogger("MyLog");

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
			logger.info("Error in parsing with Apache PDFBox parser\n");
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
			logger.info("PDF text extracted from " + file + "\n");
		} catch (PDFException e) {
			logger.info("Error in parsing with Foxit parser\n");
		}
		test.release();
		return PDFContent;
	}// Method to extract and return PDF content has ended.

	public static String extractPDFExtremeText(String file){
		Document pdf = PDF.open(file);
		StringBuilder text = new StringBuilder();
		pdf.pipe(new OutputTarget(text));
		logger.info("PDF text extracted from " + file + "\n");
		try {
			pdf.close();
		} catch (IOException e) {
			logger.info("Error in closing with PDFExtreme parser\n");
		}
		return text.toString();
	}

	public static String extractTikaText(String file){
		InputStream is = null;
		ContentHandler contenthandler = null;
		try {
			is = new FileInputStream(file);
			contenthandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			PDFParser pdfparser = new PDFParser();
			pdfparser.parse(is, contenthandler, metadata, new ParseContext());
			logger.info("PDF text extracted from " + file + "\n");

		}
		catch (Exception e) {
			logger.info("Error in parsing with Apache Tika parser\n");
		}
		finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					logger.info("Error in closing file with Apache Tika\n");
				}
		}
		return contenthandler.toString();

	}

	public static String extractITextText(String pdf){
		PdfReader reader = null;
		try {
			reader = new PdfReader(pdf);
		} catch (IOException e) {
			logger.info("Error in reading file with iText parser\n");
		}
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		TextExtractionStrategy strategy;
		String text = "";
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			try {
				strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
				text += strategy.getResultantText();
			} catch (IOException e) {
				logger.info("Error in parsing with iText parser\n");
			}
			logger.info("PDF text extracted from " + pdf + "\n");
		}
		reader.close();

		return text;
	}

	/**
	 * Method to display banner on cmd line
	 */
	public static void showBanner(){
		System.out.println("//----------------------------------------------------------------------------//");
		System.out.println("\tPDF Converter Filter v-2.0.0, 08/09/2015");
		System.out.println("\t\t\tAuthor: Rushdi Shams");
		System.out.println("\tUSAGE: java -jar pdftotext-2.0.0.jar directorypath/filename.pdf [digit]");
		System.out.println("\tUSAGE: [digit]: 1-Apache PDFBox, 2-Foxit, 3-PDFXtreme, 4-iText, 5-Apache Tika");
		System.out.println("//----------------------------------------------------------------------------//");
	}

	/**
	 * Method to write the parsed content
	 * @param filePath is the PDF file path, String
	 * @param content is the parsed content, String 
	 */
	public static void writeParsedFile(String filePath, String content){
		String fileWithoutExtension = FilenameUtils.removeExtension(filePath);
		if(content != null){
			try {
				FileUtils.write(new File(fileWithoutExtension + ".txt"), content);
			} catch (IOException e) {
				logger.info("Error writing parsed content\n");
			}
		}
		else{
			logger.info("Nothing to write as parsed content\n");
		}
	}

	/**
	 * Driver method for the class
	 * @param args contains the file path and name
	 */
	public static void main(String[] args) {
		showBanner();
		File file = new File(args[0]);
		initiateLogger(file);

		int parserChoice = Integer.parseInt(args[1]);

		String content = null;
		if(parserChoice == 1){
			content = extractPDFText(file);
		}
		else if(parserChoice == 2){
			content = extractFoxitText(args[0]);
		}
		else if(parserChoice == 3){
			content = extractPDFExtremeText(args[0]);
		}
		else if(parserChoice == 4){
			content = extractITextText(args[0]);
		}
		else if(parserChoice == 5){
			content = extractTikaText(args[0]);
		}

		writeParsedFile(args[0], content);
	}/*End of driver method*/

}/*End of class*/
