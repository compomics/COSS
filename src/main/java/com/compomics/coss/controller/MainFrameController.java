package com.compomics.coss.controller;

import com.compomics.coss.controller.SpectrumAnnotation.Annotation;
import com.compomics.ms2io.model.Spectrum;
import com.compomics.ms2io.model.Peak;
import com.compomics.coss.controller.decoyGeneration.*;
import com.compomics.coss.controller.rescoring.GenerateFeatures;
import com.compomics.coss.controller.rescoring.Rescore;
import com.compomics.coss.view.ResultPanel;
import com.compomics.coss.view.RasterPanel;
import com.compomics.coss.view.TargetDB_View;
import com.compomics.coss.view.SettingPanel;
//import com.compomics.coss.view.MainGUI;
import com.compomics.coss.view.MainFrame;
import com.compomics.coss.model.ConfigData;
import com.compomics.coss.model.MatchedLibSpectra;
import com.compomics.coss.model.ComparisonResult;
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
import com.compomics.coss.model.ConfigHolder;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;

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
    //private MainGUI mainView;
    private MainFrame mainView;
    // private ConfigHolder config = new ConfigHolder();
    Dispatcher dispatcher;
    private static List<ComparisonResult> result = null;

    ConfigData configData;
    public boolean cencelled = false;
    public boolean isBussy = false;
    String fileTobeConfigure;
    boolean isSettingSame;

    public DefaultTableModel tblModelResult;
    public DefaultTableModel tblModelTarget;
    public SpinnerNumberModel spnModel;
    public DefaultComboBoxModel cmbModel;

    private int targSpectrumNum, resultNumber;

    private boolean isReaderReady;

    /**
     * Initialize objects, variables and components.
     */
    //<editor-fold  defaultstate="Colapsed" desc="Initialize Components">
    public void init() throws JMzReaderException {

        configData = new ConfigData();
        fileTobeConfigure = "both"; //asume both query and library files to be read
        isSettingSame = false;
        isReaderReady = false;

        mainView = new MainFrame(this);

        // add gui appender
        LogTextAreaAppender logTextAreaAppender = new LogTextAreaAppender();
        logTextAreaAppender.setLogArea(mainView);

        logTextAreaAppender.setThreshold(Priority.INFO);
        logTextAreaAppender.setImmediateFlush(true);
        PatternLayout layout = new org.apache.log4j.PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} - %m%n");
        logTextAreaAppender.setLayout(layout);

        //Initializing result tables
        final String[] colNamesRes = {"Title", "ScanNo", "Sequence", "Protein", "Mods", "M/Z", "Charge", "Score", "#Peaks", "#FiltedPeaks", "TotalInt", "MatchedInt", "#MatchedPeaks"};
        final String[] colNamesExperimental = {"No. ", "Title", "ScanNo", "RetentionT", "M/Z", "Charge", "Score", "q-value", "Validation(FDR)", "#Peaks", "#FilteredPeaks", "TotalInt", "MatchedInt", "#MatchedPeaks"};

        tblModelResult = new DefaultTableModel(colNamesRes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblModelTarget = new DefaultTableModel(colNamesExperimental, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        spnModel = new SpinnerNumberModel(1, 1, 1, 1);
        mainView.spnSpectrum.setModel(spnModel);

        mainView.tblQuery.setModel(tblModelTarget);
        mainView.tblbestmatch.setModel(tblModelResult);

        //cleaning display area
        spectrumDisplay(0);

        LOG.addAppender(logTextAreaAppender);
        LOG.setLevel((Level) Level.INFO);
        LoadData();//Read input configData from file and put on GUI

        //delet temporary result file if exists... created in Matcher class for storing results temporarily
        File file = new File("temp.txt");
        if (file.exists()) {
            file.delete();
        }

    }//</editor-fold>

    /**
     * Show the main window of .
     */
    public void showMainFrame() {

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        mainView.setBounds(20, 50, dim.width * 4 / 5 - 150, dim.height * 3 / 5 + 80);
// 

        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        // mainView.setLocation(dim.width/2-mainView.getSize().width/2 -70, dim.height/2-mainView.getSize().height/2 -200);
        //mainView.setMinimumSize(new Dimension(1230, 1100));
        mainView.setVisible(true);

    }

    /**
     * stop the system safely
     */
    public void stopSearch() {

        if (dispatcher != null) {
            this.cencelled = true;
            dispatcher.stopMatching();
            mainView.prgProgressBart.setValue(0);
            //mainView.setProgressValue("");
            mainView.btnStartSearch.setEnabled(true);
            mainView.btnConfigSecReader.setEnabled(true);

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
            mainView.prgProgressBart.setValue(0);
            dispatcher = new Dispatcher(this.configData, this, LOG);
            SwingWorkerThread workerThread = new SwingWorkerThread();
            workerThread.execute();

        } else {
            LOG.info("Spectrum reader is not ready");
        }

    }

    /**
     * This function is responsible to index and read both experimental and
     * spectral library file
     *
     */
    public void configReader() {

        List<String> validationMessages = validateInpFiles();
        //if input files are empty and  are in incorrect format
        if (!validationMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            validationMessages.stream().forEach((validationMessage) -> {
                message.append(validationMessage).append(System.lineSeparator());
            });
            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
        } else {
            this.isReaderReady = false;
            this.isBussy = true;
            mainView.btnStartSearch.setEnabled(false);
            SwingReadThread readThread = new SwingReadThread();
            readThread.execute();

        }

    }

    List<ComparisonResult> validatedResult;

    /**
     * start search against decoy database
     */
    public void validateResult() {
        Validation validate = new Validation();
        validate.validate(result, 0.01);

    }

    /**
     * Read user input from GUI
     *
     */
    private void setSearchSettings() {
        int scoringFun = mainView.cmbScoringfunction.getSelectedIndex();

        double precTolerance = Double.parseDouble(mainView.txtprecTolerance.getText());
        double fragTolerance = Double.parseDouble(mainView.txtfragTolerance.getText());

        boolean applyTransform = mainView.chkTransform.isSelected();
        int transformType = mainView.cmbTransformation.getSelectedIndex();
        // boolean applyFilter = settingsPnl.chkFilter.isSelected();
        //int filterType = settingsPnl.cmbFilterType.getSelectedIndex();
        int massWindow = Integer.parseInt(mainView.txtfilterwindow.getText());;
        //int cutOff = Integer.parseInt(settingsPnl.txtCutOff.getText());
        boolean removePCM = mainView.chkremoveprec.isSelected();

        if (mainView.cmbfragmentTolerance.getSelectedIndex() != 0) {//if in PPM 
            fragTolerance /= (double) 1000000;
        }
        if (mainView.cmbprectolerance.getSelectedIndex() != 0) { //if in PPM
            precTolerance /= (double) 1000000;
        }

        isSettingSame = false;

        if (configData.getScoringFunction() == scoringFun
                && configData.getPrecTol() == precTolerance
                && configData.getfragTol() == fragTolerance) {
            isSettingSame = true;

        }

        //no need to reload settings if they are similar and flag up for not repeating same search
        if (!isSettingSame) {
            configData.setScoringFunction(scoringFun);
            configData.setIntensityOption(3);
            configData.setMsRobinOption(0);

            //instrument settings
            configData.setPrecTol(precTolerance);
            configData.setfragTol(fragTolerance);
            //preprocessing settings
            //configData.applyFilter(applyFilter);
            //configData.setFilterType(filterType);
            //configData.setCutOff(cutOff);
            configData.setIsPCMRemoved(removePCM);
            configData.applyTransform(applyTransform);
            configData.setTransformType(transformType);
            configData.setMassWindow(massWindow);

        }
    }

    /**
     * Read input configData from file and put on GUI
     */
    public void LoadData() {

        //spectral data inputs
        mainView.txtqueryspec.setText(ConfigHolder.getInstance().getString("target.spectra.path"));
        mainView.txtlibspec.setText(ConfigHolder.getInstance().getString("spectra.library.path"));

        //Scoring function
        mainView.cmbScoringfunction.setSelectedIndex(ConfigHolder.getInstance().getInt("matching.algorithm"));

        //MS instrument based settings
        mainView.txtprecTolerance.setText(Double.toString(ConfigHolder.getInstance().getDouble("precursor.tolerance")));
        mainView.txtfragTolerance.setText(Double.toString(ConfigHolder.getInstance().getDouble("fragment.tolerance")));

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

        mainView.txtfilterwindow.setText(Integer.toString(ConfigHolder.getInstance().getInt("mass.window")));
        //settingsPnl.chkFilter.setSelected(applyFilter);
        mainView.chkTransform.setSelected(applyTransform);
        //settingsPnl.cmbFilterType.setSelectedIndex(ConfigHolder.getInstance().getInt("filter.type"));
        mainView.cmbTransformation.setSelectedIndex(ConfigHolder.getInstance().getInt("transform.type"));
        mainView.chkremoveprec.setSelected(removePCM);
        //settingsPnl.txtCutOff.setText(Integer.toString(ConfigHolder.getInstance().getInt("cut.off")));

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

        String temp = mainView.txtprecTolerance.getText();

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

        temp = mainView.txtfragTolerance.getText();
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

        String tempS = mainView.txtqueryspec.getText();
        if ("".equals(tempS)) {
            validationMessages.add("Please provide a spectra input directory.");
        } else if (!tempS.endsWith(".mgf") && !tempS.endsWith(".msp") && !tempS.endsWith(".mzML")
                && !tempS.endsWith(".mzXML") && !tempS.endsWith(".ms2")) {
            validationMessages.add(" Targer Spectra file type not valid");
        }

        if (!new File(tempS).exists()) {
            validationMessages.add("Query spectra file not existed.");
        }

        String tempS2 = mainView.txtlibspec.getText();
        if ("".equals(tempS2)) {
            validationMessages.add("Please select library file");
        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp") && !tempS2.endsWith(".sptxt")) {
            validationMessages.add(" Spectral library file type is invalid." + " \n " + "Only .mgf, .msp and .sptxt file format supported");
        }
        if (!new File(tempS2).exists()) {
            validationMessages.add("Library spectra file not existed.");
        }

        boolean file1Existed = false;
        boolean file2Existed = false;
        if (validationMessages.isEmpty()) {

            File f = new File(tempS);
            if (!f.equals(configData.getExperimentalSpecFile())) {
                configData.setExperimentalSpecFile(new File(tempS));
            } else {
                file1Existed = true;
            }
            f = new File(tempS2);
            if (!f.equals(configData.getSpecLibraryFile())) {
                configData.setSpecLibraryFile(new File(tempS2));
            } else {
                file2Existed = true;
            }

            if (file1Existed && file2Existed) {
                this.fileTobeConfigure = "";
            } else if (!file1Existed && !file2Existed) {
                this.fileTobeConfigure = "both";
            } else if (file1Existed && !file2Existed) {
                this.fileTobeConfigure = "libSpec";

            } else if (!file1Existed && file2Existed) {
                this.fileTobeConfigure = "expSpec";
            }
            //configData=new ConfigData(new File(tempS), new File(tempS2));

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
        int c = 0;

        try {
            if (result != null && !result.isEmpty() && result.get(targSpectrumNum).getMatchedLibSpec() != null && !result.get(targSpectrumNum).getMatchedLibSpec().isEmpty()) {

                ComparisonResult res = result.get(targSpectrumNum);
                Spectrum targSpec = res.getEspSpectrum();
                if (this.resultNumber < 0) {
                    this.resultNumber = 0;
                }
                Spectrum matchedSpec = res.getMatchedLibSpec().get(resultNumber).getSpectrum();
                double precMass_tar = targSpec.getPCMass();
                double precMass_match = matchedSpec.getPCMass();
                String targCharge = targSpec.getCharge_asStr();
                String matchedCharge = matchedSpec.getCharge_asStr();
                String tarName = targSpec.getTitle();
                String matchedName = matchedSpec.getTitle();

                ArrayList<Peak> peaks = targSpec.getPeakList();
                double[] mz_tar = new double[peaks.size()];
                double[] int_tar = new double[peaks.size()];
                c = 0;
                for (Peak p : peaks) {
                    mz_tar[c] = p.getMz();
                    int_tar[c] = p.getIntensity();
                    c++;
                }

                peaks = matchedSpec.getPeakList();
                double[] mz_matched = new double[peaks.size()];
                double[] int_matched = new double[peaks.size()];
                c = 0;
                for (Peak p : peaks) {
                    mz_matched[c] = p.getMz();
                    int_matched[c] = p.getIntensity();
                    c++;
                }
                SpectrumPanel spanel = new SpectrumPanel(mz_tar, int_tar, precMass_tar, targCharge, tarName);
                spanel.addMirroredSpectrum(mz_matched, int_matched, precMass_match, matchedCharge, matchedName, false, java.awt.Color.blue, java.awt.Color.cyan);

                mainView.pnlResultSpec.removeAll();
                mainView.pnlResultSpec.add(spanel);

//                mainView.pnlVisualResult.removeAll();
//                mainView.pnlVisualResult.add(spanel);
            } else {
                mainView.pnlResultSpec.removeAll();
            }

        } catch (Exception exception) {
            LOG.error(exception + " target spectrum number: " + Integer.toString(this.resultNumber + c));
        }

        mainView.pnlResultSpec.revalidate();
        mainView.pnlResultSpec.repaint();
    }

    /**
     * Fill table with target spectra
     */
    private void fillExpSpectraTable() {

        if (result != null) {

            tblModelTarget.setRowCount(0);
            int resultSize = 0;
            resultSize = result.size();// configData.getExpSpectraIndex().size();
            Spectrum expSpec;
            Object[] row = new Object[13];
            ComparisonResult res;
            MatchedLibSpectra matchedSpec;
            int row_index;
            for (int p = 0; p < resultSize; p++) {
                res = result.get(p);
                matchedSpec = res.getMatchedLibSpec().get(0);
                expSpec = res.getEspSpectrum();
                row_index=0;
                
                row[row_index++] = Integer.toString(p + 1);
                row[row_index++] = expSpec.getTitle();
                row[row_index++] = expSpec.getScanNumber();
                row[row_index++] = expSpec.getRtTime();
                row[row_index++] = expSpec.getPCMass();
                row[row_index++] = expSpec.getCharge_asStr();
                double score = result.get(p).getTopScore();
                row[row_index++] = Double.toString(score);
                row[row_index++]= Double.toString(result.get(p).getQval());
                
                if (configData.isDecoyAvailable()) {
                    row[row_index++] = Double.toString(res.getFDR());

                } else {
                    row[row_index++] = "NA";
                }

                row[row_index++] = expSpec.getNumPeaks();
                row[row_index++] = Integer.toString(matchedSpec.getTotalFilteredNumPeaks_Exp());
                row[row_index++] = Double.toString(matchedSpec.getSumFilteredIntensity_Exp());
                row[row_index++] = Double.toString(matchedSpec.getSumMatchedInt_Exp());
                row[row_index++] = Integer.toString(matchedSpec.getNumMatchedPeaks());

                tblModelTarget.addRow(row);
            }
        }

    }

    /**
     * fill table with best matched spectra
     *
     * @param target target spectrum index
     */
    public void fillBestmatchTable(int target) {

        if (result != null && !result.isEmpty() && result.get(target).getMatchedLibSpec() != null && !result.get(target).getMatchedLibSpec().isEmpty()) {
            this.targSpectrumNum = target;
            ComparisonResult res = result.get(this.targSpectrumNum);
            tblModelResult.setRowCount(0);

            Object[] row = new Object[13];
            List<MatchedLibSpectra> specs = res.getMatchedLibSpec();
            Spectrum spec;
            double score;

            String protein = "";
            for (MatchedLibSpectra mSpec : specs) {
                spec = mSpec.getSpectrum();
                score = mSpec.getScore();
                row[0] = spec.getTitle();
                row[1] = spec.getScanNumber();
                row[2] = spec.getSequence();

                protein = spec.getProtein();
                protein.replaceAll("^\"|\"$", "");
                if (!protein.isEmpty()) {
                    protein = protein.substring(1);
                }
                row[3] = protein;
                row[4] = spec.getModifications_asStr();

                row[5] = spec.getPCMass();
                row[6] = spec.getCharge_asStr();
                row[7] = Double.toString(score);
                row[8] = Integer.toString(mSpec.getSpectrum().getNumPeaks());
                row[9] = Integer.toString(mSpec.getTotalFilteredNumPeaks_Lib());
                row[10] = Double.toString(mSpec.getSumFilteredIntensity_Lib());
                row[11] = Double.toString(mSpec.getSumMatchedInt_Lib());
                row[12] = Double.toString(mSpec.getNumMatchedPeaks());
                tblModelResult.addRow(row);

            }

        } else {
            tblModelResult.setRowCount(0);
        }

        updateresultview(0);

    }

    /**
     * Updates progress bar value during the comparison process
     *
     * @param taskCompleted
     */
    @Override
    public void updateprogress(int taskCompleted, double PERCENT) {
        SwingUtilities.invokeLater(() -> {

            int v = (int) (taskCompleted * PERCENT);
            mainView.prgProgressBart.setValue(v);
            mainView.prgProgressBart.setString(Integer.toString(v) + "%");
        });
    }

    /**
     * update the input information area for target spectrum on GUI based on
     * user selected spectrum
     */
    public void updateInputInfo() throws JMzReaderException {
        int specNumber = spnModel.getNumber().intValue() - 1;
        if (configData.getExpSpecReader() != null) {
            Spectrum tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(specNumber).getPos());

            mainView.txtRetentionTime.setText(Double.toString(tSpec.getRtTime()));
            mainView.txtScanNum.setText(tSpec.getScanNumber());
            mainView.txtMaxMZ.setText(Double.toString(tSpec.getMaxMZ()));
            mainView.txtMinMZ.setText(Double.toString(tSpec.getMinMZ()));
            mainView.txtNumPeaks.setText(Integer.toString(tSpec.getNumPeaks()));
//            try {
//                spectrumDisplay(specNumber);
//            } catch (JMzReaderException ex) {
//                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            }

        } else if (configData.getEbiReader() != null) {
            JMzReader redr = configData.getEbiReader();
            uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec = redr.getSpectrumByIndex(specNumber + 1);
            String fileType = FilenameUtils.getExtension(configData.getExperimentalSpecFile().getName());
            MappingJmzSpectrum jmzMap = new MappingJmzSpectrum(fileType);

            Spectrum tSpec = jmzMap.getMappedSpectrum(jmzSpec);

            mainView.txtRetentionTime.setText(Double.toString(tSpec.getRtTime()));
            mainView.txtScanNum.setText(tSpec.getScanNumber());
            mainView.txtMaxMZ.setText(Double.toString(tSpec.getMaxMZ()));
            mainView.txtMinMZ.setText(Double.toString(tSpec.getMinMZ()));
            mainView.txtNumPeaks.setText(Integer.toString(tSpec.getNumPeaks()));
//            try {
//                spectrumDisplay(specNumber);
//            } catch (JMzReaderException ex) {
//                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            }

        }

        try {
            spectrumDisplay(specNumber);
        } catch (JMzReaderException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

    public void exportResults(int type) {
        try {
            ImportExport export = new ImportExport(result, configData);
            export.saveResult(type);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    public void importResults() {
        ImportExport importR = new ImportExport(result, configData);
        importR.importResult();

        if (result != null) {
            fillExpSpectraTable();
            fillBestmatchTable(0);
            displayResult();
        } else {
            clearGraphicArea();
        }
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
            ConfigHolder.getInstance().setProperty("db.spectra.path", configData.getSpecLibraryFile());
            ConfigHolder.getInstance().setProperty("target.spectra", configData.getExperimentalSpecFile());
        }

    }

    /**
     * this method changes library path
     */
    public void setLibraryPath() {

    }

    /**
     * opens dialog to choose target spectra file
     *
     * @param file the file source library or target
     */
    public void chooseTargetFile(String file) {

        JFileChooser fileChooser = new JFileChooser("C:/human_hcd/");
        fileChooser.setDialogTitle("Target Spectra File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String tempfile = fileChooser.getSelectedFile().getPath();
            tempfile = tempfile.replace('\\', '/');

            if (file.equals("target")) {
                mainView.txtqueryspec.setText(tempfile);
            } else {
                mainView.txtlibspec.setText(tempfile);
            }

        }
    }

    /**
     * display color raster of the whole spectra in the target dataset
     */
    /**
     * visual spectrum display of a selected query spectrum at the position
     * specIndex
     *
     * @param specIndex position of the spectrum to be visualized
     */
    public void spectrumDisplay(int specIndex) throws JMzReaderException {

        //targetView.pnlVizSpectrum.removeAll();
        mainView.pnlQuerySpecViz.removeAll();
        //SpecPanel spec = new SpecPanel(null);

        double[] mz = new double[0];
        double[] intensity = new double[0];
        double precMass = 0;
        String precCharge = "";
        String name = "";
        SpectrumPanel spanel = new SpectrumPanel(mz, intensity, precMass, precCharge, name);

        Spectrum tSpec = new Spectrum();
        if (configData.getExpSpectraIndex() != null || configData.getEbiReader() != null) {
            if (configData.getExpFileformat().equals("ms2io")) {
                tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(specIndex).getPos());
            } else if (configData.getExpFileformat().equals("ebi")) {
                JMzReader redr = configData.getEbiReader();
                uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec = redr.getSpectrumByIndex(specIndex + 1);

                String fileType = FilenameUtils.getExtension(configData.getExperimentalSpecFile().getName());
                MappingJmzSpectrum jmzMap = new MappingJmzSpectrum(fileType);
                tSpec = jmzMap.getMappedSpectrum(jmzSpec);
            }

            ArrayList<Peak> peaks = tSpec.getPeakList();
            try {
                int lenPeaks = peaks.size();
                mz = new double[lenPeaks];
                intensity = new double[lenPeaks];
                precMass = tSpec.getPCMass();
                precCharge = tSpec.getCharge_asStr();
                name = tSpec.getTitle();

                int c = 0;
                for (Peak p : peaks) {
                    mz[c] = p.getMz();
                    intensity[c] = p.getIntensity();
                    c++;
                }
                spanel = new SpectrumPanel(mz, intensity, precMass, precCharge, name);

            } catch (Exception ex) {

                System.out.println(ex);
            }

        }

        mainView.pnlQuerySpecViz.add(spanel);
        mainView.pnlQuerySpecViz.repaint();
        mainView.pnlQuerySpecViz.revalidate();

    }

    /**
     * clears the graphical area
     */
    private void clearGraphicArea() {
        mainView.pnlResultSpec.removeAll();
        tblModelTarget.setRowCount(0);
        tblModelResult.setRowCount(0);
    }

    int decoyType = 0;
    File libFile;

    /**
     * generate decoy library and append on the given spectral library file
     *
     * @param i : type of decoy generation technique; 0 if fixed mz value shift
     * 1 is random mz and intensity change of each peak in the spectrum
     * @param library path to library file
     */
    public void generateDeoy(int i) {
        decoyType = i;

        String tempS2 = mainView.txtlibspec.getText();
        if ("".equals(tempS2)) {
            LOG.info("Please select library file");

        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp") && !tempS2.endsWith(".sptxt")) {
            LOG.info(" Spectral library file type is invalid." + " \n " + "Only .mgf, .msp and .sptxt file format supported");
        } else {

            libFile = new File(tempS2);

            SwingDecoyGeneratorThread workerThread = new SwingDecoyGeneratorThread();
            workerThread.execute();

        }

    }

    public void annotateSpectrumFile(boolean overwriteOriginal) {
        String tempS2 = mainView.txtlibspec.getText();
        if ("".equals(tempS2)) {
            LOG.info("Please select library file");

        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp") && !tempS2.endsWith(".sptxt")) {
            LOG.info(" Spectral library file type is invalid." + " \n " + "Only .mgf, .msp and .sptxt file format supported");
        } else {

            libFile = new File(tempS2);

            SwingSpectrumAnnotatorThread workerThread = new SwingSpectrumAnnotatorThread();
            workerThread.execute();

        }

    }

    /**
     * swing thread to start the search and it runs on background
     */
    private class SwingWorkerThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            isBussy = true;
            mainView.btnStartSearch.setEnabled(false);

            result = null;
            LOG.info("COSS version 1.0");
            LOG.info("Query spectra: " + configData.getExperimentalSpecFile().toString());
            LOG.info("Library: " + configData.getSpecLibraryFile().toString());
            LOG.info("Search started ");

            result = dispatcher.dispatch();

            return null;

        }

        @Override
        protected void done() {

            try {

                if (cencelled) {

                    LOG.info("Process Cancelled.");
                    mainView.prgProgressBart.setValue(0);
                    mainView.prgProgressBart.setString(Integer.toString(0) + "%");

                } else if (result != null) {
                    LOG.info("Search Completed");
                    mainView.prgProgressBart.setValue(100);
                    mainView.prgProgressBart.setString(Integer.toString(100) + "%");

                    LOG.info("Total number of identified spectra: " + Integer.toString(result.size()));

                    if (!result.isEmpty() && configData.isDecoyAvailable()) {
                        Collections.sort(result);
                        Collections.reverse(result);
                        validateResult();
                        LOG.info("Number of validated identified spectra by COSS: " + Integer.toString(result.size()));

                        //Rescoring result using Percolator is the option is checked
                        //Rscoring is possible only if there is decoy in the library spectra
                        if (mainView.chkboxPercolator.isSelected()) {
                            LOG.info("rescoring the result with Percolator ... ");
                            Rescore rescore = new Rescore(result);
                            try {
                                boolean finished = rescore.start_rescoring(configData.getExperimentalSpecFile().toString());
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
                            }finally{
                                
                                List<ComparisonResult> temp = new ArrayList<>();                                
                                List<Integer> newIndex= new ArrayList<Integer>(rescore.rescored_result.keySet()) ;
                                double rescored_score;
                                double q_val;
                                String[] splits;
                                for(int i=0;i<newIndex.size();i++){
                                    
                                    temp.add(i, result.get(newIndex.get(i)));
                                    splits = rescore.rescored_result.get(newIndex.get(i)).split(",");
                                    rescored_score = Double.parseDouble(splits[0]);
                                    q_val = Double.parseDouble(splits[1]);
                                    
                                    temp.get(i).setTopScore(rescored_score);
                                    temp.get(i).setQval(q_val);
                                }
                                
                                result=temp;
                            }
                        }

                    } else if (!configData.isDecoyAvailable()) {
                        LOG.info("No decoy spectra found in library");
                    }

                    fillExpSpectraTable();
                    fillBestmatchTable(0);
                    displayResult();
                    get();

                } else {
                    LOG.info("No comparison result.");
                    clearGraphicArea();
                }

                isBussy = false;
                mainView.btnStartSearch.setEnabled(true);

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
            mainView.btnConfigSecReader.setEnabled(false);
            mainView.btnStartSearch.setEnabled(false);

            LOG.info("Configuring Spectrum Reader ....");
            ConfigSpecReaders cfReader = new ConfigSpecReaders(configData);
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
                    mainView.txtTotalSpec.setText("/" + Integer.toString(expSpecSize));

                    try {
                        updateInputInfo();
                        spectrumDisplay(0);

                        // rasterDisplay();
                    } catch (JMzReaderException ex) {
                        java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                } else {
                    LOG.info("Null Spectrum Index");
                }

                isReaderReady = true;
                isBussy = false;
                mainView.btnConfigSecReader.setEnabled(true);
                mainView.btnStartSearch.setEnabled(true);
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
     * swing thread generating decoy
     */
    private class SwingSpectrumAnnotatorThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            // isBussy = true;        
            mainView.btnConfigSecReader.setEnabled(false);
            mainView.btnStartSearch.setEnabled(false);

            LOG.info("Annotating spectrum file....");
            Annotation ann = new Annotation(libFile, 0.05);
            try {
                ann.annotateSpecFile(false);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ReverseSequence.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(ReverseSequence.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                java.util.logging.Logger.getLogger(ReverseSequence.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            return null;
        }

        @Override
        protected void done() {

            try {

                LOG.info("Spectrum file annotation is completed");
                isReaderReady = true;
                isBussy = false;
                mainView.btnConfigSecReader.setEnabled(true);
                mainView.btnStartSearch.setEnabled(true);

            } catch (CancellationException ex) {
                LOG.info("the spectrum similarity score pipeline run was cancelled");
            } finally {

            }
        }

    }

    /**
     * swing thread generating decoy
     */
    private class SwingDecoyGeneratorThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            // isBussy = true;        
            mainView.btnConfigSecReader.setEnabled(false);
            mainView.btnStartSearch.setEnabled(false);

            LOG.info("Generating decoy ....");
            GenerateDecoy gen = null;
            switch (decoyType) {
                case 0:
                    gen = new ReverseSequence(libFile, LOG);
                    gen.generate();
                    break;

                case 1:
                    gen = new RandomSequene(libFile, LOG);
                    gen.generate();
                    break;

                case 2:
                    gen = new FixedPeakShift(libFile, LOG);
                    gen.generate();
                    break;

                case 3:
                    gen = new RandomPeaks(libFile, LOG);
                    gen.generate();
                    break;

                case 4:
                    gen = new PrecursorSwap(libFile, LOG);
                    gen.generate();
                    break;

            }

            return null;
        }

        @Override
        protected void done() {

            try {

                LOG.info("Decoy library generation completed");
                isReaderReady = true;
                isBussy = false;
                mainView.btnConfigSecReader.setEnabled(true);
                mainView.btnStartSearch.setEnabled(true);

            } catch (CancellationException ex) {
                LOG.info("the spectrum similarity score pipeline run was cancelled");
            } finally {

            }
        }

    }
}
