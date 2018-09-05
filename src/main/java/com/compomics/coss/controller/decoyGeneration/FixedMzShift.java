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
public class FixedMzShift extends GenerateDecoyLib {

    public FixedMzShift(File f) {
        super(f);
    }

    @Override
    public File Generate() {
        File mzShift = null;
        BufferedWriter bw = null;
        BufferedReader br = null;
        String filename;

        if (file.getName().endsWith("mgf")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            mzShift = new File(file.getParent(), filename + "_mzshift" + ".mgf");

        } else if (file.getName().endsWith("msp")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            mzShift = new File(file.getParent(), filename + "_mzshift" + ".msp");

        }

        try {
            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(mzShift));

            int count = 1;

            String line = br.readLine();
            while (line != null) {

                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");
                    double pcm = Double.parseDouble(p[0]) + 50;
                    p[0] = Double.toString(pcm);
                    int len = p.length;
                    line = "";
                    for (int s = 0; s < len; s++) {
                        line += p[s] + " ";
                    }

                   
                }
                else if(line.startsWith("Name")){
                    line+="_decoy";
                    System.out.println("Current spectrum index :  " + Integer.toString(count));
                    count++;
                }

                bw.write(line + "\n");
                line = br.readLine();

            }

        } catch (IOException ex) {
            Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedMzShift.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedMzShift.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return mzShift;
    }

}
