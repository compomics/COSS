package com.compomics.coss.model;

import com.compomics.ms2io.model.IndexKey;
import java.io.File;
import com.compomics.ms2io.controller.SpectraReader;
import java.util.List;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;

/**
 * this class holds the configuration data for spectral comparison
 *
 * @author Genet
 */
public class ConfigData {

    //User input experimental and library spectra file   
    private File experimentalSpecFile = null;
    private File specLibraryFile = null;
    private File experimentalSpecDirectry = null;

    //scoring function
    private int scoringFun;//index

    //instrument settings
    private double precTol;
    private double fragTol;
    private int MSRobinOption;
    private int intensityOption;

    //preprocessing settings
    private boolean isTransform;
    private int tranformType;
    private boolean isFilter;
    private int filterType;
    private boolean isPCMremoved;
    private int massWindow;
    private int cutOff;

    //parameters to be assigned based on the imput
    private List<IndexKey> expIndex = null;
    private List<IndexKey> libIndex = null;
    private SpectraReader rdExperimental = null;
    private SpectraReader rdLibrary = null;
    private JMzReader ebiReader = null;
    
    String expSpecFormat="";
    int totalExpSpectraCount=0;
    private boolean isDecoySpecAvailable;

    //Decoy database index and reader
    private List<IndexKey> decoyDBIndex;
    private SpectraReader rdDecoyDB;

    //initial log text
    String logText = "";
   private String percolator_path="";
    //User input Search Settings
    /**
     * constructor
     */
    public ConfigData() {

    }

      public int getExpSpecCount() {
        return this.totalExpSpectraCount;
    }

    public void setExpSpecCount(int size) {
        this.totalExpSpectraCount = size;
    }
    
 
    
      public String getPercolatorPath() {
        return this.percolator_path;
    }

    public void setPercolatorPath(String path) {
        this.percolator_path = path;
    }
    
    public int getMassWindow() {
        return this.massWindow;
    }

    public void setMassWindow(int mwindow) {
        this.massWindow = mwindow;
    }

    public boolean applyTransform() {
        return this.isTransform;
    }

    public void applyTransform(boolean t) {
        this.isTransform = t;
    }

    public boolean applyFilter() {
        return this.isFilter;
    }

    public void applyFilter(boolean t) {
        this.isFilter = t;
    }

    public int getTransformType() {
        return this.tranformType;
    }

    public void setTransformType(int t) {
        this.tranformType = t;
    }

    public int getFilterType() {
        return this.filterType;
    }

    public void setFilterType(int t) {
        this.filterType = t;
    }

    public boolean getIsPCMRemove() {
        return this.isPCMremoved;
    }

    public void setIsPCMRemoved(boolean t) {
        this.isPCMremoved = t;
    }

    public int getCutOff() {
        return this.cutOff;
    }

    public void setCutOff(int t) {
        this.cutOff = t;
    }

    /**
     * return experimental spectra
     *
     * @return
     */
    public List<IndexKey> getExpSpectraIndex() {
        return this.expIndex;
    }

    public SpectraReader getExpSpecReader() {
        return this.rdExperimental;
    }

    public void setExpSpecReader(SpectraReader rd) {

        this.rdExperimental = rd;

    }

    public void setLibSpecReader(SpectraReader rd) {
        this.rdLibrary = rd;
    }

    public SpectraReader getLibSpecReader() {
        return this.rdLibrary;
    }

    public List<IndexKey> getSpectraLibraryIndex() {
        return this.libIndex;
    }

    public void setSpectralLibraryIndex(List<IndexKey> indx) {
        this.libIndex = indx;
    }

    /**
     * set experimental spectra index
     *
     * @param indx
     */
    public void setExpSpectraIndex(List<IndexKey> indx) {
        this.expIndex = indx;
    }

 

    public int getScoringFunction() {
        return this.scoringFun;
    }

    public void setScoringFunction(int matchAlgorithm) {
        this.scoringFun = matchAlgorithm;
    }

    public File getExperimentalSpecFolder() {
        return this.experimentalSpecDirectry;
    }

    public void setExperimentalSpecFolder(File directory) {
        this.experimentalSpecDirectry = directory;
    }

    public File getExperimentalSpecFile() {
        return this.experimentalSpecFile;
    }

    public File getSpecLibraryFile() {
        return this.specLibraryFile;
    }

    public void setExperimentalSpecFile(File file) {
        this.experimentalSpecFile = file;
    }

    public void setSpecLibraryFile(File file) {
        this.specLibraryFile = file;
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

    public void setEbiReader(JMzReader reader) {
        this.ebiReader = reader;
    }

    public JMzReader getEbiReader() {
        return this.ebiReader;
    }

    public void setDecoyDBIndexList(List<IndexKey> indx) {
        this.decoyDBIndex = indx;
    }

    public List<IndexKey> getDecoyDBIndexList() {
        return this.decoyDBIndex;
    }

    public void setDecoyDBReader(SpectraReader rd) {
        this.rdDecoyDB = rd;
    }

    public SpectraReader getDecoyDBReader() {
        return this.rdDecoyDB;
    }

    public void setLogText(String log) {
        this.logText = log;
    }

    public String getLogText() {
        return this.logText;
    }

    
    public void setExpFileformat(String format) {
        this.expSpecFormat = format;
    }

    public String getExpFileformat() {
        return this.expSpecFormat;
    }
    public boolean isDecoyAvailable() {
        return this.isDecoySpecAvailable;
    }

    public void setDecoyAvailability(boolean decoySpec) {
        this.isDecoySpecAvailable = decoySpec;
    }

  

}
