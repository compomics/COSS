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
import java.util.HashMap;
import java.util.Map;

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
            reverseSequence = new File(file.getParent(), filename + "_revSequence" + ".mgf");
            fileExtension = "mgf";

        } else if (file.getName().endsWith("msp")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            reverseSequence = new File(file.getParent(), filename + "_revSequence" + ".msp");
            fileExtension = "msp";

        }

        try {
            int count = 1;

            String sequence = "";
            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(reverseSequence));
            String line = br.readLine();
            List<String[]> lines = new ArrayList<>();
            List<Peak> peaks = new ArrayList<>();
            List<Peak> peaks_d = new ArrayList<>();
            int spectrum_charge = 1;
            Map<Integer, List<String>> modifications = new HashMap<>();    
         
            while (line != null) {
                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");
                    Peak peak = new Peak(Double.parseDouble(p[0]), Double.parseDouble(p[1]), p[2]);
                    peaks.add(peak);

                } else if (line.equals("") && !peaks.isEmpty()) {
                    //reverse sequence keeping the last amino acid position unmodified
                    String reversed_seq = reverse(sequence.substring(0, sequence.length() - 1));
                    reversed_seq += sequence.charAt(sequence.length() - 1);
                    FragmentIon ions = new FragmentIon(sequence, modifications);
                    Map frag_ion_actual = ions.getFragmentIon();
                    
                    ions = new FragmentIon(reversed_seq, modifications);
                    Map frag_ion_reverse = ions.getFragmentIon();
                    peaks_d=getDecoyPeak(peaks, frag_ion_actual, frag_ion_reverse);
                    peaks.clear();                
                    Collections.sort(peaks_d);
                    for (Peak p : peaks_d) {
                        //all peaks and new line after them
                        String p_list = (Double.toString(p.getMz()) + "\t" + Double.toString(p.getIntensity()) + "\t" + p.getPeakAnnotation() + "\n");
                        bw.write(p_list);
                    }
                    
                   ////writing the whole p_list to destination file
                    bw.write("\n");
                    peaks_d.clear();

                } else if (line.contains("Comment")) {
                    bw.write(line + " _Decoy" + "\n");
                    
                    modifications = new HashMap<Integer, List<String>>();                     
                    modifications = getModifications(line);   

                } else if (line.contains("Charge")) {
                     bw.write(line + "\n");
                    spectrum_charge = getCharge(line);

                } else if ((line.startsWith("Name") && fileExtension.equals("msp")) || (line.startsWith("TITLE") && fileExtension.equals("mgf"))) {
                     bw.write(line + "\n");
                    sequence = line.substring(line.indexOf(":") + 1, line.indexOf("/"));
                   
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
                    Thread.sleep(1);
                    System.out.print("Current decoy spectrum index generated :  " + Integer.toString(count));
                    count++;
                } else {
                     bw.write(line + "\n");
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
