package com.compomics.coss.controller;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigHolder;
import com.compomics.coss.controller.matching.Matching;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.compomics.coss.model.ConfigData;
import com.compomics.coss.controller.matching.CosineSimilarity;
import com.compomics.coss.controller.matching.MeanSquareError;
import com.compomics.coss.controller.matching.Dispartcher;
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
public class MainConsolControler {

    /**
     * @param args the command line arguments
     */
    // static ConfigHolder config = new ConfigHolder();
    static ConfigData configData;
    static Dispartcher dispatcher;
    private boolean isReaderReady;
    private boolean isBussy;

    private static final Logger LOG = Logger.getLogger(MainConsolControler.class);

    /**
     * the main method
     *
     * @param args
     */
    public static void main(String[] args) {
//        try {
//
//            configData = new ConfigData();
//            List<ComparisonResult> result = new ArrayList<>();
//            MainConsolControler mc = new MainConsolControler();
//
//            //Load user inputs from properties file
//            mc.loadData();
//            //validate user input configData
//            List<String> valdMsg = mc.validateSettings();
//
//            if (!valdMsg.isEmpty()) {
//                StringBuilder message = new StringBuilder();
//                for (String validationMessage : valdMsg) {
//                    message.append(validationMessage).append(System.lineSeparator());
//                }
//                LOG.info("Validation errors" + message.toString());
//            } else {
//                //Read spectral configData both target and db spectra
//                mc.configReader();
//                if ((configData.getExpSpectraIndex() != null || configData.getEbiReader() != null) && configData.getSpectraLibraryIndex() != null) {
//
//                    result = mc.startMatching();
//                    String fullName = configData.getExperimentalSpecFile().getName();
//                    String fname = "";
//                    if (fullName.contains(".")) {
//                        fname = fullName.substring(0, fullName.indexOf("."));
//                    } else {
//                        fname = fullName;
//                    }
//                    mc.saveResultExcel(result, fname);
//
//                }
//
//            }
//
//        } catch (Exception ex) {
//            LOG.info(null + " " + ex);
//        }

    }

    /**
     * Method to load setting configData from config.properties file
     */
    private void loadData() {
        //Reading User inputs and set to config configData 
        configData.setScoringFunction(ConfigHolder.getInstance().getInt("matching.algorithm"));
        configData.setMaxPrecursorCharg(ConfigHolder.getInstance().getInt("max.charge"));
        configData.setPrecTol(ConfigHolder.getInstance().getDouble("precursor.tolerance"));
        configData.setfragTol(ConfigHolder.getInstance().getDouble("fragment.tolerance"));
        configData.setIntensityOption(ConfigHolder.getInstance().getInt("intensity.option"));
        File fileQuery = new File(ConfigHolder.getInstance().getString("target.spectra.path"));
        File fileLib = new File(ConfigHolder.getInstance().getString("spectra.library.path"));
        configData.setSpecLibraryFile(fileLib);
        configData.setExperimentalSpecFile(fileQuery);

    }

    private void configReader() {

        isBussy = true;
        isReaderReady = false;
        LOG.info("Configuring Spectrum Reader ....");
        ConfigSpecReaders cfReader = new ConfigSpecReaders(configData);
        cfReader.startConfig();

    }

    private List<ComparisonResult> startMatching() {

        int scoring = configData.getScoringFunction();
        switch (scoring) {
            case 0:
                dispatcher = new Dispartcher(configData, lstner, LOG);
                break;
            case 1:
                dispatcher = new CosineSimilarity(this.configData);
                break;
            case 2:
                dispatcher = new MeanSquareError(this.configData);
                break;

        }

        String[] arg = {Integer.toString(configData.getMsRobinOption()), Integer.toString(configData.getIntensityOption()),
            Double.toString(configData.getfragTol()), Double.toString(configData.getPrecTol())};
        dispatcher.InpArgs(arg);

        List<ComparisonResult> result = null;
        LOG.info("COSS version 1.0");
        LOG.info("Query spectra: " + configData.getExperimentalSpecFile().toString());
        LOG.info("Library: " + configData.getSpecLibraryFile().toString());
        LOG.info("Search started ");

        result = dispatcher.dispatcher(LOG);
        return result;

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
        } else if (!fileExtnTar.endsWith(".mgf") && !fileExtnTar.endsWith(".msp")) {
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

    /**
     * Save the result to file selected by the user
     *
     * @param result result of the comparison
     * @param path the file path to which the result to be written
     */
    /**
     * Save results as excel file
     */
    private void saveResultExcel(List<ComparisonResult> result, String filename) {

        FileOutputStream fileOut = null;
        try {

            String[] columns = {"Title", "Library Source", "Scan No.", "Sequence", "Prec. Mass", "Charge", "Score"};
            //List<Employee> employees =  new ArrayList<>();

            // Create a Workbook
            Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
            /* CreationHelper helps us create instances of various things like DataFormat,
            Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
            CreationHelper createHelper = workbook.getCreationHelper();
            // Create a Sheet
            Sheet sheet = workbook.createSheet("Coss_Result");
            // Create a Font for styling header cells
            Font headerFont = workbook.createFont();
            headerFont.setBold(false);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
            // Create a CellStyle with the font
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            // Create a Row
            Row headerRow = sheet.createRow(0);
            // Create cells
            int len = columns.length;
            for (int i = 0; i < len; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }   // Create Cell Style for formatting Date
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
            // Create Other rows and cells with employees data
            int rowNum = 1;
            Spectrum spec;

            for (ComparisonResult res : result) {
                Row row = sheet.createRow(rowNum);
                spec = res.getEspSpectrum();
                row.createCell(0).setCellValue(spec.getTitle());
                row.createCell(1).setCellValue(res.getMatchedLibSpec().get(0).getSource());
                row.createCell(2).setCellValue(spec.getScanNumber());
                row.createCell(3).setCellValue(res.getMatchedLibSpec().get(0).getSequence());
                row.createCell(4).setCellValue(spec.getPCMass());
                row.createCell(5).setCellValue(spec.getCharge());
                row.createCell(6).setCellValue(Double.toString(res.getTopScore()));
                rowNum++;

            }   // Resize all columns to fit the content size
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }   // Write the output to a file

            //int total_len = result.size();
            //sheet.shiftRows(cutoff_index_1percent, total_len, 1);
            // sheet.shiftRows(cutoff_index_5percent, total_len, 1);
            fileOut = new FileOutputStream(filename + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            // Closing the workbook
            workbook.close();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            try {
                fileOut.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }
}
