package com.compomics.coss.controller.decoyGeneration;

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
import com.compomics.ms2io.Peak;
import java.util.Collections;

/**
 *
 * @author Genet
 */
public class ReverseSequence extends GenerateDecoyLib {

    File reverseSequence;

    public ReverseSequence(File f, org.apache.log4j.Logger log) {
        super(f, log);
    }

    @Override
    public File generate() {
        BufferedWriter bw = null;
        BufferedReader br = null;
        String filename;
        String fileExtension = "";

        if (file.getName().endsWith("mgf")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            reverseSequence = new File(file.getParent(), filename + "_mzshift" + ".mgf");
            fileExtension = "mgf";

        } else if (file.getName().endsWith("msp")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            reverseSequence = new File(file.getParent(), filename + "_mzshift" + ".msp");
            fileExtension = "msp";

        }

        try {
            int count = 1;

            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(reverseSequence));
            String line = br.readLine();
            List<String[]> lines = new ArrayList<>();
            List<Peak> peaks = new ArrayList<>();
            List<Peak> peaks_d = new ArrayList<>();
            int charge = 1;
            String mods = "";
            boolean isAnnotated = false;
            String spectrum = "";

            while (line != null) {
                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");                  
                    Peak peak = new Peak(Double.parseDouble(p[0]), Double.parseDouble(p[1]), p[2]);
                    peaks.add(peak);

                } else if (!lines.isEmpty() && line.equals("") && !peaks.isEmpty() && !"".equals(spectrum)) {
                    for (Peak p : peaks) {
                        //copy peak of the library to decoy initially
                        Peak peak_decoy = p;
                        String ann = p.getPeakAnnotation();

                        //alter decoy peak m/z value if annotaion not empty and annotation doesn't contain NH3 and H2O loss
                        if (!"".equals(ann) && !p.getPeakAnnotation().contains("NH") && !ann.contains("H2O")) {
                            String strAnn = ann.substring(0, ann.indexOf("/"));//sub-string before the first occurence of '/'that contains ion type
                            strAnn = strAnn.trim(); //remove white spaces, leading and trailing
                            strAnn = strAnn.replaceAll("[^abyABY0-9]", "");//remove characters except letters a,b,y and numbers                          

                            double mass_frag = 0;//get mass for specific fragment ion ..... key value hash map   map(strAnn)                               
                            mass_frag += (p.getMz() - mass_frag) / (double) charge;
                            peak_decoy.setMz(mass_frag); //update decoy_decoy peak mz value with the new 
                            isAnnotated = true;
                        }

                        peaks_d.add(peak_decoy);
                        //dealing with redundant peaks should be considered here

                    }
                    peaks.clear();
                    if (!isAnnotated) {
                        log.info("Library has one or more un-annotated spectrum and stoped generating decoy library");
                        return null;
                    }
                    Collections.sort(peaks_d);
                    for (Peak p : peaks_d) {
                        //all peaks and new line after them
                        spectrum += (Double.toString(p.getMz()) + "\t" + Double.toString(p.getIntensity()) + "\t" + p.getPeakAnnotation() + "\n");
                    }
                    spectrum += "\n"; //end of spectrum blank line
                    bw.write(spectrum);//writing the whole spectrum to destination file

                    spectrum = ""; // clear spectrum

                } else if (line.contains("Comment")) {
                    spectrum += line + " _Decoy" + "\n";
                    charge = Integer.parseInt(line.substring(line.indexOf("Charge") + 6, line.indexOf("Charge") + 7));
                    mods = line.substring(line.indexOf("Mods") + 3, line.indexOf("Fullname") - 1);

                } else if (line.contains("Charge")) {
                    spectrum += line + "\n";
                    charge = Integer.parseInt(line.replaceAll("\\D+", ""));

                } else if ((line.startsWith("Name") && fileExtension.equals("msp")) || (line.startsWith("TITLE") && fileExtension.equals("mgf"))) {
                    spectrum += line + "\n";
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    Thread.sleep(1);
                    System.out.print("Current decoy spectrum index generated :  " + Integer.toString(count));
                    count++;
                } else {
                    spectrum += line + "\n";

                }

                line = br.readLine();

            }

        } catch (IOException ex) {
            Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedMzShift.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReverseSequence.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedMzShift.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return reverseSequence;
    }

}
