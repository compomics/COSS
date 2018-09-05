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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class RandomMzIntShift extends GenerateDecoyLib {

    public RandomMzIntShift(File f) {
        super(f);
    }

    @Override
    public File Generate() {
        File randomMzShift = null;
        BufferedWriter bw = null;
        BufferedReader br = null;
        String filename;

        if (file.getName().endsWith("mgf")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            randomMzShift = new File(file.getParent(), filename + "_randMzshift" + ".mgf");

        } else if (file.getName().endsWith("msp")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            randomMzShift = new File(file.getParent(), filename + "_randMzshift" + ".msp");

        }

        try {
            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(randomMzShift));

            int count = 0;

            String line = br.readLine();
            List<String[]> lines = new ArrayList<>();
            List<double[][]> peaks = new ArrayList<>();

            while (line != null) {
                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");
                    lines.add(p);
                    double[][] peak  = {{Double.parseDouble(p[0]), Double.parseDouble(p[1])}};
                    peaks.add(peak);

                } else if (!lines.isEmpty() && line.equals("")) {
                    int len = peaks.size();
                    double[] originalMz = new double[len];
                    double[] originalMag = new double[len];
                    double maxMz = 0;
                    double maxInt = 0;
                    int peakCount=0;
                    for (double[][] peak : peaks) {
                        originalMz[peakCount] = peak[0][0];
                        originalMag[peakCount] = peak[0][1];
                        if (maxMz < originalMz[peakCount]) {
                            maxMz = originalMz[peakCount];
                        }
                        if (maxInt < originalMag[peakCount]) {
                            maxInt = originalMag[peakCount];
                        }
                        peakCount++;
                    }
                    peaks.clear();

                    List<Double> randomMz = new ArrayList<>();                  
                    Random r;

                    List<Integer> indexTobeRemoved=new ArrayList<>();
                    for (int a = 0; a < len; a++) {
                        double mz = originalMz[a];
                        double mag = originalMag[a];
                        double lower;
                        double upper;
                        double inc;
                        r = new Random();

                        //Generate new random mz value
                        double newMz = Double.MAX_VALUE;
                        lower = (-1) * mz;
                        upper = maxMz - mz;
                        double val;
                        while (!randomMz.contains(newMz)) {
                            inc = lower + (r.nextDouble() * (upper - lower));
                            val=mz + inc;
                            newMz = Math.round(val*10000.0)/10000.0;
                            
                            randomMz.add(newMz);
                        }

                        //Generate new random intensity
                        double newMag;
                        lower = (-1) * mag;
                        upper =  (maxInt - mag)/2.0;
                        inc = lower + (r.nextDouble() * (upper - lower));
                        val=mag + inc;
                        newMag = Math.round(val);
                       
                        
                        //assinge new mz and intensity values to original lines
                        if(newMag<=0){
                            indexTobeRemoved.add(a);
                        }
                        
                        String[] l = lines.get(a);
                        l[0]=Double.toString(newMz);
                        l[1]=Double.toString(newMag);
                        lines.set(a, l);
                       
                    }
                    randomMz.clear();
                    int toberemoved=indexTobeRemoved.size();
                    for(int q=0;q<toberemoved;q++){
                        lines.remove(q);
                    }
                    String numpeaks="Num peaks: " + Integer.toString(lines.size());
                    bw.write(numpeaks + "\n");
                    for (String[] s : lines) {
                        int ll = s.length;
                        String tempS="";
                        for(int q=0;q<ll;q++){
                            tempS+=s[q] + "\t";
                        }
                        bw.write(tempS + "\n");
                    }
                    bw.write("\n");
                    lines.clear();                   
                    

                }else if(line.contains("Num")){
                    
                } else {
                    bw.write(line + "\n");
                    if (line.startsWith("Name")) {
                        line+="_decoy";
                        System.out.println("Current spectrum index :  " + Integer.toString(count));
                        count++;
                    }
                }

                line = br.readLine();

            }

        } catch (IOException ex) {
            Logger.getLogger(com.compomics.coss.controller.decoyGeneration.RandomMzIntShift.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(com.compomics.coss.controller.decoyGeneration.RandomMzIntShift.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return randomMzShift;
    }

}
