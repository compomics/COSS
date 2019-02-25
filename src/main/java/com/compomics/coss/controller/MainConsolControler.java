package com.compomics.coss.controller;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigHolder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.compomics.coss.model.ConfigData;
import com.compomics.coss.model.MatchedLibSpectra;
import com.compomics.ms2io.Spectrum;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Controller class to run the project from command line
 *
 * @author Genet
 */
public class MainConsolControler implements UpdateListener {

    /**
     * @param args the command line arguments
     */
    // static ConfigHolder config = new ConfigHolder();
    private static final Logger LOG = Logger.getLogger(MainFrameController.class);
    static ConfigData configData;
    static Dispartcher dispatcher;
    List<ComparisonResult> result = new ArrayList<>();
    int cutoff_index_1percent;
    int cutoff_index_5percent;

    /**
     * the main method
     *
     * @param args
     */
    public void startRunning(String[] args) {
        try {
            
          
            configData = new ConfigData();

            //Load user inputs from properties file
            loadData(args);
            //validate user input configData
            List<String> valdMsg = validateSettings();

            if (!valdMsg.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (String validationMessage : valdMsg) {
                    message.append(validationMessage).append(System.lineSeparator());
                }
                LOG.info("Validation errors" + message.toString());
            } else {
                //Read spectral configData both target and db spectra
                configReader();

                if ((configData.getExpSpectraIndex() != null || configData.getEbiReader() != null) && configData.getSpectraLibraryIndex() != null) {

                    startMatching();
                   
                    ImportExport exp=new ImportExport(result, configData);
                    exp.saveResult_CL(1);
                }

            }

        } catch (Exception ex) {
            LOG.info(null + " " + ex);
        }

    }

    /**
     * Method to load setting configData from config.properties file
     */
    private void loadData(String[] ipArgs) {
        //Reading User inputs and set to config configData 
        int lenArgs=ipArgs.length;
        if(lenArgs<2 && lenArgs > 5){
            System.out.println("At least two prameters has to be provided: Target spectrum and Library file \n max. number of argument is five");
            System.exit(1);
        }

        //Scoring function
        configData.setScoringFunction(ConfigHolder.getInstance().getInt("matching.algorithm"));
        configData.setIntensityOption(3);
        configData.setMsRobinOption(0);
        
        
        File fileQuery = new File(ipArgs[0]);
        File fileLib = new File(ipArgs[1]);
        configData.setSpecLibraryFile(fileLib);
        configData.setExperimentalSpecFile(fileQuery);
        //

        //MS instrument based settings
        
        configData.setPrecTol(ConfigHolder.getInstance().getDouble("precursor.tolerance")/1000000);
        configData.setfragTol(ConfigHolder.getInstance().getDouble("fragment.tolerance"));
        configData.setMaxPrecursorCharg(ConfigHolder.getInstance().getInt("max.charge"));

        if(lenArgs>2){        
            double pcTol=Double.parseDouble(ipArgs[2]);
            if(pcTol<0){
                System.out.print("Make sure the precursor tolerance value is correct. \n it should be given in ppm");
            }
            configData.setPrecTol(pcTol);
        
        }
        
        if(lenArgs>3){        
            double frTol=Double.parseDouble(ipArgs[3]);
            if(frTol>0){
                System.out.print("Make sure the fragment tolerance value is correct. \n it should be given in Da");
            }
            configData.setPrecTol(frTol);
        
        }
        
        if(lenArgs>4){        
            double charge=Double.parseDouble(ipArgs[4]);
            if(charge<0){
                System.out.print("invalid charge value");
                System.exit(1);
            }
            configData.setPrecTol(charge);
        
        }
        
        //Preprocessing settings
        boolean applyFilter = false;
        boolean applyTransform = false;
        boolean removePCM = false;
        int useFilter = ConfigHolder.getInstance().getInt("noise.filtering");
        int useTransform = ConfigHolder.getInstance().getInt("transformation");
        int removePrecursor = ConfigHolder.getInstance().getInt("precursor.peak.removal");
        if (useFilter == 1) {
            applyFilter = true;
        }

        if (useTransform == 1) {
            applyTransform = true;
        }

        if (removePrecursor == 1) {
            removePCM = true;
        }

        configData.applyFilter(applyFilter);
        configData.setFilterType(useFilter);
        configData.setCutOff(ConfigHolder.getInstance().getInt("cut.off"));
        configData.setIsPCMRemoved(removePCM);
        configData.applyTransform(applyTransform);
        configData.setTransformType(useTransform);
        configData.setMassWindow(ConfigHolder.getInstance().getInt("mass.window"));

    }

    private void configReader() {
        LOG.info("Configuring Spectrum Reader ....");
        ConfigSpecReaders cfReader = new ConfigSpecReaders(configData);
        cfReader.startConfig();

    }

    /**
     * initiated by start button and it starts the search process
     */
    private void startMatching() {

      //  List<ComparisonResult> result = null;
        LOG.info("COSS version 1.0");
        LOG.info("Query spectra: " + configData.getExperimentalSpecFile().toString());
        LOG.info("Library: " + configData.getSpecLibraryFile().toString());
        LOG.info("Search started ");

        dispatcher = new Dispartcher(this.configData, this, LOG);
        result = dispatcher.dispatch();
        if (configData.isDecoyAvailable() && result != null) {
            validateResult();
            LOG.info("Number of validated identified spectra: " + Integer.toString(result.size()));
        } else {
            LOG.info("No decoy spectra found in library");
        }

    }
    
      /**
     * start search against decoy database
     */
    List<ComparisonResult> validatedRes;
    public void validateResult() {
        Validation validate = new Validation();
        List<ComparisonResult> validatedRes=validate.validate(result, 0.01);
        
        //cutoff_index_1percent = validate.validate(result, 0.01);
        //cutoff_index_5percent = validate.validate(result, 0.05);

    }

    /**
     * Method to validate the settings read from the file
     *
     * @return
     */
    private List<String> validateSettings() {
        List<String> validationMessages = new ArrayList<>();

        String fileExtnTar = configData.getExperimentalSpecFile().getName();
        String fileExtnDB = configData.getSpecLibraryFile().getName();
                
        if (!configData.getSpecLibraryFile().exists()) {
            validationMessages.add("Database spectra file not found");
        } else if (!fileExtnDB.endsWith(".mgf") && !fileExtnDB.endsWith(".msp")) {
            validationMessages.add(" Database Spectra file typenot valid");
        }
        if (!configData.getExperimentalSpecFile().exists()) {
            validationMessages.add("Target spectra file not found");
        } else if (!fileExtnTar.endsWith(".mgf") && !fileExtnTar.endsWith(".msp")&& !fileExtnTar.endsWith(".mzML")
                && !fileExtnTar.endsWith(".mzXML") && !fileExtnTar.endsWith(".ms2")) {
            validationMessages.add(" Targer Spectra file type not valid");
        }

        if (configData.getPrecTol() < 0.0) {
            validationMessages.add("Please provide a positive precursor tolerance value.");
        }

        if (configData.getMaxPrecursorCharg() < 0.0) {
            validationMessages.add("Please provide a positive precursor charge value.");
        }

        if (configData.getfragTol() < 0.0) {
            validationMessages.add("Please provide a positive fragment tolerance value.");
        }

        return validationMessages;

    }

    @Override
    public void updateprogress(int taskCompleted) {

        final double PERCENT = 100.0 / (double) configData.getExpSpectraIndex().size();

        int v = (int) (taskCompleted * PERCENT);
        System.out.print(Integer.toString(v) + "%" + "  ");

    }
}
