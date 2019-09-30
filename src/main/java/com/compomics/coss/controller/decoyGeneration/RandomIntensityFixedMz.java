/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.decoyGeneration;

import com.compomics.coss.controller.UpdateListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class RandomIntensityFixedMz extends GenerateDecoy {

    File randomIntShift;

    public RandomIntensityFixedMz(File f, double fragTol, org.apache.log4j.Logger log) throws IOException {
        super(f,fragTol, log);
    }

    @Override
    public void generate() {
        BufferedWriter bw = null;
        BufferedReader br = null;
        String filename;
        String fileExtension = "";

        if (file.getName().endsWith("mgf")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            randomIntShift = new File(file.getParent(), filename + "_mzshift" + ".mgf");
            fileExtension = "mgf";

        } else if (file.getName().endsWith("msp")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            randomIntShift = new File(file.getParent(), filename + "_mzshift" + ".msp");
            fileExtension = "msp";

        }

        try {
            int count = 1;

            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(randomIntShift));
            String line = br.readLine();
            List<String[]> lines = new ArrayList<>();
            List<double[][]> peaks = new ArrayList<>();

            while (line != null) {
                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");
                    lines.add(p);
                    double[][] peak = {{Double.parseDouble(p[0]), Double.parseDouble(p[1])}};
                    peaks.add(peak);

                } else if (!lines.isEmpty() && line.equals("")) {
                    int len = peaks.size();
                    double[] originalMz = new double[len];
                    double[] originalMag = new double[len];

                    int peakCount = 0;
                    for (double[][] peak : peaks) {
                        originalMz[peakCount] = peak[0][0];
                        originalMag[peakCount] = peak[0][1];
                        peakCount++;
                    }
                    peaks.clear();

                    List<Integer> randIndx = new ArrayList<>();
                    List<Integer> originalIndx = new ArrayList<>();
                    for (int s = 0; s < len; s++) {
                        originalIndx.add(s);
                    }
                    //generate len random numbers 0 to len-1
                    while (randIndx.isEmpty() || originalIndx.equals(randIndx)) {//repeat if the original index is same as the new randomly generated index
                        for (int i = 0; i < len; i++) {
                            int indx = (int) (Math.random() * len);
                            while (!randIndx.isEmpty() && randIndx.contains(indx)) {
                                indx = (int) (Math.random() * len);
                            }
                            randIndx.add(indx);
                        }
                    }
                    for (int a = 0; a < len; a++) {
                        int indx = randIndx.get(a);
                        double mz = originalMz[a];
                        double mag = originalMag[indx];

                        String[] l = lines.get(a);
                        l[0] = Double.toString(mz);
                        l[1] = Double.toString(mag);
                        lines.set(a, l);

                    }

                    String numpeaks = "Num peaks: " + Integer.toString(lines.size());
                    bw.write(numpeaks + "\n");
                    for (String[] s : lines) {
                        int ll = s.length;
                        String tempS = "";
                        for (int q = 0; q < ll; q++) {
                            tempS += s[q] + "\t";
                        }
                        bw.write(tempS + "\n");
                    }
                    bw.write("\n");
                    lines.clear();

                } else if (line.contains("Num")) {

                } else {
                    bw.write(line + "\n");
                    if ((line.startsWith("Name") && fileExtension.equals("msp")) || (line.startsWith("TITLE") && fileExtension.equals("mgf"))) {
                        line += "_decoy";
                        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                        Thread.sleep(1);
                        System.out.print("Current decoy spectrum index generated :  " + Integer.toString(count));
                        
                        count++;
                    }
                }

                line = br.readLine();

            }

        } catch (IOException ex) {
            Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedMzShift.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(RandomIntensityFixedMz.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedMzShift.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
      
    }

}
