package org.docear.pdf;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.docear.pdf.options.Configuration;
import org.docear.pdf.options.OptionParser;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		PdfDataExtractor extractor = new PdfDataExtractor(new File("05.pdf"));
		String title = extractor.extractTitle();
		if (title != null) {
			System.out.println("Title: " + title);
		}

	}
}
