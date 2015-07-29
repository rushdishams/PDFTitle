package org.docear.pdf;
// SimpleFileChooser.java
// A simple file chooser to see what it takes to make one of these work.
//
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

public class SimpleFileChooser extends JFrame {

   public SimpleFileChooser() {
    super("Choose a directory of PDF Files");
    setSize(350, 200);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    Container c = getContentPane();
    c.setLayout(new FlowLayout());
    JButton dirButton = new JButton("Pick Dir");
    final JLabel statusbar = 
                 new JLabel("Output of your selection will go here");

    // Create a file chooser that allows you to pick a directory
    // rather than a file
    dirButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = chooser.showOpenDialog(SimpleFileChooser.this);
        if (option == JFileChooser.APPROVE_OPTION) {
          statusbar.setText("You opened " + ((chooser.getSelectedFile()!=null)?
                            chooser.getSelectedFile().toString():"nothing"));
          
          File directory = new File(chooser.getSelectedFile().toString());
  		//terminate if the user provides anything that is not directory
  		if(!directory.isDirectory()){
  			System.out.println("You must provide a directory");
  			System.exit(1);
  		}
  		
  		File[] files = directory.listFiles();
  		String title = "";
  		int number = 1;
  		
  		int i = 1;
  		for (File file: files){
  			
  			statusbar.setText("Processing " + i + "/" + files.length + " files" );
  			c.add(statusbar);
  			i++;
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
        }
        else {
          statusbar.setText("You canceled.");
        }
      }
    });
    c.add(dirButton);
    c.add(statusbar);
  }

  public static void main(String args[]) {
    SimpleFileChooser sfc = new SimpleFileChooser();
    sfc.setVisible(true);
  }
}