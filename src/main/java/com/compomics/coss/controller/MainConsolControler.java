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
    public static void main(String[] args) {
        try {

            configData = new ConfigData();

            MainConsolControler mc = new MainConsolControler();

            //Load user inputs from properties file
            mc.loadData();
            //validate user input configData
            List<String> valdMsg = mc.validateSettings();

            if (!valdMsg.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (String validationMessage : valdMsg) {
                    message.append(validationMessage).append(System.lineSeparator());
                }
                LOG.info("Validation errors" + message.toString());
            } else {
                //Read spectral configData both target and db spectra
                mc.configReader();

                if ((configData.getExpSpectraIndex() != null || configData.getEbiReader() != null) && configData.getSpectraLibraryIndex() != null) {

                    mc.startMatching();
                    String fullName = configData.getExperimentalSpecFile().getName();
                    String fname = "";
                    if (fullName.contains(".")) {
                        fname = fullName.substring(0, fullName.indexOf("."));
                    } else {
                        fname = fullName;
                    }
                     String path =  configData.getExperimentalSpecFile().getParent();
                    mc.saveResultExcel(path + "\\" + fname);
                }

            }

        } catch (Exception ex) {
            LOG.info(null + " " + ex);
        }

    }

    /**
     * Method to load setting configData from config.properties file
     */
    private void loadData() {
        //Reading User inputs and set to config configData 

        File fileQuery = new File(ConfigHolder.getInstance().getString("target.spectra.path"));
        File fileLib = new File(ConfigHolder.getInstance().getString("spectra.library.path"));
        configData.setSpecLibraryFile(fileLib);
        configData.setExperimentalSpecFile(fileQuery);

        //Scoring function
        configData.setScoringFunction(ConfigHolder.getInstance().getInt("matching.algorithm"));
        configData.setIntensityOption(ConfigHolder.getInstance().getInt("intensity.option"));
        configData.setMsRobinOption(ConfigHolder.getInstance().getInt("msRobin.option"));

        //MS instrument based settings
        configData.setMaxPrecursorCharg(ConfigHolder.getInstance().getInt("max.charge"));

        configData.setPrecTol(ConfigHolder.getInstance().getDouble("precursor.tolerance"));
        configData.setfragTol(ConfigHolder.getInstance().getDouble("fragment.tolerance") / (double) 1000);

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
     * Save results as excel file
     */
    private void saveResultExcel(String filename) {

        FileOutputStream fileOut = null;
        try {

            String[] columns = {"Title", "Library", "Scan No.", "Sequence", "Prec. Mass", "Charge", "Score", "Validation", "#filteredQueryPeaks", "#filteredLibraryPeaks", "SumIntQuery", "SumIntLib", "#MatchedPeaks", "MatchedIntQuery", "MatchedIntLib"};
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
                List<MatchedLibSpectra> mSpec = res.getMatchedLibSpec();
                // int lenMspec = mSpec.size();
                //for (int s = 0; s < lenMspec; s++) {
                int s = 0;
                Row row = sheet.createRow(rowNum);
                spec = res.getEspSpectrum();
                row.createCell(0).setCellValue(spec.getTitle());
                row.createCell(1).setCellValue(mSpec.get(s).getSource());
                row.createCell(2).setCellValue(spec.getScanNumber());
                row.createCell(3).setCellValue(mSpec.get(s).getSequence());
                row.createCell(4).setCellValue(spec.getPCMass());
                row.createCell(5).setCellValue(spec.getCharge());
                row.createCell(6).setCellValue(Double.toString(res.getTopScore()));
                if (configData.isDecoyAvailable()) {
                    if (rowNum - 1 <= cutoff_index_1percent) {
                        //conf= score * 100;
                        row.createCell(7).setCellValue("<1% FDR");
                    } else if (rowNum - 1 <= cutoff_index_5percent) {
                        row.createCell(7).setCellValue("<5% FDR");
                    } else {
                        row.createCell(7).setCellValue(">5% FDR");
                    }
                } else {
                    row.createCell(7).setCellValue("NA");
                }
                row.createCell(8).setCellValue(Integer.toString(mSpec.get(s).getTotalFilteredNumPeaks_Exp()));
                row.createCell(9).setCellValue(Integer.toString(mSpec.get(s).getTotalFilteredNumPeaks_Lib()));
                row.createCell(10).setCellValue(Double.toString(mSpec.get(s).getSumFilteredIntensity_Exp()));
                row.createCell(11).setCellValue(Double.toString(mSpec.get(s).getSumFilteredIntensity_Lib()));
                row.createCell(12).setCellValue(Integer.toString(mSpec.get(s).getNumMatchedPeaks()));
                row.createCell(13).setCellValue(Double.toString(mSpec.get(s).getSumMatchedInt_Exp()));
                row.createCell(14).setCellValue(Double.toString(mSpec.get(s).getSumMatchedInt_Lib()));
                rowNum++;

                //}
            }   // Resize all columns to fit the content size
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }   // Write the output to a file

            int total_len = result.size();
            sheet.shiftRows(cutoff_index_1percent, total_len, 1);
            sheet.shiftRows(cutoff_index_5percent, total_len, 1);

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

    @Override
    public void updateprogress(int taskCompleted) {

        final double PERCENT = 100.0 / (double) configData.getExpSpectraIndex().size();

        int v = (int) (taskCompleted * PERCENT);
        System.out.println(Integer.toString(v) + "%");

    }
}
