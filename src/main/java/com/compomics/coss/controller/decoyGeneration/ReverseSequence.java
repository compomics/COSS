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
            reverseSequence = new File(file.getParent(), filename + "_mzshift" + ".mgf");
            fileExtension = "mgf";

        } else if (file.getName().endsWith("msp")) {
            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
            reverseSequence = new File(file.getParent(), filename + "_mzshift" + ".msp");
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
            int charge = 1;
            Map<Integer, List<String>> mods = new HashMap<>();
            boolean isAnnotated = false;
            String spectrum = "";

            FragmentIon ions;
            while (line != null) {
                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");                  
                    Peak peak = new Peak(Double.parseDouble(p[0]), Double.parseDouble(p[1]), p[2]);
                    peaks.add(peak);

                } else if (!lines.isEmpty() && line.equals("") && !peaks.isEmpty() && !"".equals(spectrum)) {
                    //reverse sequence keeping the last amino acid position unmodified
                    String reversed_seq = reverse(sequence.substring(0, sequence.length()-1));
                    reversed_seq+=sequence.charAt(sequence.length()-1);
                    ions=new FragmentIon(sequence, mods);                  
                    Map frag_ion= ions.getFragmentIon();
                    
                    for (Peak p : peaks) {
                        //copy peak of the library to decoy initially
                        Peak peak_decoy = p;
                        String ann = p.getPeakAnnotation();

                        //alter decoy peak m/z value if annotaion not empty and annotation doesn't contain NH3 and H2O loss
                        if (!"".equals(ann) && !p.getPeakAnnotation().contains("NH") && !ann.contains("H2O")) {
                            String strAnn = ann.substring(0, ann.indexOf("/"));//sub-string before the first occurence of '/'that contains ion type
                            strAnn = strAnn.trim(); //remove white spaces, leading and trailing
                            strAnn = strAnn.replaceAll("[^abyABY0-9]", "");//remove characters except letters a,b,y and numbers                          

                            double mass_frag = (double) frag_ion.get(strAnn);//return mass of srtAnn ion
                            
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
                    mods = new HashMap<Integer, List<String>>();
                    spectrum += line + " _Decoy" + "\n";
                    charge = Integer.parseInt(line.substring(line.indexOf("Charge") + 6, line.indexOf("Charge") + 7));
                    String mods_str = line.substring(line.indexOf("Mods") + 3, line.indexOf(" ") - 1);
                    String[] strAr = mods_str.split("/");
                    int num_mods=strAr.length-1; //first string represents number of modifications
                    if(num_mods == Integer.parseInt(strAr[0])){
                        List l=new ArrayList<String>();
                        for(int p=0;p<num_mods;p++){
                            strAr[p]=strAr[p].replaceAll("\\s", ""); //remove all white space
                            
                            String[] m = strAr[p].split(",");
                            int pos=Integer.parseInt(m[0]);
                            if(!mods.containsKey(pos)){
                                l=new ArrayList<String>();
                                l.add(m[2]);                                
                                mods.put(pos, l);
                            }else{
                                l=new ArrayList<String>();
                                l=mods.get(pos);
                                l.add(m[2]);
                                mods.put(pos, l);                                
                            }
                            
                        }
                    }
                    

                } else if (line.contains("Charge")) {
                    spectrum += line + "\n";
                    charge = Integer.parseInt(line.replaceAll("\\D+", ""));

                } else if ((line.startsWith("Name") && fileExtension.equals("msp")) || (line.startsWith("TITLE") && fileExtension.equals("mgf"))) {
                    spectrum += line + "\n";
                    sequence = line;
                    sequence = sequence.replaceAll("[^AC-IK-NP-TVWY]", "");
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
