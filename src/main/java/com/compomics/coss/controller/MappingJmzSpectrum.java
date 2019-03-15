/*
 This class convertes  ebi spectrum object jmzspec to MSIO2 spectrum
 */
package com.compomics.coss.controller;

import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;

/**
 *
 * @author Genet
 */
public class MappingJmzSpectrum {

    String fileExtn;
    private Spectrum expSpec;

    /**
     *
     * @param fileType experimental spectrum file type
     */
    public MappingJmzSpectrum(String fileType) {
        this.fileExtn = fileType;
    }

    public Spectrum getMappedSpectrum(uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec) {

        this.expSpec = new Spectrum();
        double mz, intensity;
        ArrayList<Peak> peakList;

        Iterator entriesIterator;
        Map map = jmzSpec.getPeakList();
        Set entries = map.entrySet();
        entriesIterator = entries.iterator();
        peakList = new ArrayList<>();

        while (entriesIterator.hasNext()) {

            Map.Entry mapping = (Map.Entry) entriesIterator.next();
            mz = (double) mapping.getKey();
            intensity = (double) mapping.getValue();
            peakList.add(new Peak(mz, intensity));

        }

        expSpec.setPeakList(peakList);
        expSpec.setNumPeaks(peakList.size());
        
        getHeader(jmzSpec);
        return expSpec;
    }

    private void getHeader(uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec) {

        switch (this.fileExtn) {
            case "mzML":
            case "mzXML":
                if (jmzSpec.getAdditional().getCvParams().isEmpty()) {
                    System.out.println("Additional CV parameters missing for the spectrum");
                    break;
                } else {
                    List<CvParam> params = jmzSpec.getAdditional().getCvParams();
                    for (CvParam p : params) {
                        if (p.getName().equals("base peak m/z")) {
                            double pcmass = Double.parseDouble(p.getValue());
                            expSpec.setPCMass(pcmass);

                        } else if (p.getName().equals("base peak intensity")) {
                            double pcInt = Double.parseDouble(p.getValue());
                            expSpec.setPCIntesity(pcInt);
                        } else if (p.getName().equals("spectrum title")) {
                            String title = p.getValue();
                            expSpec.setTitle(title);

                        } else if (p.getName().equals("charge state")) {
                            String charge = p.getValue();
                            expSpec.setCharge(charge);

                        } else if (p.getName().equals("highest observed m/z")) {
                            Double maxMz = Double.parseDouble(p.getValue());
                            expSpec.setMaxMz(maxMz);

                        } else if (p.getName().equals("lowest observed m/z")) {
                            Double minMz = Double.parseDouble(p.getValue());
                            expSpec.setMinIntensity(minMz);

                        }
                    }
                    double[][] peakList = expSpec.getPeakListDouble();
                    double[] intArray=peakList[1];
                    Arrays.sort(intArray);
                    double minInt = intArray[0];
                    double maxInt = intArray[intArray.length-1];
                    
                    
                    expSpec.setMaxIntensity(maxInt);
                    expSpec.setMinIntensity(minInt);
                }
                break;

            case "ms2":
                if (jmzSpec.getAdditional().getParams().isEmpty()) {
                    System.out.println("Additional parameters missing for the spectrum");
                    break;
                } else {
                    List<Param> temp = jmzSpec.getAdditional().getParams();
                    for (Param p : temp) {
                        if (p.getName().equals("BPM")) {
                            double pcmass = Double.parseDouble(p.getValue());
                            expSpec.setPCMass(pcmass);

                        } else if (p.getName().equals("BPI")) {
                            double pcInt = Double.parseDouble(p.getValue());
                            expSpec.setPCIntesity(pcInt);

                        } else if (p.getName().equals("RTime")) {
                            double rtTime = Double.parseDouble(p.getValue());
                            expSpec.setRtTime(rtTime);
                        }

                    }
                    double[][] peakList = expSpec.getPeakListDouble();
                    double[] arr=peakList[1];
                    Arrays.sort(arr);
                    double min = arr[0];
                    double max = arr[arr.length-1];
                    expSpec.setMaxIntensity(max);
                    expSpec.setMinIntensity(min);
                    
                    arr=peakList[0];
                    Arrays.sort(arr);
                    min = arr[0];
                    max = arr[arr.length-1];
                    expSpec.setMaxIntensity(max);
                    expSpec.setMinIntensity(min);

                }
                break;

            case "mzdata":
                expSpec.setPCMass(0);
                expSpec.setPCIntesity(0);

                break;

            case "dta":
                expSpec.setPCMass(0);
                expSpec.setPCIntesity(0);
                break;

            case "pkl":
                expSpec.setPCMass(0);
                expSpec.setPCIntesity(0);
                break;
        }

    }

}
