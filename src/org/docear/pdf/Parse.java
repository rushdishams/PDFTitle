package org.docear.pdf;
import com.foxit.gsdk.PDFException;
import com.foxit.gsdk.PDFLibrary;
import com.foxit.gsdk.pdf.PDFDocument;
import com.foxit.gsdk.pdf.PDFPage;
import com.foxit.gsdk.pdf.PDFTextPage;
import com.foxit.gsdk.pdf.Progress;
import com.foxit.gsdk.utils.FileHandler;



public class Parse {
	private static String license_id = "dxertz7Zw21Um6c6ULChTZ5xnEShZIp39tPmaI4KJwOgrxlJ/KCnhQ==";
	private static String unlockCode = "8f3o18GNtRkNBDdqtFZS8bag26vXuHklApT0VbrYMFmi5Tn8cP2aKFdzLC30QRvjfyg6bxC69l4OoWcIjB76xzSs7B2+u3q3Mt4Cf4ORp7OPcP3AgWwNfIt+tYeqCILLPsM+/QcLywUd4HOR+iE94sQgNE4iOq74vy/tmbU4LUkxwvN3PrzLYNhuzzza3Xor31wnNbASpXv7gam6YHWEv98Gr1zyofqe9J5A8QyX8NC8I8j43DTLcSme1LhXoM3cAeQu+deRg+ZeKgXjz9oyDD6MvuDJtFSGXUjoLdEbKHM7bldpnj/MruIdVJs5He1AHUreavlCnc43AuJ/x+Ki4e2hLu7dClcDBE0HpURIc25tBaxjjFG7pbsZyKvbHmE3+7xTr9G6L9vSzaEvpC6WU1Mo4Xj0+bov7tYq25KPX7lX9ULghLsXdqtoaLY9w8J86j48xR/fkSvH8ibWtVNE94tLu8U15gcPCZnimEbEcWBi/4QA266WGY2/b5PaQlTgT13GXSRONJRPnmCxhbDjlbB38VJ24XxkiZ+XTTQg2Gw1mUJndVrEJPdU5YXTW4LjTEAHW4fhcTxJaEGYGSaJtZsmmTBBV7hYNUnElQAvnVsq0fQT/fKKY9wKPMDmIIVgMGLQJgTFmrW9oG//uaMBynZHKFOusgXhgP2wSkTEQOnkwzHh1cHK4K6h3KNzLN53Iro6IySzwhTihFVhPXEPx5i60mfYLaJf4ZVvmA3qaJ5oWe4+71EpYnJkNvslOrVnnISvxS1l39ckHwr0ptYYDAeZ6T1lVnY4ghn+hKeBYvY53G/T7joHN11aJ9l0S1u4Yk/xuvHj0wuhcx0rb7ePAycfbi3nmQqJII9o1VuNnmlky30XPylUC4iS104+vpnyI+dxDWj3HRMAPFrmrtIJTJIElV1VeoWHFmEhQawu36g/X5qcxGxuNhdvUmG25D8vITyJW+JRRB7X8AfybwL+dTBnYk6QU837Wq7voveGLQQdys2oW+0iW9Tt91IQJIM6kt9KOL6xTDagXBq9T2EF4qxlMdv1Dsq/870nrwhqXyaSHYLi+Hh41oPN31oM2mQMRju5sMEB/F5OMDscA2JUFB+cxVUaStEfQm+Kwm1ULEPJOu2OZIMaKXU7adi2Qd2w/XSKwzjPABgC2TxLltoncHcL2b7vCsHjquWHzbj/xqVtJcqNUTFKcs0ODGHOh/9T9VuMfVnCkeLvqbztY7SaIkNkEMKKtBPnn8OdlxnfFBqcxZh1f5Jq43Aa2IP+o+xZfUbCt0vjLWBHJ9BgU3MMgzPft74934hl5LCflb4wT1P9aZcO/88aG/33ORF4uUM37flA4bkU+mPctQVfA81aHmfwxjHTxCggqfdvpzK3dvnE+W42HMBYILP4kWs4CK++iY3qkydeSdnEexy1dmfGcFsbWfG0yS5Jlo9wROh9S5Wsm5bWCQsTntWEfA0UCDHYWh+1gZ5umTNSjtGCa4M48E1Hu1ohXAePgNc7SA9441azHGTAVkp1cAEBNezHfj+qxCmK/E8RJLuqo8zai2Wnmep9kh7uOzEfxvYMUGDZaoYsgcjiZLYxpEw4ojjmkkm2c9q3EWnK6INI3Th/1sljQ/gvLmAUuzyeGQnL8wjdzw5RXa2Y/3bUDT2c7OCoYifA7cW95WBy7ZRSzZMrfTUX7p7PrI7+2iRw+1cQRVDPupWQ1nKgdedFaqv2YxVidnAQV8D208blV05hIuLyL1KnhQxmtRUI55SDgQ==";
	private int memorySize = 10 * 1024 * 1024;
	private boolean scaleable = true;
	static {
		try {
			String arch = System.getProperty("os.arch");
			if (arch.contains("64"))
				System.load(System.getProperty("user.dir")
						+ "//lib//fsdk_java_win64.dll");
			else {
				System.load(System.getProperty("user.dir")
						+ "//lib//fsdk_java_win32.dll");
			}
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Native code library failed to load.\n" + e);
			System.exit(1);
		}
	}

	public void initLib() {
		// The implementation of initiating SDK library manager and applying
		PDFLibrary pdfLibrary = PDFLibrary.getInstance();
		try {
			pdfLibrary.initialize(memorySize, scaleable);
			pdfLibrary.unlock(license_id, unlockCode);
			// System.out.println("Success: Initialize and unlock the library.");

		} catch (PDFException e) {
			e.printStackTrace();
			// System.out.println("Failed to initlize or unlock the library" +
			// e.getLastError());
			System.exit(1);// exit
		}
	}
	public String pdfOperation(String file) throws PDFException {
		// The implementation of pdf operation goes here
		PDFPage page = null;
		String myString = "";
		PDFDocument pdfDocument = null;
		FileHandler fileHandler = null;

		try {
			fileHandler = FileHandler.create(file, FileHandler.FILEMODE_READONLY);
			pdfDocument = PDFDocument.open(fileHandler, null);
			int cnt = pdfDocument.countPages();
			for(int i = 0; i < cnt; i++){
				page = pdfDocument.getPage(i);
				Progress parserProgress = null;
				if(page != null)
					parserProgress = page.startParse(PDFPage.PARSEFLAG_NORMAL);
				int ret_prog = Progress.TOBECONTINUED;
				while (ret_prog == Progress.TOBECONTINUED){
					ret_prog = parserProgress.continueProgress(30);
				}

				PDFTextPage textPage = PDFTextPage.create(page);
				myString += textPage.getChars(0, textPage.countChars());
				parserProgress.release();
				pdfDocument.closePage(page);
			}
			pdfDocument.close();
			fileHandler.release();
		}
		catch (PDFException e) {
			e.printStackTrace();
		}
		
		return myString;

	}
	public void release() {
		PDFLibrary pdfLibrary = PDFLibrary.getInstance();
		pdfLibrary.destroy();
	}
}
