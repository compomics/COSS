package com.compomics.coss.controller.featureExtraction;

import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.log4j.Logger;

/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaks implements Features {

    private final int topN;
    private double windowMassSize = 100;    
    private final Spectrum spectrum;
    private final Logger LOGGER;

    /**
     * This constructs an object to filter out spectra based on 100Da window and
     * it selects X peaks with highest intensities for each window.
     *
     * @param spec is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     * @param windowmassSize moving window size to filter spectrum
     * @param log
     */
    public DivideAndTopNPeaks(Spectrum spec, int topN, double windowmassSize, Logger log) {
        this.spectrum = spec;
        this.topN = topN;
        this.windowMassSize=windowmassSize;        
        this.LOGGER=log;
        
    }

  
   
    @Override
    public ArrayList<Peak>  getFeatures() {
        ArrayList<Peak> cPeaks = new ArrayList<>();
        ArrayList<Peak> filteredPeaks=new ArrayList<>();
        int total_peak_length = 0;
        double startMz = 0;
        double limitMz = 0;
     
        try {
            
            Collections.sort(spectrum.getPeakList());
            startMz = spectrum.getMinMZ();
            limitMz = startMz + windowMassSize;
            total_peak_length = spectrum.getPeakList().size();
            
          
            for (int k = 0; k < total_peak_length ; k++) {

              //  errorIndx="spectrum peakList index: " + Integer.toString(k)+ ", " + Integer.toString(spectrum.getPeakList().size())+ ", " + Integer.toString(total_peak_length);
                double tmpMZ = spectrum.getPeakList().get(k).getMz();
                
                //Peak tmpPeak = spectrum.getPeakMap().get(tmpMZ);
                if (tmpMZ < limitMz) {
                   
                    cPeaks.add(spectrum.getPeakList().get(k));
                    
                } else if(cPeaks!=null && !cPeaks.isEmpty()) {
                    //cPeaks already sort as spectrum.PeakList is sorted
                    
                   //int tmp_num = topN; //old select first peaks
                   int selected_peaks_length=cPeaks.size();
                    if (topN > selected_peaks_length) {
                         //tmp_num = selected_peaks_length;
                         filteredPeaks.addAll(cPeaks);//selectt best;adds all if top N greater than peaks available
                    }
                    
//                    //new else ... selecting most intense peaks
                    else{
                        while(selected_peaks_length > topN){                            
                            double min=cPeaks.get(0).getIntensity();
                            int pos=0;
                            for(int i=1; i<selected_peaks_length; i++){
                                if(min>cPeaks.get(i).getIntensity()){
                                    min=cPeaks.get(i).getIntensity();
                                    pos=i;
                                }
                            }
                            cPeaks.remove(pos);    
                            selected_peaks_length=cPeaks.size();                            
                        }
                        filteredPeaks.addAll(cPeaks);
                        
                    }
                    
                    //old for loop to add to filtered peaks
//                    for (int num = 0; num < tmp_num; num++) {
//                        //errorIndx="spectrum filtered peaks index: " + Integer.toString(num)+ ", " + Integer.toString(cPeaks.size());
//                        Peak tmpCPeakToAdd = cPeaks.get(num);
//                        filteredPeaks.add(tmpCPeakToAdd);
//                    }
                    cPeaks.clear();
                    limitMz += windowMassSize;
                    k = k - 1;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            String srtLen = Integer.toString(total_peak_length);
            String srtMzMin = Double.toString(startMz);
            LOGGER.info("Len, minMz and Temp : " + srtLen + ", " + srtMzMin );//  + ": Error Location " + errorIndx);           
            throw ex;
        }

        
        
        if (!cPeaks.isEmpty()) {
            
            //Top N selected peaks
            int selected_peaks_length=cPeaks.size();
             while(selected_peaks_length > topN){                            
                double min=cPeaks.get(0).getIntensity();
                int pos=0;
                for(int i=1; i<selected_peaks_length; i++){
                    if(min>cPeaks.get(i).getIntensity()){
                        min=cPeaks.get(i).getIntensity();
                        pos=i;
                    }
                }
                cPeaks.remove(pos);    
                selected_peaks_length=cPeaks.size();                            
            }
            filteredPeaks.addAll(cPeaks);
            
            //First N selected peaks
            //            //Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
            //            int tmp_num = topN;
            //            if (topN > cPeaks.size()) {
            //                tmp_num = cPeaks.size();
            //            }
            //            for (int num = 0; num < tmp_num; num++) {
            //                Peak tmpCPeakToAdd = cPeaks.get(num);
            //                filteredPeaks.add(tmpCPeakToAdd);
            //            }

            
        }

        return filteredPeaks;
    }

}
