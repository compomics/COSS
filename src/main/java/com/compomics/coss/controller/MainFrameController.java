package com.compomics.coss.controller;

import com.compomics.coss.controller.SpectrumAnnotation.Annotation;
import com.compomics.ms2io.model.Spectrum;
import com.compomics.ms2io.model.Peak;
import com.compomics.coss.controller.decoyGeneration.*;
import com.compomics.coss.view.ResultPanel;
import com.compomics.coss.view.RasterPanel;
import com.compomics.coss.view.querySpectum_View;
import com.compomics.coss.view.SettingPanel;
import com.compomics.coss.view.MainGUI;
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
import com.compomics.ms2io.*;
import java.awt.BorderLayout;
import com.compomics.util.gui.spectrum.SpectrumPanel;
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
    private MainGUI mainView;
    private SettingPanel settingsPnl;
    private ResultPanel resultPnl;
    private querySpectum_View quryspecView;

    // private ConfigHolder config = new ConfigHolder();
    Dispatcher dispatcher;
    private static List<ComparisonResult> result = null;

    ConfigData configData;
    public boolean cencelled = false;
    public boolean isBussy = false;
    String fileTobeConfigure;
    boolean isSettingSame;

    public DefaultTableModel tblModelResult;
    public DefaultTableModel tblModelQuery;
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
        String libPath = ConfigHolder.getInstance().getString("spectra.library.path");
        settingsPnl = new SettingPanel(this, new File(libPath));
        resultPnl = new ResultPanel(this);
        quryspecView = new querySpectum_View(this);

        mainView = new MainGUI(settingsPnl, resultPnl, quryspecView, this);
        mainView.pnlCommands.setLayout(new BorderLayout());

        // add gui appender
        LogTextAreaAppender logTextAreaAppender = new LogTextAreaAppender();
        logTextAreaAppender.setLogArea(mainView);

        logTextAreaAppender.setThreshold(Priority.INFO);
        logTextAreaAppender.setImmediateFlush(true);
        PatternLayout layout = new org.apache.log4j.PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} - %m%n");
        logTextAreaAppender.setLayout(layout);

        //Initializing result tables
        final String[] colNamesRes = {"Title", "ScanNo", "Sequence", "Protein","Mods", "M/Z", "Charge", "Score", "#Peaks", "#FiltedPeaks", "TotalInt", "MatchedInt", "#MatchedPeaks"};
        final String[] colNamesExperimental = {"No. ", "Title", "ScanNo", "RetentionT", "M/Z", "Charge", "Score", "Validation(FDR)", "#Peaks", "#FilteredPeaks", "TotalInt", "MatchedInt", "#MatchedPeaks"};

        tblModelResult = new DefaultTableModel(colNamesRes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblModelQuery = new DefaultTableModel(colNamesExperimental, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        spnModel = new SpinnerNumberModel(1, 1, 1, 1);
        quryspecView.spnSpectrum.setModel(spnModel);

        resultPnl.tblQuerySpectra.setModel(tblModelQuery);
        resultPnl.tblBestMatch.setModel(tblModelResult);

        //cleaning display area
        spectrumDisplay(0);
        rasterDisplay();

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

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainView.setBounds(0, 0, screenSize.width, screenSize.height - 40);
        mainView.setMinimumSize(new Dimension(530, 600));
        mainView.setVisible(true);

    }

    /**
     * stop the system safely
     */
    public void stopSearch() {

        if (dispatcher != null) {
            this.cencelled = true;
            dispatcher.stopMatching();
            mainView.setProgressValue(0);
            //mainView.setProgressValue("");
            mainView.searchBtnActive(true);
            mainView.readerBtnActive(true);

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
            mainView.setProgressValue(0);
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
            mainView.searchBtnActive(false);
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
        int scoringFun = settingsPnl.cmbScoringFun.getSelectedIndex();

        double precTolerance = Double.parseDouble(settingsPnl.txtPrecursorTolerance.getText());
        double fragTolerance = Double.parseDouble(settingsPnl.txtFragmentTolerance.getText());

        boolean applyTransform = settingsPnl.chkTransform.isSelected();
        int transformType = settingsPnl.cmbTransformType.getSelectedIndex();
        boolean applyFilter = settingsPnl.chkFilter.isSelected();
        int filterType = settingsPnl.cmbFilterType.getSelectedIndex();
        int massWindow = Integer.parseInt(settingsPnl.txtMassWindow.getText());;
        int cutOff = Integer.parseInt(settingsPnl.txtCutOff.getText());
        boolean removePCM = settingsPnl.chkRemovePrecursor.isSelected();

        if (settingsPnl.cmbFragTolUnit.getSelectedIndex() != 0) {//if in PPM 
            fragTolerance /= (double) 1000000;
        }
        if (settingsPnl.cmbPrcTolUnit.getSelectedIndex() != 0) { //if in PPM
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
            configData.applyFilter(applyFilter);
            configData.setFilterType(filterType);
            configData.setCutOff(cutOff);
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
        settingsPnl.txtqueryspec.setText(ConfigHolder.getInstance().getString("query.spectra.path"));
        settingsPnl.txtLibrary.setText(ConfigHolder.getInstance().getString("spectra.library.path"));

        //Scoring function
        settingsPnl.cmbScoringFun.setSelectedIndex(ConfigHolder.getInstance().getInt("matching.algorithm"));

        //MS instrument based settings
        settingsPnl.txtPrecursorTolerance.setText(Double.toString(ConfigHolder.getInstance().getDouble("precursor.tolerance")));
        settingsPnl.txtFragmentTolerance.setText(Double.toString(ConfigHolder.getInstance().getDouble("fragment.tolerance")));

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

        settingsPnl.txtMassWindow.setText(Integer.toString(ConfigHolder.getInstance().getInt("mass.window")));
        settingsPnl.chkFilter.setSelected(applyFilter);
        settingsPnl.chkTransform.setSelected(applyTransform);
        settingsPnl.cmbFilterType.setSelectedIndex(ConfigHolder.getInstance().getInt("filter.type"));
        settingsPnl.cmbTransformType.setSelectedIndex(ConfigHolder.getInstance().getInt("transform.type"));
        settingsPnl.chkRemovePrecursor.setSelected(removePCM);
        settingsPnl.txtCutOff.setText(Integer.toString(ConfigHolder.getInstance().getInt("cut.off")));

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

        String tempS = settingsPnl.txtqueryspec.getText();
        if ("".equals(tempS)) {
            validationMessages.add("Please provide a spectra input directory.");
        } else if (!tempS.endsWith(".mgf") && !tempS.endsWith(".msp") && !tempS.endsWith(".mzML")
                && !tempS.endsWith(".mzXML") && !tempS.endsWith(".ms2")) {
            validationMessages.add(" Targer Spectra file type not valid");
        }

        if (!new File(tempS).exists()) {
            validationMessages.add("Query spectra file not existed.");
        }

        String tempS2 = settingsPnl.txtLibrary.getText();
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

                resultPnl.pnlVisualSpectrum.removeAll();
                resultPnl.pnlVisualSpectrum.add(spanel);

            } else {
                resultPnl.pnlVisualSpectrum.removeAll();
            }

        } catch (Exception exception) {
            LOG.error(exception + "spectrum number: " + Integer.toString(this.resultNumber+c));
        }

        resultPnl.pnlVisualSpectrum.revalidate();
        resultPnl.pnlVisualSpectrum.repaint();
    }

    /**
     * Fill table with query spectra
     */
    private void fillExpSpectraTable() {

        if (result != null) {

            tblModelQuery.setRowCount(0);
            int resultSize = 0;
            resultSize = result.size();// configData.getExpSpectraIndex().size();
            Spectrum expSpec;
            Object[] row = new Object[13];
            ComparisonResult res;
            MatchedLibSpectra matchedSpec;

            for (int p = 0; p < resultSize; p++) {
                res = result.get(p);
                matchedSpec = res.getMatchedLibSpec().get(0);
                expSpec = res.getEspSpectrum();
                row[0] = Integer.toString(p + 1);
                row[1] = expSpec.getTitle();
                row[2] = expSpec.getScanNumber();
                row[3] = expSpec.getRtTime();
                row[4] = expSpec.getPCMass();
                row[5] = expSpec.getCharge_asStr();
                double score = result.get(p).getTopScore();
                row[6] = Double.toString(score);

                if (configData.isDecoyAvailable()) {
                    row[7] = Double.toString(res.getFDR());

                } else {
                    row[7] = "NA";
                }

                row[8] = expSpec.getNumPeaks();
                row[9] = Integer.toString(matchedSpec.getTotalFilteredNumPeaks_Exp());
                row[10] = Double.toString(matchedSpec.getSumFilteredIntensity_Exp());
                row[11] = Double.toString(matchedSpec.getSumMatchedInt_Exp());
                row[12] = Integer.toString(matchedSpec.getNumMatchedPeaks());

                tblModelQuery.addRow(row);
            }
        }

    }

    /**
     * fill table with best matched spectra
     *
     * @param queryIndex spectrum index
     */
    public void fillBestmatchTable(int queryIndex) {

        if (result != null && !result.isEmpty() && result.get(queryIndex).getMatchedLibSpec() != null && !result.get(queryIndex).getMatchedLibSpec().isEmpty()) {
            this.targSpectrumNum = queryIndex;
            ComparisonResult res = result.get(this.targSpectrumNum);
            tblModelResult.setRowCount(0);

            Object[] row = new Object[13];
            List<MatchedLibSpectra> specs = res.getMatchedLibSpec();
            Spectrum spec;
            double score;

            String protein="";
            for (MatchedLibSpectra mSpec : specs) {
                spec = mSpec.getSpectrum();
                score = mSpec.getScore();
                row[0] = spec.getTitle();
                row[1] = spec.getScanNumber();
                row[2] = spec.getSequence();
                
                protein = spec.getProtein();
                protein.replaceAll("^\"|\"$", "");
                if(!protein.isEmpty()){
                    protein=protein.substring(1);
                }
                row[3] = protein;
                row[4]= spec.getModifications_asStr();
                
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
            mainView.setProgressValue(v);
            mainView.setProgressValue(Integer.toString(v) + "%");
        });
    }

    /**
     * update the input information area for query spectrum on GUI based on
     * user selected spectrum
     */
    public void updateInputInfo() throws JMzReaderException {
        int specNumber = spnModel.getNumber().intValue() - 1;
        if (configData.getExpSpecReader() != null) {
            Spectrum tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(specNumber).getPos());

            quryspecView.txtRtTime.setText(Double.toString(tSpec.getRtTime()));
            quryspecView.txtScanno.setText(tSpec.getScanNumber());
            quryspecView.txtmaxmz.setText(Double.toString(tSpec.getMaxMZ()));
            quryspecView.txtminmz.setText(Double.toString(tSpec.getMinMZ()));
            quryspecView.txtnumpeaks.setText(Integer.toString(tSpec.getNumPeaks()));
            try {
                spectrumDisplay(specNumber);
            } catch (JMzReaderException ex) {
                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

        } else if (configData.getEbiReader() != null) {
            JMzReader redr = configData.getEbiReader();
            uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec = redr.getSpectrumByIndex(specNumber + 1);
            String fileType = FilenameUtils.getExtension(configData.getExperimentalSpecFile().getName());
            MappingJmzSpectrum jmzMap = new MappingJmzSpectrum(fileType);

            Spectrum tSpec = jmzMap.getMappedSpectrum(jmzSpec);

            quryspecView.txtRtTime.setText(Double.toString(tSpec.getRtTime()));
            quryspecView.txtScanno.setText(tSpec.getScanNumber());
            quryspecView.txtmaxmz.setText(Double.toString(tSpec.getMaxMZ()));
            quryspecView.txtminmz.setText(Double.toString(tSpec.getMinMZ()));
            quryspecView.txtnumpeaks.setText(Integer.toString(tSpec.getNumPeaks()));
            try {
                spectrumDisplay(specNumber);
            } catch (JMzReaderException ex) {
                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

        }

        try {
            spectrumDisplay(specNumber);
        } catch (JMzReaderException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * Update the visual result panel based on the selection of the query spectrum
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
            ConfigHolder.getInstance().setProperty("query.spectra", configData.getExperimentalSpecFile());
        }

    }


    /**
     * opens dialog to choose query or library spectra file
     *
     * @param file the file library or query
     */
    public void chooseFile(String file) {

        JFileChooser fileChooser = new JFileChooser("C:/");
        fileChooser.setDialogTitle("Query Spectra File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String tempfile = fileChooser.getSelectedFile().getPath();
            tempfile = tempfile.replace('\\', '/');

            if (file.equals("query")) {
                settingsPnl.txtqueryspec.setText(tempfile);
            } else {
                settingsPnl.txtLibrary.setText(tempfile);
            }

        }
    }

    /**
     * display color raster of the whole spectra in the query dataset
     */
    public void rasterDisplay() {
        quryspecView.pnlRaster.removeAll();
        RasterPanel drawingPanel = null;
        if (configData.getExpSpectraIndex() != null) {

            drawingPanel = new RasterPanel(configData.getExpSpectraIndex(), configData.getExpSpecReader());

            drawingPanel.setPreferredSize(new Dimension(600, 300));
            quryspecView.pnlRaster.add(drawingPanel);
            quryspecView.pnlRaster.repaint();
            quryspecView.pnlRaster.revalidate();
        }

    }

    /**
     * visual spectrum display of a selected query spectrum at the position specIndex
     *
     * @param specIndex position of the spectrum to be visualized
     */
    public void spectrumDisplay(int specIndex) throws JMzReaderException {

        quryspecView.pnlVizSpectrum.removeAll();
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

        quryspecView.pnlVizSpectrum.add(spanel);
        quryspecView.pnlVizSpectrum.repaint();
        quryspecView.pnlVizSpectrum.revalidate();

    }

    /**
     * clears the graphical area
     */
    private void clearGraphicArea() {
        resultPnl.pnlVisualSpectrum.removeAll();
        tblModelQuery.setRowCount(0);
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

        String tempS2 = settingsPnl.txtLibrary.getText();
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
        String tempS2 = settingsPnl.txtLibrary.getText();
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

//    public void mergeFiles() {
//        String tempS = settingsPnl.txtLibrary.getText();
//        String tempS2 = settingsPnl.txtqueryspec.getText();
//
//        if ("".equals(tempS2) || "".equals(tempS)) {
//            LOG.info("Please give files to be merged");
//
//        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp") && !tempS2.endsWith(".sptxt") && !tempS.endsWith(".mgf") && !tempS.endsWith(".msp") && !tempS.endsWith(".sptxt")) {
//            LOG.info(" Spectral file type is invalid." + " \n " + "Only .mgf, .msp and .sptxt file format supported");
//        } else {
//            MergeFiles merg = new MergeFiles(new File(tempS), new File(tempS2));
//            try {
//                merg.Merge();
//            } catch (InterruptedException ex) {
//                java.util.logging.Logger.getLogger(ReverseSequence.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            } finally {
//
//            }
//
//        }
//
//    }
    /**
     * swing thread to start the search and it runs on background
     */
    private class SwingWorkerThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            isBussy = true;
            mainView.searchBtnActive(false);

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
                    mainView.setProgressValue(0);
                    mainView.setProgressValue(Integer.toString(0) + "%");

                } else if (result != null) {
                    LOG.info("Search Completed");
                    mainView.setProgressValue(100);
                    mainView.setProgressValue(Integer.toString(100) + "%");

                    LOG.info("Total number of identified spectra: " + Integer.toString(result.size()));

                    if (!result.isEmpty() && configData.isDecoyAvailable()) {
                        Collections.sort(result);
                        Collections.reverse(result);
                        validateResult();
                        LOG.info("Number of validated identified spectra: " + Integer.toString(result.size()));
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
                mainView.searchBtnActive(true);

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
                    quryspecView.txtTotalSpec.setText("/" + Integer.toString(expSpecSize));

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

    /**
     * swing thread generating decoy
     */
    private class SwingSpectrumAnnotatorThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            // isBussy = true;        
            mainView.readerBtnActive(false);
            mainView.searchBtnActive(false);

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
                mainView.readerBtnActive(true);
                mainView.searchBtnActive(true);

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
            mainView.readerBtnActive(false);
            mainView.searchBtnActive(false);

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
                mainView.readerBtnActive(true);
                mainView.searchBtnActive(true);

            } catch (CancellationException ex) {
                LOG.info("the spectrum similarity score pipeline run was cancelled");
            } finally {

            }
        }

    }
}
