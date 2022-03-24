package com.compomics.coss.controller;

import com.compomics.coss.controller.decoyGeneration.*;
import com.compomics.coss.controller.SpectrumAnnotation.Annotation;
import com.compomics.coss.controller.rescoring.Rescore;
import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigHolder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.compomics.coss.model.ConfigData;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * Controller class to run the project from command line
 *
 * @author Genet
 */
public class MainConsoleController implements UpdateListener {

    /**
     * @param args the command line arguments
     */
    // static ConfigHolder config = new ConfigHolder();
    private static final Logger LOG = Logger.getLogger(MainConsoleController.class);
    static ConfigData configData;
    static Dispatcher dispatcher;
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

            System.out.println("Starting COSS-V2");
            System.out.println("Rescoring with Percolator is set on by default in this version");
            int lenArgs = args.length;
            String arg1 = args[0];
            if (lenArgs <= 1 || lenArgs > 5) {
                System.out.println("At least two prameters has to be provided: Target spectrum and Library file \n max. number of argument is five");
                System.out.println("\n\nUsage: \n");
                System.out.println("java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile \n");
                System.out.println("OR\n");
                System.out.println("java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile precursorMassTolerance(PPM) fragmentTolerance(Da.)  \n");
                System.out.println("OR\n");
                System.out.println("java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile precursorMassTolerance(PPM) fragmentTolerance(Da.) maxNumberofCharge \n");
                
                System.out.println("OR decoy spectra can be generated and appended to the given library file using the command below\n");
                System.out.println("java -jar COSS-X.Y.jar -d librarySpectraFile \n");
                System.out.println("OR spectra library file can be annotated using the command below\n");
                System.out.println("java -jar COSS-X.Y.jar -a librarySpectraFile \n");
                Runtime.getRuntime().exit(0);

            }

            if (lenArgs == 2 && (arg1.startsWith("-d"))) {
                //Generate decoy library and exit

                if (arg1.equals("-dV")) {// reverse sequence decoy generateion
                    System.out.println("Generating decoy library with fixed mz value shift");
                    generateDeoy(0, args[1]);
                    Runtime.getRuntime().exit(0);

                } else if (arg1.equals("-dR")) { //random sequence 
                    System.out.println("Generating decoy library with shuffle mz value and random intensity");
                    generateDeoy(1, args[1]);
                    Runtime.getRuntime().exit(0);
                }

            } else if (lenArgs == 3 && (arg1.startsWith("-a"))) {

                System.out.println("Annotating spectral library file...");
                double fragTol = 0.5;
                try {
                    fragTol = Double.parseDouble(args[2]);
                } catch (NumberFormatException ex) {
                    LOG.info("check the fragment tolearnce input: " + ex.toString());
                }

                annotateLibrary(fragTol, args[1]);
                Runtime.getRuntime().exit(0);

            }

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

//                    ImportExport exp = new ImportExport(result, configData);
//                    exp.saveResult_CL(2);
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
        int lenArgs = ipArgs.length;

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
        configData.setPrecTol(ConfigHolder.getInstance().getDouble("precursor.tolerance") / 1000000);
        configData.setfragTol(ConfigHolder.getInstance().getDouble("fragment.tolerance"));

        if (lenArgs == 3) {
            int matching_algorithm = Integer.parseInt(ipArgs[2]);
            if (matching_algorithm == 0 || matching_algorithm == 1) {
                configData.setScoringFunction(matching_algorithm);
            } else {
                System.out.print("scoring function input error: 0: for MsRobin, and 1 for Cosine similarity");
            }

        } else if (lenArgs == 4) {
            double pcTol = Double.parseDouble(ipArgs[3]);
            if (pcTol < 0) {
                System.out.print("Make sure the precursor tolerance value is correct. \n it should be given in ppm");
            } else {
                configData.setPrecTol(pcTol);
            }

        } else if (lenArgs == 5) {
            double frTol = Double.parseDouble(ipArgs[4]);
            if (frTol > 0) {
                System.out.print("Make sure the fragment tolerance value is correct. \n it should be given in Da");
            } else {
                configData.setPrecTol(frTol);
            }

        } else if (lenArgs == 6) {
            double charge = Double.parseDouble(ipArgs[5]);
            if (charge < 0) {
                System.out.print("invalid charge value, default charge value is set instead");
                Runtime.getRuntime().exit(0);
            } else {
                configData.setPrecTol(charge);
            }

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

        dispatcher = new Dispatcher(this.configData, this, LOG);
        result = dispatcher.dispatch();
        
        if (configData.isDecoyAvailable() && result != null) {
            validateResult();
            LOG.info("Number of validated identified spectra by COSS: " + Integer.toString(result.size()));

            LOG.info("starting rescoring the result with Percolator ... ");
            Rescore rescore = new Rescore(result);
            try {
                boolean finished = rescore.start_rescoring(configData, configData.getExperimentalSpecFile().toString());
                if (finished) {
                    LOG.info("Percolator finishes scoring and result is stored in the directory of input file ");
                } else {
                    LOG.info("Percolator exits with error. Result not rescored ");
                    if (!rescore.error_msg.isEmpty()) {
                        LOG.info(rescore.error_msg);
                    }
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

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
        validate.validate(result, 0.01);

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
        } else if (!fileExtnDB.endsWith(".mgf") && !fileExtnDB.endsWith(".msp") && !fileExtnDB.endsWith(".sptxt")) {
            validationMessages.add(" Database Spectra file typenot valid");
        }
        if (!configData.getExperimentalSpecFile().exists()) {
            validationMessages.add("Target spectra file not found");
        } else if (!fileExtnTar.endsWith(".mgf") && !fileExtnTar.endsWith(".msp") && !fileExtnTar.endsWith(".mzML")
                && !fileExtnTar.endsWith(".mzXML") && !fileExtnTar.endsWith(".ms2")) {
            validationMessages.add(" Targer Spectra file type not valid");
        }

        if (configData.getPrecTol() < 0.0) {
            validationMessages.add("Please provide a positive precursor tolerance value.");
        }

        if (configData.getfragTol() < 0.0) {
            validationMessages.add("Please provide a positive fragment tolerance value.");
        }

        return validationMessages;

    }

    @Override
    public void updateprogress(int taskCompleted, double percent) {
        int v = (int) (taskCompleted * percent);
        System.out.print("\b\b\b\b\b\b\b\b" + Integer.toString(v) + "%" + "  ");

    }

    /**
     * generate decoy library and append on the given spectral library file
     *
     * @param i : type of decoy generation technique; 0 if reverse sequence 1 is
     * random sequence
     * @param library path to library file
     */
    public void generateDeoy(int i, String library) throws IOException {

        if ("".equals(library)) {
            System.out.println("Validation errors: No spectra library has given");

        } else if (!library.endsWith(".mgf") && !library.endsWith(".msp")) {
            System.out.println("Validation errors: given spectral library file format is not supported");
        } else {

            File libFile = new File(library);
            GenerateDecoy gn;
            if (i == 0) {
                gn = new ReverseSequence(libFile, LOG);
            } else {
                gn = new RandomSequene(libFile, LOG);
            }
            gn.generate();

        }

    }

    /**
     * this function annotates a, b and y ions
     *
     * @param fragTol fragment tolerance to use
     * @param library library spectra to be annotated
     * @throws IOException
     */
    public void annotateLibrary(double fragTol, String library) throws IOException {

        if ("".equals(library)) {
            System.out.println("Validation errors: No spectra library has given");

        } else if (!library.endsWith(".mgf") && !library.endsWith(".msp")) {
            System.out.println("Validation errors: given spectral library file format is not supported");
        } else {

            try {
                File libFile = new File(library);
                Annotation ann = new Annotation(libFile, fragTol);

                ann.annotateSpecFile(false);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MainConsoleController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                java.util.logging.Logger.getLogger(MainConsoleController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
