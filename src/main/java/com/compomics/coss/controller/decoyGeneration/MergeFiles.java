/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.decoyGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class MergeFiles {
    private final File fileOriginal;
    private final File fileTobeApended;
    
       public MergeFiles(File file1, File file2) {

       this.fileOriginal=file1;
       this.fileTobeApended=file2;
       
    }

    public void Merge() throws InterruptedException {
        BufferedWriter bw = null;
        BufferedReader br = null;
       // String filename1, filename2;
       boolean isFormatSame=false;
       boolean isFormatCorrect=false;
       
       if((this.fileOriginal.getName().endsWith("mgf") || this.fileOriginal.getName().endsWith("msp")) && (this.fileTobeApended.getName().endsWith("mgf") || this.fileTobeApended.getName().endsWith("msp")) ){
           isFormatCorrect=true;
       }
    
       
       if((this.fileTobeApended.getName().endsWith("msp") && this.fileOriginal.getName().endsWith("msp")) || (this.fileTobeApended.getName().endsWith("mgf") && this.fileOriginal.getName().endsWith("mgf")) ){
           isFormatSame=true;
       }
    
       if(isFormatSame && isFormatCorrect){
           
    
        try {
            // configureReadWriter();
            br = new BufferedReader(new FileReader(this.fileTobeApended));
            bw = new BufferedWriter(new FileWriter(this.fileOriginal, true));

            String line = br.readLine();
            int count=1;
            while (line != null) {
                bw.write(line + "\n"); //wr.write(spectrum, bw);              
                line = br.readLine();

                if(line!=null && line.contains("Name")){
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    Thread.sleep(1);
                    count++;
                    System.out.print("Number of spectrum apended: " + Integer.toString(count));                    
                }
            }


        } catch (IOException ex) {
            Logger.getLogger(com.compomics.coss.controller.decoyGeneration.MergeFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(com.compomics.coss.controller.decoyGeneration.MergeFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    }
}
