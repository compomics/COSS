package com.compomics.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class TestApending {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            // TODO code application logic here

      
             
            File fileOriginal = new File("C:/human_hcd_2020_target.msp/.msp");
            File fileModified = new File("C:/human_hcd_2020_target.msp/human_hcdAall_2020_target_proc.msp");
            br = new BufferedReader(new FileReader(fileOriginal));
            bw = new BufferedWriter(new FileWriter(fileModified));
            String line = br.readLine();
            String name = "";
            String mw = "";
            String sequence="";
            String annotation = "";

            //int cnt = 0;
            String[] str_arr = null;
            while (line != null) {
                
            //Adding decoy tag to Name field and correcting mz values of less than 1 to 1 for MsPepsearch library (binary) generation

                if (!line.equals("") && Character.isDigit(line.charAt(0))) {
                    line.trim().replaceAll("\\s{2,}", " ");
                    
                    str_arr = line.split("\t");
                    double pm = Double.parseDouble(str_arr[0]);
                    double pi = Double.parseDouble(str_arr[1]);
                    pm = Math.round(pm * 10000) / 10000.0;
                    if (pm < 1) {
                        pm = 1;
                    }
                    pi = Math.round(pi * 10) / 10.0;
                    annotation = str_arr[2]; // "\"?\"";
                    
                    // annotation = "\"" + "teststring"+"\"" + "\"";
//                    annotation = annotation.replace("\"", "");
//                    annotation = "\"" + annotation + "\"";

                    line = Double.toString(pm) + "\t" + Double.toString(pi) + "\t" + annotation;
                    bw.write(line);
                    bw.write("\n");
                    line = br.readLine();                   

                }                

//                else if (line.startsWith("Name:")) {
//                    name = line;
//                    line = br.readLine();
//                    
//                } 
//                else if (line.startsWith("MW:")) {
//                    mw = line;
//                    line = br.readLine();
//                    
//                }
//                else if (line.startsWith("Comment")) {
//                    String newLine = name;
////                    if (line.contains("Decoy") || line.contains("decoy")) {
//                        int index_isert = name.indexOf("/");
//                        newLine = name.substring(0, index_isert - 1) + "_decoy" + name.substring(index_isert);
////                    }
//                    bw.write(newLine);
//                    bw.write("\n");
//                    
//                    if (!mw.equals("")) {
//                        bw.write(mw);
//                        bw.write("\n");
//                    }
//
//                    bw.write(line);
//                    bw.write("\n");
//                    line = br.readLine();
//                }

                else {
                    bw.write(line);
                    bw.write("\n");
                    line = br.readLine();
                }





//Library conversion for TMT
//                if(line.startsWith("Name:")){
//                    name=line;
//                    line = br.readLine();
//                    continue;
//                }else if(line.contains("MW:")){
//                    mw=line;
//                    line = br.readLine();
//                    continue;
//                            
//                }
//                else if(line.contains("LibID:")){
//                    line = br.readLine();
//                    continue;
//                }else if(line.contains("PrecursorMZ:")){
//                    line = br.readLine();
//                    continue;
//                }else if(line.contains("Status:")){
//                    line = br.readLine();
//                    continue;
//                }else if(line.contains("FullName:")){
//                    line = br.readLine();
//                    continue;
//                }else if(line.contains("Comment:")){
//                    line = line.replaceAll("DECOY", "");
//                    int index_isert = name.indexOf("/");
//                        
//                    sequence=name.substring(name.indexOf(":") +1, index_isert);
//                    sequence = sequence.replaceAll("[^A-Z]", "");
//                    if(line.contains("Remark")){
//                        
//                        name = "Name: " + sequence+ "_decoy" + name.substring(index_isert);
//                        line+= "_decoy";
//                    }else{
//                        name = "Name: " +sequence+name.substring(index_isert);
//                    }
//                    bw.write(name);
//                    bw.write("\n");
//                    bw.write(mw);
//                    bw.write("\n");
//                    bw.write(line);
//                    bw.write("\n");
//                    line = br.readLine();
//                    continue;
//                }else{
//                     bw.write(line);
//                     bw.write("\n");
//                     line = br.readLine();
//                }
              
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestApending.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestApending.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(TestApending.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
