package com.compomics.coss.Controller;

import com.compomics.coss.Model.*;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import com.compomics.coss.Model.ConfigHolder;
import com.compomics.coss.View.*;
//import com.compomics.matching.Cascade;
import javax.swing.JFileChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.SwingUtilities;
import com.compomics.matching.Matching;
import com.compomics.matching.UseMsRoben;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import com.compomics.ms2io.*;
import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Controller class for GUI
 *
 * @author Genet
 */
public class MainFrameController implements UpdateListener {

    private static final Logger LOG = Logger.getLogger(MainFrameController.class);

    /**
     * Objects of the views
     */
    private MainGUI mainView;
    private SettingPanel settingsPnl;
    private ResultPanel resultPnl;
    private TargetDB_View targetView;

    // private ConfigHolder config = new ConfigHolder();
    Matching matching;
    private static List<ArrayList<ComparisonResult>> result = null;
    int cutoff_index;
    ConfigData configData;
    public boolean cencelled = false;
    public boolean isBussy = false;

    public DefaultTableModel tblModelResult;
    public DefaultTableModel tblModelTarget;
    public SpinnerNumberModel spnModel;
    public DefaultComboBoxModel cmbModel;

    private int targSpectrumNum, resultNumber;

    private boolean isReaderReady;
    private int cut_off_index;

    /**
     * Initialize objects, variables and components.
     */
    //<editor-fold  defaultstate="Colapsed" desc="Initialize Components">
    public void init() {

        configData = new ConfigData(null, null);
        isReaderReady = false;
        String libPath = ConfigHolder.getInstance().getString("spectra.library.path");
        settingsPnl = new SettingPanel(this, new File(libPath));
        resultPnl = new ResultPanel(this);
        targetView = new TargetDB_View(this);

        mainView = new MainGUI(settingsPnl, resultPnl, targetView, this);
        mainView.pnlCommands.setLayout(new BorderLayout());
        // add gui appender
        LogTextAreaAppender logTextAreaAppender = new LogTextAreaAppender();
        logTextAreaAppender.setLogArea(mainView);

        logTextAreaAppender.setThreshold(Priority.INFO);
        logTextAreaAppender.setImmediateFlush(true);
        PatternLayout layout = new org.apache.log4j.PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} - %m%n");
        logTextAreaAppender.setLayout(layout);

//        settingsPnl.txtFragmentTolerance.setText(Integer.toString(6));
//        settingsPnl.txtPrecursorTolerance.setText(Integer.toString(20));
//        settingsPnl.txtPrecursorCharge.setText(Integer.toString(3));
        final String[] colNamesRes = {"No.", "Name/Title", "M/Z", "Charge", "Score", "Confidence(%)"};
        final String[] colNamesExperimental = {"No.", "Name/Title", "M/Z", "Charge", "No. Peaks"};

        tblModelResult = new DefaultTableModel(colNamesRes, 0);
        tblModelTarget = new DefaultTableModel(colNamesExperimental, 0);

        spnModel = new SpinnerNumberModel(1, 1, 1, 1);
        targetView.spnSpectrum.setModel(spnModel);

        resultPnl.tblTargetSpectra.setModel(tblModelTarget);
        resultPnl.tblBestMatch.setModel(tblModelResult);

        //cmbModel = new DefaultComboBoxModel();
        //settingsPnl.cboSpectraLibrary.setModel(cmbModel);
        spectrumDisplay(0);
        rasterDisplay();

        LOG.addAppender(logTextAreaAppender);
        LOG.setLevel((Level) Level.INFO);
        LoadData();//loading data from propery file to user interface

    }//</editor-fold>

    /**
     * Show the view of this controller.
     */
    public void showMainFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainView.setBounds(0, 0, screenSize.width, screenSize.height - 40);
        mainView.setMinimumSize(new Dimension(530, 600));
        mainView.setVisible(true);
    }

    /**
     * stop the system safely
     */
    public void stopSearch() {

        if (matching != null) {
            this.cencelled = true;
            matching.stopMatching();

        } else {
            LOG.info("Nothing to cancel! Matching process is not running");
        }
    }

    /**
     * Start Spectrum searching upon user click on start button
     */
    public void startSearch() {

        List<String> validationMessages = validateInpSettings();
        if (!validationMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            validationMessages.stream().forEach((validationMessage) -> {
                message.append(validationMessage).append(System.lineSeparator());
            });
            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);

        } else if (this.isReaderReady) {

            setSearchSettings();
            this.cencelled = false;
            matching = new UseMsRoben(this, this.configData, "target");
            mainView.setProgressValue(0);
            SwingWorkerThread workerThread = new SwingWorkerThread();
            workerThread.execute();

        } else {
            LOG.info("Spectrum reader is not ready");
        }

    }

    public void configReader() {

        List<String> validationMessages = validateInpFiles();
        if (!validationMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            validationMessages.stream().forEach((validationMessage) -> {
                message.append(validationMessage).append(System.lineSeparator());
            });
            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
        } else {
            this.isReaderReady = false;
            this.isBussy = true;
            mainView.searchBtnActive(false);
            SwingReadThread readThread = new SwingReadThread();
            readThread.execute();

        }

    }

    /**
     * start search against decoy database
     */
    public void validateResult() {

        Validation validate = new Validation();
        cut_off_index = validate.validate(result, 0.1);
        result.subList(cut_off_index, result.size()).clear();

    }

    /**
     * Read user input from GUI
     *
     */
    private void setSearchSettings() {

        configData.setScoringFunction(settingsPnl.cboScoringFun.getSelectedIndex());
        configData.setMaxPrecursorCharg(Integer.parseInt(settingsPnl.txtPrecursorCharge.getText()));
        configData.setPrecTol(Double.parseDouble(settingsPnl.txtPrecursorTolerance.getText()));
        configData.setfragTol(Double.parseDouble(settingsPnl.txtFragmentTolerance.getText()));

    }

    /**
     * Read input configData from file and put on GUI
     */
    public void LoadData() {

        settingsPnl.txttargetspec.setText(ConfigHolder.getInstance().getString("target.spectra.path"));
        settingsPnl.cboScoringFun.setSelectedIndex(ConfigHolder.getInstance().getInt("matching.algorithm"));
        settingsPnl.txtPrecursorCharge.setText(Integer.toString(ConfigHolder.getInstance().getInt("max.charge")));
        settingsPnl.txtPrecursorTolerance.setText(Double.toString(ConfigHolder.getInstance().getDouble("precursor.tolerance")));
        settingsPnl.txtFragmentTolerance.setText(Double.toString(ConfigHolder.getInstance().getDouble("fragment.tolerance")));

    }

    /**
     * Validate the user input and return a list of validation messages if input
     * value is not the right format.
     *
     * @return the list of validation messages
     */
    public List<String> validateInpSettings() {
        //settingsPnl.txtLibrary.setText("C:/tempData/SpecB.msp");
        List<String> validationMessages = new ArrayList<>();

        String temp = settingsPnl.txtPrecursorTolerance.getText();

        if ("".equals(temp)) {
            validationMessages.add("Please provide a precursor tolerance value.");
        } else {
            try {
                double d = Double.parseDouble(temp);
                if (d < 0.0) {
                    validationMessages.add("Please provide a valid precursor tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric precursor tolerance value.");
            }
        }

        temp = settingsPnl.txtPrecursorCharge.getText();
        if (temp.equals("")) {
            validationMessages.add("Please provide a maximum precursor charge value.");
        } else {
            try {
                double d = Double.parseDouble(temp);
                if (d < 0.0) {
                    validationMessages.add("Please provide a valid precursor tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric maximum precursor charge value.");
            }
        }

        temp = settingsPnl.txtFragmentTolerance.getText();
        if (temp.equals("")) {
            validationMessages.add("Please provide a fragment tolerance value.");
        } else {
            try {
                double d = Double.parseDouble(temp);
                if (d < 0.0) {
                    validationMessages.add("Please provide a valid precursor tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric fragment tolerance value.");
            }
        }

//        if (mainView.getChkFilter().isSelected()) {
//            if (mainView.getTxtCutOff().getText().isEmpty()) {
//                validationMessages.add("Please a provide peak cutoff number when choosing the TopN intense peak selection filter.");
//            } else {
//                try {
//                    Integer number = Integer.valueOf(mainView.getTxtCutOff().getText());
//                    if (number < 0) {
//                        validationMessages.add("Please provide a positive peak cutoff number value.");
//                    }
//                } catch (NumberFormatException nfe) {
//                    validationMessages.add("Please provide a numeric peak cutoff number value.");
//                }
//            }
//        }
        return validationMessages;
    }

    /**
     * Validate the user input and return a list of validation messages if input
     * value is not the right format.
     *
     * @return the list of validation messages
     */
    public List<String> validateInpFiles() {
        //settingsPnl.txtLibrary.setText("C:/tempData/SpecB.msp");
        List<String> validationMessages = new ArrayList<>();

        String tempS = settingsPnl.txttargetspec.getText();
        if ("".equals(tempS)) {
            validationMessages.add("Please provide a spectra input directory.");
        } else if (!tempS.endsWith(".mgf") && !tempS.endsWith(".msp") && !tempS.endsWith(".mzML")
                && !tempS.endsWith(".mzXML") && !tempS.endsWith(".ms2")) {
            validationMessages.add(" Targer Spectra file type not valid");
        }

        String tempS2 = settingsPnl.txtLibrary.getText();
        if ("".equals(tempS2)) {
            validationMessages.add("Please select library file");
        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp")) {
            validationMessages.add(" Data Base Spectra file type is invalid." + " \n " + "Only .mgf and .msp file format supported");
        }

        if (validationMessages.isEmpty()) {
            configData = new ConfigData(new File(tempS), new File(tempS2));
        }
        return validationMessages;
    }

    /**
     * Shows a message dialog.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @param messageType the dialog message type
     */
    private void showMessageDialog(final String title, final String message, final int messageType) {
        JTextArea textArea = new JTextArea(message);
        //put JTextArea in JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        scrollPane.getViewport().setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JOptionPane.showMessageDialog(mainView.getContentPane(), scrollPane, title, messageType);
    }

    /**
     * Displays the comparison result visually on the result panel
     */
    private void displayResult() {

        try {
            if (!result.get(targSpectrumNum).isEmpty()) {
                
                int indexExp=result.get(targSpectrumNum).get(0).getMatchedExpIndex();                
                Long posExp=configData.getExpSpectraIndex().get(indexExp).getPos();              
                Spectrum targSpec = configData.getExpSpecReader().readAt(posExp);

                ArrayList<ComparisonResult> singleResult = result.get(targSpectrumNum);

                if(this.resultNumber<0){
                    this.resultNumber=0;
                }
                if (singleResult != null) {
                    ComparisonResult targMatchedResult = singleResult.get(this.resultNumber);
                    Spectrum matchedSpec = configData.getLibSpecReader().readAt(targMatchedResult.getSpecPosition());
                    resultPnl.pnlVisualSpectrum.removeAll();
                    SpecPanel spcPanel = new SpecPanel(targSpec, matchedSpec);
                    spcPanel.setPreferredSize(new Dimension(600, 300));

                    resultPnl.pnlVisualSpectrum.add(spcPanel);
                    //resultPnl.pnlVisualSpectrum.repaint();
                    //resultPnl.pnlVisualSpectrum.revalidate();
                }
            } else {
                resultPnl.pnlVisualSpectrum.removeAll();
            }

        } catch (Exception exception) {
            LOG.error(exception + " target spectrum number: " + Integer.toString(this.resultNumber));
        }

        resultPnl.pnlVisualSpectrum.revalidate();
        resultPnl.pnlVisualSpectrum.repaint();
    }

    /**
     * Fill table with target spectra
     */
    private void fillExpSpectraTable() {

        tblModelTarget.setRowCount(0);
        int targetsize = 0;
        if (configData.getExpSpectraIndex() != null) {
            targetsize = result.size();// configData.getExpSpectraIndex().size();
            Spectrum expSpec;
            Object[][] rows = new Object[targetsize][5];
            
            for (int p = 0; p < targetsize; p++){
                // name = d.getSpectra1().get(p).getSpectrumTitle();

                //expSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(p).getPos());
                int indexExp=result.get(p).get(0).getMatchedExpIndex();
                Long posExp=configData.getExpSpectraIndex().get(indexExp).getPos();
                expSpec = configData.getExpSpecReader().readAt(posExp);
            
                rows[p][0] = indexExp;
                //rows[p][1] = "ID" + Integer.toString(p + 1);
                rows[p][1] = expSpec.getTitle();
                rows[p][2] = expSpec.getPCMass();
                rows[p][3] = expSpec.getCharge();
                rows[p][4] = expSpec.getNumPeaks();

                tblModelTarget.addRow(rows[p]);
            }
        } else if (configData.getEbiReader() != null) {

            uk.ac.ebi.pride.tools.jmzreader.model.Spectrum expSpec;
            Iterator<uk.ac.ebi.pride.tools.jmzreader.model.Spectrum> itr = configData.getEbiReader().getSpectrumIterator();
            targetsize = configData.getEbiReader().getSpectraCount();
            Object[][] rows = new Object[targetsize][5];
            int p = 0;
            while (itr.hasNext()) {
                expSpec = itr.next();
                rows[p][0] = p + 1;
                // rows[p][1] = "ID" + Integer.toString(p + 1);
                rows[p][1] = expSpec.getId();
                rows[p][2] = expSpec.getPrecursorMZ();
                rows[p][3] = expSpec.getPrecursorCharge();
                rows[p][4] = expSpec.getPeakList().size();
                tblModelTarget.addRow(rows[p]);
                p++;
            }
        }

    }

    /**
     * fill table with best matched spectra
     *
     * @param target target spectrum index
     */
    public void fillBestmatchTable(int target) {
        
        this.targSpectrumNum = target;
        ArrayList<ComparisonResult> singleResult = result.get(this.targSpectrumNum);
        

        if (!singleResult.isEmpty()) {
            tblModelResult.setRowCount(0);
            
            int i = 0;
            Object[] rows = new Object[7];

            for (ComparisonResult r : singleResult) {

                rows[0] = Integer.toString(++i);
                rows[1] = "ID" + Integer.toString(i);
                rows[2] = r.getTitle();
                rows[3] = r.getPrecMass();
                rows[4] = r.getCharge();
                rows[5] = r.getScore();

                double conf = (r.getScore() / 400) * 100;
                rows[6] = Math.round(conf);
                tblModelResult.addRow(rows);
                i++;

            }

        } else {
            tblModelResult.setRowCount(0);
        }
        

        
        //tblBestMatch.setRowSelectionInterval(0, tblBestMatch.getRowCount()-1);
        
       // this.resultNumber=0;
       
        updateresultview(0);

    }

    /**
     * fill table with best matched spectra
     *
     *
     */
//    public void fillBestmatchTable() {
//
//        //ArrayList<ComparisonResult> singleResult = resultTarget.get(target);
//        int a = 0;
//        Object[][] rows = new Object[10][8];
//
//        for (ArrayList<ComparisonResult> res : result) {
//
//            if (!res.isEmpty()) {
//                // Long pos = res.get(0).getSpecPosition();
//
//                rows[a][0] = a + 1;
//                rows[a][1] = "ID" + Integer.toString(a);
//                rows[a][2] = res.get(0).getTitle();
//                rows[a][3] = res.get(0).getPrecMass();
//                rows[a][4] = res.get(0).getCharge();
//                rows[a][5] = res.get(0).getScore();
//
//                double conf = (res.get(0).getScore() / 400) * 100;
//                rows[a][6] = Math.round(conf);
//                tblModelResult.addRow(rows[a]);
//                a++;
//            }
//
//        }
//
//        updateresultview(0);
//
//    }

    /**
     * Updates progress bar value during the comparison process
     *
     * @param percent
     */
    @Override
    public void updateprogressbar(double percent) {

        final double PERCENT = percent;
        SwingUtilities.invokeLater(() -> {
            int v = (int) (100 * PERCENT);
            mainView.setProgressValue(v);
            mainView.setProgressValue(Integer.toString(v) + "%");
        });
    }

    /**
     * update the input information area for target spectrum on GUI based on
     * user selected spectrum
     */
    public void updateInputInfo() {
        if (configData.getExpSpecReader() != null && configData.getExpSpectraIndex() != null) {

            int specNumber = spnModel.getNumber().intValue() - 1;
            Spectrum tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(specNumber).getPos());

            targetView.txtRtTime.setText(Double.toString(tSpec.getRtTime()));
            targetView.txtScanno.setText(tSpec.getScanNumber());
            targetView.txtmaxmz.setText(Double.toString(tSpec.getMaxMZ()));
            targetView.txtminmz.setText(Double.toString(tSpec.getMinMZ()));
            targetView.txtnumpeaks.setText(Integer.toString(tSpec.getNumPeaks()));
            spectrumDisplay(specNumber);
        }
    }

    /**
     * Update the visual result panel based on the selection of the target
     * spectrum
     *
     * @param index
     */
    public void updateresultview(int index) {

        //getIndex(index);
        this.resultNumber = index;
        displayResult();
    }

    /**
     * Save graphically provided settings to the property file
     */
    public void saveSettings() {

        List<String> validationMessages = validateInpSettings();
        if (!validationMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (String validationMessage : validationMessages) {
                message.append(validationMessage).append(System.lineSeparator());
            }
            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
        } else {

            setSearchSettings();
            ConfigHolder.getInstance().setProperty("matching.algorithm", configData.getScoringFunction());
            ConfigHolder.getInstance().setProperty("fragment.tolerance", configData.getfragTol());
            ConfigHolder.getInstance().setProperty("precursor.tolerance", configData.getPrecTol());
            ConfigHolder.getInstance().setProperty("max.charge", configData.getMaxPrecursorCharg());
            ConfigHolder.getInstance().setProperty("db.spectra.path", configData.getSpecLibraryFile());
            ConfigHolder.getInstance().setProperty("target.spectra", configData.getExperimentalSpecFile());
        }

    }

    private void saveResultExcel(String filename){
        
        
        FileOutputStream fileOut = null;
        try {
            
            String[] columns = {"Index", "Name", "Prec. Mass", "Charge", "Score"};
            //List<Employee> employees =  new ArrayList<>();
            
            // Create a Workbook
            Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
            /* CreationHelper helps us create instances of various things like DataFormat,
            Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
            CreationHelper createHelper = workbook.getCreationHelper();
            // Create a Sheet
            Sheet sheet = workbook.createSheet("Employee");
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
            int len=columns.length;
            for(int i = 0; i < len; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }   // Create Cell Style for formatting Date
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
            // Create Other rows and cells with employees data
            int rowNum = 1;
            for(ArrayList<ComparisonResult> res: result) {
                Row row = sheet.createRow(rowNum);
                
                row.createCell(0)
                        .setCellValue(Integer.toString(rowNum));
                
                row.createCell(1)
                        .setCellValue(res.get(0).getTitle());
                
                row.createCell(2)
                        .setCellValue(res.get(0).getPrecMass());
                
                row.createCell(3)
                        .setCellValue(res.get(0).getCharge());
                
               
                row.createCell(4)
                        .setCellValue(Double.toString(res.get(0).getScore()));
                
                rowNum++;
//                if(rowNum>=4)
//                    break;
            }   // Resize all columns to fit the content size
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }   // Write the output to a file
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
    
    
    
    /**
     * saves the result to user selected file
     */
    public void saveResult() throws InterruptedException {

        if (result != null) {

            JFileChooser fileChooser = new JFileChooser("D:/");
            fileChooser.setDialogTitle("Specify a file to save");

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File filename = fileChooser.getSelectedFile();
                
                saveResultExcel(filename.getPath());
                
                
                
                
                
//                BufferedWriter writer = null;
//                try {
//
//                    writer = new BufferedWriter(new FileWriter(filename));
//                    for (ArrayList<ComparisonResult> singleTargetResult : result) {
//                        for (ComparisonResult r : singleTargetResult) {
//                            double confidence = (r.getScore() / 400.0) * 100.0;//value of the score
//                            confidence = Math.round(confidence);
//                            String op = r.getTitle() + "," + r.getCharge() + "," + r.getScanNum() + "," + Double.toString(r.getPrecMass()) + "," + Double.toString(r.getScore()) + "," + Double.toString(confidence);
//                            writer.write(op);
//                            writer.write("\n");
//
//                        }
//                        writer.write("\n\n");
//                    }
//
//                } catch (IOException e) {
//
//                } finally {
//                    try {
//                        if (writer != null) {
//                            writer.close();
//                        }
//                    } catch (IOException e) {
//                    }
//                }

            }

        } else {
//            Log.info("No comparison result.");
        }
    }

    /**
     * this method changes library path
     */
    public void setLibraryPath() {

    }

    /**
     * opens dialog to choose target spectra file
     */
    public void chooseTargetFile() {

        JFileChooser fileChooser = new JFileChooser("C:/pandyDS/");
        fileChooser.setDialogTitle("Target Spectra File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String tempfile = fileChooser.getSelectedFile().getPath();
            tempfile = tempfile.replace('\\', '/');

            settingsPnl.txttargetspec.setText(tempfile);

        }
    }

    /**
     * display color raster of the whole spectra in the target dataset
     */
    public void rasterDisplay() {
        targetView.pnlRaster.removeAll();
        RasterPanel drawingPanel = null;
        if (configData.getExpSpectraIndex() != null) {

            drawingPanel = new RasterPanel(configData.getExpSpectraIndex(), configData.getExpSpecReader());

            drawingPanel.setPreferredSize(new Dimension(600, 300));
            targetView.pnlRaster.add(drawingPanel);
            targetView.pnlRaster.repaint();
            targetView.pnlRaster.revalidate();
        }

    }

    /**
     * visual spectrum display of a selected spectrum at the position specIndex
     *
     * @param specIndex position of the spectrum to be visualized
     */
    public void spectrumDisplay(int specIndex) {

        targetView.pnlVizSpectrum.removeAll();
        SpecPanel spec = new SpecPanel(null);

        if (configData.getExpSpectraIndex() != null) {
            Spectrum tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(specIndex).getPos());
            spec = new SpecPanel(tSpec);
        }

        spec.setPreferredSize(new Dimension(700, 300));
        targetView.pnlVizSpectrum.add(spec);
        targetView.pnlVizSpectrum.repaint();
        targetView.pnlVizSpectrum.revalidate();

    }

    /**
     * swing thread to start the search and it runs on background
     */
    private class SwingWorkerThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            isBussy = true;
            mainView.searchBtnActive(false);

            String[] args = {Integer.toString(configData.getMsRobinOption()), Integer.toString(configData.getIntensityOption()),
                Double.toString(configData.getfragTol()), Double.toString(configData.getPrecTol())};
            matching.InpArgs(args);

            result = matching.dispatcher(LOG);
            return null;

        }

        @Override
        protected void done() {

            try {

                if (cencelled) {

                    LOG.info("Process Cancelled.");
                    mainView.setProgressValue(0);
                    mainView.setProgressValue(Integer.toString(0) + "%");

                } else {
                    LOG.info("Spectrum Similarity Comparison Completed");
                    mainView.setProgressValue(100);
                    mainView.setProgressValue(Integer.toString(100) + "%");

                    if (result != null) {

                        //validationPnl=new ValidationHistogramPanel(resultTarget, resultDecoy);
                        
                        Sort sort = new Sort();
                        sort.Quicksort(result, 0, result.size() - 1);
                        validateResult();
                        
                        fillExpSpectraTable();
                        fillBestmatchTable(0);
                        displayResult();
                        
                        

                    } else {
                        LOG.info("No comparison result.");
                    }

                }

                isBussy = false;
                mainView.searchBtnActive(true);
                get();

            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getMessage(), ex);
                showMessageDialog("Unexpected error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (CancellationException ex) {
                LOG.info("the spectrum similarity score pipeline run was cancelled");
            } finally {

            }
        }

    }

    /**
     * swing thread configure readers and it runs on background
     */
    private class SwingReadThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            // isBussy = true;        
            mainView.readerBtnActive(false);
            mainView.searchBtnActive(false);

            LOG.info("Configuring Spectrum Reader ....");
            ConfigSpecReaders cfReader = new ConfigSpecReaders(configData.getExperimentalSpecFile(), configData.getSpecLibraryFile(), configData);
            cfReader.startConfig();
            return null;
        }

        @Override
        protected void done() {

            try {

                LOG.info("Spectrum Reader Config Completed");
                if ((configData.getExpSpectraIndex() != null || configData.getEbiReader() != null) && configData.getSpectraLibraryIndex() != null) {

                    int expSpecSize = 0;
                    if (configData.getExpSpectraIndex() != null) {
                        expSpecSize = configData.getExpSpectraIndex().size();
                    } else if (configData.getEbiReader() != null) {
                        expSpecSize = configData.getEbiReader().getSpectraCount();
                    }

                    spnModel.setMaximum(expSpecSize);
                    targetView.txtTotalSpec.setText("/" + Integer.toString(expSpecSize));
                    updateInputInfo();
                    spectrumDisplay(0);

                    // rasterDisplay();
                } else {
                    LOG.info("Null Spectrum Index");
                }

                isReaderReady = true;
                isBussy = false;
                mainView.readerBtnActive(true);
                mainView.searchBtnActive(true);
                get();

            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getMessage(), ex);
                showMessageDialog("Unexpected error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (CancellationException ex) {
                LOG.info("the spectrum similarity score pipeline run was cancelled");
            } finally {

            }
        }

    }

}
