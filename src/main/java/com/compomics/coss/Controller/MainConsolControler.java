package com.compomics.coss.Controller;

import com.compomics.coss.Model.ConfigHolder;
import com.compomics.matching.Matching;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import com.compomics.coss.Model.ConfigData;

/**
 * Controller class to run the project from command line
 *
 * @author Genet
 */
public class MainConsolControler {

    /**
     * @param args the command line arguments
     */
    // static ConfigHolder config = new ConfigHolder();
    static ConfigData data;
    static Matching matching;

    private static final Logger LOG = Logger.getLogger(MainConsolControler.class);

    /**
     * the main method
     *
     * @param args
     */
    public static void main(String[] args) {
//        try {
//
//            List<int[]> result;
//            MainConsolControler mc = new MainConsolControler();
//
//            //Load user inputs from properties file
//            mc.loadData();
//
//            //validate user input data
//            List<String> valdMsg = mc.validateSettings();
//
//            if (!valdMsg.isEmpty()) {
//                StringBuilder message = new StringBuilder();
//                for (String validationMessage : valdMsg) {
//                    message.append(validationMessage).append(System.lineSeparator());
//                }
//                LOG.info("Validation errors" + message.toString());
//            } else {
//                //Read spectral data both target and db spectra
//                ReadSpectralData r = new ReadSpectralData();
//                ArrayList<MSnSpectrum> specA = r.readSpectra(data.getTargetSpecFile());
//
//                r = new ReadSpectralData();
//                ArrayList<MSnSpectrum> specB = r.readSpectra(data.getSpecLibraryFile());
//
//                data.setTargetSpectra(specA);
//                data.setLibSpectra(specB);
//
//                matching.InpArgs(Integer.toString(data.getScoringFunction()), Integer.toString(data.getIntensityOption()), Double.toString(data.getfragTol()));
//
//                result = matching.compare(data.getTargetSpectra(), data.getLibSpectra(), LOG);
//
//                mc.saveResult(result, data.getOutputFilePath());
//            }
//
//        } catch (Exception ex) {
//            LOG.info(null + " " + ex);
//        }

    }

    /**
     * Method to load setting data from config.properties file
     */
    private void loadData() {
        //Reading User inputs and set to config data 

        data.setSpecLibraryFile(ConfigHolder.getInstance().getString("db.spectra.path"));
        data.setExperimentalSpecFile(ConfigHolder.getInstance().getString("target.spectra.path"));
        data.setScoringFunction(ConfigHolder.getInstance().getInt("matching.algorithm"));
        data.setMaxPrecursorCharg(ConfigHolder.getInstance().getInt("max.charge"));
        data.setPrecTol(ConfigHolder.getInstance().getDouble("precursor.tolerance"));
        data.setfragTol(ConfigHolder.getInstance().getDouble("fragment.tolerance"));
        data.setIntensityOption(ConfigHolder.getInstance().getInt("intensity.option"));

    }

    /**
     * Method to validate the settings read from the file
     *
     * @return
     */
    private List<String> validateSettings() {
        List<String> validationMessages = new ArrayList<>();

        String fileExtnTar = data.getExperimentalSpecFile().getName();
        String fileExtnDB = data.getSpecLibraryFile().getName();
        if (!data.getSpecLibraryFile().exists()) {
            validationMessages.add("Database spectra file not found");
        } else if (!fileExtnDB.endsWith(".mgf") || !fileExtnDB.endsWith(".msp")) {
            validationMessages.add(" Database Spectra file typenot valid");
        }
        if (!data.getExperimentalSpecFile().exists()) {
            validationMessages.add("Target spectra file not found");
        } else if (!fileExtnTar.endsWith(".mgf") || !fileExtnTar.endsWith(".msp")) {
            validationMessages.add(" Targer Spectra file typenot valid");
        }

        if (data.getPrecTol() < 0.0) {
            validationMessages.add("Please provide a positive precursor tolerance value.");
        }

        if (data.getMaxPrecursorCharg() < 0.0) {
            validationMessages.add("Please provide a positive precursor charge value.");
        }

        if (data.getfragTol() < 0.0) {
            validationMessages.add("Please provide a positive fragment tolerance value.");
        }

        return validationMessages;

    }

    /**
     * Save the result to file selected by the user
     *
     * @param result result of the comparison
     * @param path the file path to which the result to be written
     */
    private void saveResult(List<int[]> result, String path) {

        BufferedWriter writer = null;
        File fname = new File(path + File.separator + "COSS_Result.txt");

        try {

            writer = new BufferedWriter(new FileWriter(fname));
            int[] singleArgetResult;
            int j;
            String op;
            for (int i = 0; i < result.size(); i++) {
                singleArgetResult = result.get(i);
                op = "";
                for (j = 0; j < 10; j++) {

                    op = op.concat("[" + Integer.toString(singleArgetResult[j + 10]) + " : " + Integer.toString(singleArgetResult[j]) + "]");
                }
                writer.write(op);
                writer.write("\n");
            }

        } catch (IOException e) {

        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }

    }
}
