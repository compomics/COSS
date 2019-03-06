package com.compomics.coss.controller;

import com.compomics.coss.controller.decoyGeneration.Generate;
import com.compomics.coss.view.ResultPanel;
import com.compomics.coss.view.RasterPanel;
import com.compomics.coss.view.TargetDB_View;
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
    Dispartcher dispatcher;
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
    public void init() {

        configData = new ConfigData();
        fileTobeConfigure = "both"; //asume both query and library files to be read
        isSettingSame = false;
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

        //Initializing result tables
        final String[] colNamesRes = {"Title", "Scan Num.", "Sequence", "Protein", "M/Z", "Charge", "Score", "#Peaks", "#FiltedPeaks", "TotalInt", "MatchedInt", "#MatchedPeaks"};
        final String[] colNamesExperimental = {"No. ", "Title", "Scan", "M/Z", "Charge", "Score", "Validation(FDR)", "#Peaks", "#FilteredPeaks", "TotalInt", "MatchedInt", "#MatchedPeaks"};

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
        targetView.spnSpectrum.setModel(spnModel);

        resultPnl.tblTargetSpectra.setModel(tblModelTarget);
        resultPnl.tblBestMatch.setModel(tblModelResult);

        //cleaning display area
        spectrumDisplay(0);
        rasterDisplay();

        LOG.addAppender(logTextAreaAppender);
        LOG.setLevel((Level) Level.INFO);
        LoadData();//Read input configData from file and put on GUI

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
            dispatcher = new Dispartcher(this.configData, this, LOG);
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

        int maxPrecCharg = Integer.parseInt(settingsPnl.txtPrecursorCharge.getText());
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
                && configData.getMaxPrecursorCharg() == maxPrecCharg
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
            configData.setMaxPrecursorCharg(maxPrecCharg);
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
        settingsPnl.txttargetspec.setText(ConfigHolder.getInstance().getString("target.spectra.path"));
        settingsPnl.txtLibrary.setText(ConfigHolder.getInstance().getString("spectra.library.path"));

        //Scoring function
        settingsPnl.cmbScoringFun.setSelectedIndex(ConfigHolder.getInstance().getInt("matching.algorithm"));

        //MS instrument based settings
        settingsPnl.txtPrecursorCharge.setText(Integer.toString(ConfigHolder.getInstance().getInt("max.charge")));
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
        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp")  && !tempS2.endsWith(".sptxt") ) {
            validationMessages.add(" Spectral library file type is invalid." + " \n " + "Only .mgf, .msp and .sptxt file format supported");
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

        try {
            if (result != null && !result.isEmpty() && result.get(targSpectrumNum).getMatchedLibSpec() != null && !result.get(targSpectrumNum).getMatchedLibSpec().isEmpty()) {

                ComparisonResult res = result.get(targSpectrumNum);
                Spectrum targSpec = res.getEspSpectrum();
                if (this.resultNumber < 0) {
                    this.resultNumber = 0;
                }
                Spectrum matchedSpec = res.getMatchedLibSpec().get(resultNumber).getSpectrum();

                int sizeTarget = targSpec.getNumPeaks();
                int sizeMatched = matchedSpec.getNumPeaks();
                double[] mz_tar = new double[sizeTarget];
                double[] int_tar = new double[sizeTarget];
                double[] mz_matched = new double[sizeMatched];
                double[] int_matched = new double[sizeMatched];
                ArrayList<Peak> peaks = targSpec.getPeakList();
                double precMass_tar = targSpec.getPCMass();
                double precMass_match = matchedSpec.getPCMass();
                String targCharge = targSpec.getCharge();
                String matchedCharge = matchedSpec.getCharge();
                String tarName = targSpec.getTitle();
                String matchedName = matchedSpec.getTitle();

                int c = 0;
                for (Peak p : peaks) {
                    mz_tar[c] = p.getMz();
                    int_tar[c] = p.getIntensity();
                    c++;
                }
                peaks = matchedSpec.getPeakList();
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
            LOG.error(exception + " target spectrum number: " + Integer.toString(this.resultNumber));
        }

        resultPnl.pnlVisualSpectrum.revalidate();
        resultPnl.pnlVisualSpectrum.repaint();
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
            Object[] row = new Object[12];
            ComparisonResult res;
            MatchedLibSpectra matchedSpec;

            for (int p = 0; p < resultSize; p++) {
                res = result.get(p);
                matchedSpec = res.getMatchedLibSpec().get(0);
                expSpec = res.getEspSpectrum();

                row[0] = Integer.toString(p + 1);
                row[1] = expSpec.getTitle();
                row[2] = expSpec.getScanNumber();
                row[3] = expSpec.getPCMass();
                row[4] = expSpec.getCharge();
                double score = result.get(p).getTopScore();
                row[5] = Double.toString(score);

                if (configData.isDecoyAvailable()) {
                    row[6] = Double.toString(res.getFDR());

                } else {
                    row[6] = "NA";
                }

                row[7] = expSpec.getNumPeaks();
                row[8] = Integer.toString(matchedSpec.getTotalFilteredNumPeaks_Exp());
                row[9] = Double.toString(matchedSpec.getSumFilteredIntensity_Exp());
                row[10] = Double.toString(matchedSpec.getSumMatchedInt_Exp());
                row[11] = Integer.toString(matchedSpec.getNumMatchedPeaks());

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

            Object[] row = new Object[12];
            List<MatchedLibSpectra> specs = res.getMatchedLibSpec();
            Spectrum spec;
            double score;

            for (MatchedLibSpectra mSpec : specs) {
                spec = mSpec.getSpectrum();
                score = mSpec.getScore();
                row[0] = spec.getTitle();
                row[1] = spec.getScanNumber();
                row[2] = spec.getSequence();
                row[3] = spec.getProtein();
                row[4] = spec.getPCMass();
                row[5] = spec.getCharge();
                row[6] = Double.toString(score);
                row[7] = Integer.toString(mSpec.getSpectrum().getNumPeaks());
                row[8] = Integer.toString(mSpec.getTotalFilteredNumPeaks_Lib());
                row[9] = Double.toString(mSpec.getSumFilteredIntensity_Lib());
                row[10] = Double.toString(mSpec.getSumMatchedInt_Lib());
                row[11] = Double.toString(mSpec.getNumMatchedPeaks());
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
    public void updateprogress(int taskCompleted) {

        final double PERCENT = 100.0 / (double) configData.getExpSpectraIndex().size();
        SwingUtilities.invokeLater(() -> {

            int v = (int) (taskCompleted * PERCENT);
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

    public void exportResults(int type) {
        try {
            ImportExport export=new ImportExport(result, configData);
            export.saveResult(type);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        

    }

    public void importResults() {
        ImportExport importR=new ImportExport(result, configData);
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
            ConfigHolder.getInstance().setProperty("max.charge", configData.getMaxPrecursorCharg());
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

        JFileChooser fileChooser = new JFileChooser("C:/1_pandy_datasets/");
        fileChooser.setDialogTitle("Target Spectra File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String tempfile = fileChooser.getSelectedFile().getPath();
            tempfile = tempfile.replace('\\', '/');

            if (file.equals("target")) {
                settingsPnl.txttargetspec.setText(tempfile);
            } else {
                settingsPnl.txtLibrary.setText(tempfile);
            }

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
        //SpecPanel spec = new SpecPanel(null);

        double[] mz = new double[0];
        double[] intensity = new double[0];
        double precMass = 0;
        String precCharge = "";
        String name = "";
        SpectrumPanel spanel = new SpectrumPanel(mz, intensity, precMass, precCharge, name);

        if (configData.getExpSpectraIndex() != null) {
            Spectrum tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(specIndex).getPos());
            ArrayList<Peak> peaks = tSpec.getPeakList();
            int lenPeaks = peaks.size();
            mz = new double[lenPeaks];
            intensity = new double[lenPeaks];
            precMass = tSpec.getPCMass();
            precCharge = tSpec.getCharge();
            name = tSpec.getTitle();

            int c = 0;
            for (Peak p : peaks) {
                mz[c] = p.getMz();
                intensity[c] = p.getIntensity();
                c++;
            }
            spanel = new SpectrumPanel(mz, intensity, precMass, precCharge, name);
            //spanel.addMirroredSpectrum(d.getSpectra2().get(bestResultIndex).getMzValuesAsArray(), d.getSpectra2().get(bestResultIndex).getIntensityValuesAsArray(), 500, "+2", cf_data.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            // resultView.getSpltPanel().add(specPanel);
        }

        //spec.setPreferredSize(new Dimension(700, 300));
        targetView.pnlVizSpectrum.add(spanel);
        targetView.pnlVizSpectrum.repaint();
        targetView.pnlVizSpectrum.revalidate();

    }

    /**
     * clears the graphical area
     */
    private void clearGraphicArea() {
        resultPnl.pnlVisualSpectrum.removeAll();
        tblModelTarget.setRowCount(0);
        tblModelResult.setRowCount(0);
    }

    int decoyType = 0;
    Generate gn = null;
    File libFile = null;

    /**
     * generate decoy library and append on the given spectral library file
     *
     * @param i : type of decoy generation technique; 0 if fixed mz value shift
     * 1 is random mz and intensity change of each peak in the spectrum
     * @param library path to library file
     */
    public void generateDeoy(int i, String library) {
        
        if ("".equals(library)) {
            showMessageDialog("Validation errors", "No spectra library given", JOptionPane.WARNING_MESSAGE);

        } else if (!library.endsWith(".mgf") && !library.endsWith(".msp")) {
            showMessageDialog("Validation errors", "Spectral library file format not supported", JOptionPane.WARNING_MESSAGE);
        } else {

            libFile = new File(library);
            gn = new Generate(LOG, this);
            SwingDecoyGeneratorThread workerThread = new SwingDecoyGeneratorThread();
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

                } else {
                    LOG.info("Search Completed");
                    mainView.setProgressValue(100);
                    mainView.setProgressValue(Integer.toString(100) + "%");

                    if (result != null) {

                        LOG.info("Total number of identified spectra: " + Integer.toString(result.size()));

                        if (configData.isDecoyAvailable()) {
                            validateResult();
                            LOG.info("Number of validated identified spectra: " + Integer.toString(result.size()));
                        } else {
                            LOG.info("No decoy spectra found in library");
                        }
                        fillExpSpectraTable();
                        fillBestmatchTable(0);
                        displayResult();

                    } else {
                        LOG.info("No comparison result.");
                        clearGraphicArea();
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

            gn.start(libFile, decoyType);
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
