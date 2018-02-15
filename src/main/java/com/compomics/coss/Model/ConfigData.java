package com.compomics.coss.Model;


import com.compomics.ms2io.IndexKey;
import java.io.File;
import com.compomics.ms2io.SpectrumReader;
import java.util.List;
/**
 *this class holds the configuration data for spectral comparison
 * @author Genet
 */
public class ConfigData {
    
    File experimentalSpecFile;
    File specLibraryFile;
    List<IndexKey> expIndex;
    List<IndexKey> libIndex;
    SpectrumReader rdExperimental;
    SpectrumReader rdLibrary;
    
    String outputPath;
    int ScoringFun;//index
    double precTol;
    double fragTol;
    int MSRobinOption;
    int intensityOption; 
    int maxPrecursorCharge;
    
  
    
    
   /**
    * constructor
    */
    public ConfigData(){
        precTol = 0;
        fragTol = 0.5;
        MSRobinOption = 0; // 0-sqrt(Intensities), 1-Intensities
        intensityOption = 0; // 0-Summed Up 1-Multiply intensities   
        ScoringFun=0;
        
    }

    
    /**
     * return experimental spectra
     * @return 
     */
    public List<IndexKey> getExpSpectraIndex() {
        return this.expIndex;
    }

    public SpectrumReader getExpSpecReader(){
        return this.rdExperimental;
    }
    
    public void setExpSpecReader(SpectrumReader rd){
        this.rdExperimental=rd;
    }
    
    public void setLibSpecReader(SpectrumReader rd){
        this.rdLibrary=rd;
    }
   
    public SpectrumReader getLibSpecReader(){
        return this.rdLibrary;
    }
    
   public List<IndexKey>  getSpectraLibraryIndex(){
       return this.libIndex;
   }
   
   public void setSpectralLibraryIndex(List<IndexKey>  indx){
       this.libIndex=indx;
   }

    /**
     * set experimental spectra index
     * @param indx 
     */
    public void setExpSpectraIndex(List<IndexKey>  indx) {
        this.expIndex = indx;
    }


    /**
     * set spectra library
     * @return 
     */
     public int getMaxPrecursorCharg() {
        return this.maxPrecursorCharge;
    }
    
     
     /**
      * set maximum precursor charge
      * @param maxPrecCharge the value to be set to max precursor charge
      */
    public void setMaxPrecursorCharg(int maxPrecCharge) {
        this.maxPrecursorCharge=maxPrecCharge;
    }
    
     public int getScoringFunction() {
        return this.ScoringFun;
    }
    
    public void setScoringFunction(int matchAlgorithm) {
        this.ScoringFun=matchAlgorithm;
    }
       
    public String getOutputFilePath() {
        return this.outputPath;
    }

    public void setOutputFilePath(String filename) {
        this.outputPath=filename;
    }
    
    public File getExperimentalSpecFile() {
        return this.experimentalSpecFile;
    }

    public File getSpecLibraryFile() {
        return this.specLibraryFile;
    }

    public void setExperimentalSpecFile(String filename){
        this.experimentalSpecFile= new File(filename);
        
    }
      public void setSpecLibraryFile(String filename){
        this.specLibraryFile= new File(filename);
        
    }
    public int getIntensityOption() {
        return this.intensityOption;
    }

    public void setIntensityOption(int sp) {
        this.intensityOption = sp;
    }

    public int getMsRobinOption() {
        return this.MSRobinOption;
    }

    public void setMsRobinOption(int sp) {
        this.MSRobinOption = sp;
    }

    public void setPrecTol(double prcTol) {
        this.precTol = prcTol;
    }

    public double getPrecTol() {
        return this.precTol;
    }

    public void setfragTol(double frTol) {
        this.fragTol = frTol;
    }

    public double getfragTol() {
        return this.fragTol;
    }

    
}
