package com.compomics.coss.Controller;

import com.compomics.coss.Model.ComparisonResult;
import com.compomics.coss.Model.ConfigData;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import com.compomics.coss.Model.ConfigHolder;
import com.compomics.coss.View.SpecPanel;
import com.compomics.coss.View.MainGUI;
import com.compomics.coss.View.ResultPanel;
import com.compomics.coss.View.SettingPanel;
import com.compomics.coss.View.RasterPanel;
import com.compomics.coss.View.TargetDB_View;
import com.compomics.matching.Cascade;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.SwingUtilities;
import com.compomics.matching.Matching;
import com.compomics.matching.UseMsRoben;
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
//import org.jfree.util.Log;
import com.compomics.ms2io.IndexKey;
import com.compomics.ms2io.Indexer;
import com.compomics.ms2io.MgfReader;
import com.compomics.ms2io.MspReader;
import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.SpectrumReader;
import java.util.Collections;

/**
 * Controller class for GUI
 *
 * @author Genet
 */
public class MainFrameController implements UpdateListener {

    private static final Logger LOG = Logger.getLogger(MainFrameController.class);
    private SwingWorkerThread workerThread;

    /**
     * Objects of the views
     */
    private MainGUI mainView;
    private SettingPanel settingsPnl;
    private ResultPanel resultPnl;
    private TargetDB_View targetView;

    // private ConfigHolder config = new ConfigHolder();
    Matching matching;
    private static List<ArrayList<ComparisonResult>> res;
    ConfigData configData;
    public boolean cencelled = false;
    public boolean isBussy = false;

    public DefaultTableModel tblModelResult;
    public DefaultTableModel tblModelTarget;
    public SpinnerNumberModel spnModel;
    public DefaultComboBoxModel cmbModel;

    private int targSpectrumNum, resultNumber;

    /**
     * Initialize objects, variables and components.
     */
    //<editor-fold  defaultstate="Colapsed" desc="Initialize Components">
    public void init() {

        String libPath = ConfigHolder.getInstance().getString("spectra.library.path");
        settingsPnl = new SettingPanel(this, new File(libPath));
        resultPnl = new ResultPanel(this);
        targetView = new TargetDB_View(this);
        mainView = new MainGUI(settingsPnl, resultPnl, targetView, this);

        configData = new ConfigData();
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
        final String[] colNamesRes = {"No.", "ID", "Name/Title", "M/Z", "Charge", "Score", "Confidence(%)"};
        final String[] colNamesTar = {"No.", "ID", "Name/Title", "M/Z", "Charge", "No. Peaks"};

        tblModelResult = new DefaultTableModel(colNamesRes, 0);
        tblModelTarget = new DefaultTableModel(colNamesTar, 0);

        spnModel = new SpinnerNumberModel(1, 1, 1, 1);
        targetView.spnSpectrum.setModel(spnModel);

        resultPnl.tblTargetSpectra.setModel(tblModelTarget);
        resultPnl.tblBestMatch.setModel(tblModelResult);

        //cmbModel = new DefaultComboBoxModel();
        //settingsPnl.cboSpectraLibrary.setModel(cmbModel);
        mainView.prgProgress.setStringPainted(true);
        mainView.prgProgress.setForeground(Color.BLUE);
        spectrumDisplay(0);
        rasterDisplay();

        LOG.addAppender(logTextAreaAppender);
        LOG.setLevel((Level) Level.INFO);
        LoadData();

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

        this.cencelled = true;
        matching.stopMatching();

    }

    /**
     * Start Spectrum searching upon user click on start button
     */
    public void startSearch() {

        //check for input validation and display if one or more infalid value found
        List<String> validationMessages = validateInput();
        if (!validationMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (String validationMessage : validationMessages) {
                message.append(validationMessage).append(System.lineSeparator());
            }
            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
        } else {
            ReadInputData();
            readSpectra();
            this.cencelled = false;
            this.isBussy = true;
            matching = new UseMsRoben(this, configData.getExperimentalSpecFile(), configData.getSpecLibraryFile());
            // matching = new Cascade(this);

            mainView.prgProgress.setValue(0);
            workerThread = new SwingWorkerThread();
            workerThread.execute();

        }

        try {

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(com.compomics.main.ProjectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    /**
     * Read user input from GUI
     *
     */
    private void ReadInputData() {

        configData.setScoringFunction(settingsPnl.cboScoringFun.getSelectedIndex());
        configData.setMaxPrecursorCharg(Integer.parseInt(settingsPnl.txtPrecursorCharge.getText()));
        configData.setPrecTol(Double.parseDouble(settingsPnl.txtPrecursorTolerance.getText()));
        configData.setfragTol(Double.parseDouble(settingsPnl.txtFragmentTolerance.getText()));
        configData.setExperimentalSpecFile(settingsPnl.txttargetspec.getText());
        String path = settingsPnl.txtLibrary.getText();
        configData.setSpecLibraryFile(path);

    }

    /**
     * Initiates spectra reading from the specified directory, draws the target
     * spectra and display information of the spectra
     */
    private void readSpectra() {

        try {

            LOG.info("Reading Data ....");
            mainView.btnStartSearch.setEnabled(false);
            //Read target and librart spectra  

            // List<Spectrum> experimentalSpectra;
            Indexer giExp = new Indexer(configData.getExperimentalSpecFile());
            List<IndexKey> indxList = giExp.generate();
            Collections.sort(indxList);
            configData.setExpSpectraIndex(indxList);

            //Read spectra library
            String libname = configData.getSpecLibraryFile().getName().substring(0, configData.getSpecLibraryFile().getName().lastIndexOf("."));
            File lib_indxfile = new File(configData.getSpecLibraryFile().getParent(), libname + ".idx");

            List<IndexKey> indxList2;
            if (lib_indxfile.exists()) {

                Indexer indxer = new Indexer();
                indxList2 = indxer.readFromFile(lib_indxfile);

            } else {

                Indexer gi = new Indexer(configData.getSpecLibraryFile());
                indxList2 = gi.generate();
                Collections.sort(indxList2);
                gi.saveIndex2File(lib_indxfile);

            }
            configData.setSpectralLibraryIndex(indxList2);

            //reader for experimental spectrum file
            if (configData.getExperimentalSpecFile().getName().endsWith("mgf")) {
                SpectrumReader rd = new MgfReader(configData.getExperimentalSpecFile(), configData.getExpSpectraIndex());
                configData.setExpSpecReader(rd);

            } else if (configData.getExperimentalSpecFile().getName().endsWith("msp")) {
                SpectrumReader rd = new MspReader(configData.getExperimentalSpecFile(), configData.getExpSpectraIndex());
                configData.setExpSpecReader(rd);

            }

            //reader for spectral library file
            if (configData.getSpecLibraryFile().getName().endsWith("mgf")) {
                SpectrumReader rd = new MgfReader(configData.getSpecLibraryFile(), configData.getSpectraLibraryIndex());
                configData.setLibSpecReader(rd);

            } else if (configData.getSpecLibraryFile().getName().endsWith("msp")) {
                SpectrumReader rd = new MspReader(configData.getSpecLibraryFile(), configData.getSpectraLibraryIndex());
                configData.setLibSpecReader(rd);
            }

            //Displaying info and visualization of experimental spectrum
            int expSpecSize = configData.getExpSpectraIndex().size();
            spnModel.setMaximum(expSpecSize);
            targetView.txtTotalSpec.setText("/" + Integer.toString(expSpecSize));
            updateInputInfo();
            spectrumDisplay(0);
            rasterDisplay();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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
    public List<String> validateInput() {
        //settingsPnl.txtLibrary.setText("C:/tempData/SpecB.msp");
        List<String> validationMessages = new ArrayList<>();

        String tempS = settingsPnl.txttargetspec.getText();
        if ("".equals(tempS)) {
            validationMessages.add("Please provide a spectra input directory.");
        } else if (!tempS.endsWith(".mgf") && !tempS.endsWith(".msp")) {
            validationMessages.add(" Targer Spectra file typenot valid");
        }

        String tempS2 = settingsPnl.txtLibrary.getText();
        if ("".equals(tempS2)) {
            validationMessages.add("Please select library file");
        } else if (!tempS2.endsWith(".mgf") && !tempS2.endsWith(".msp")) {
            validationMessages.add(" Data Base Spectra file typenot valid");
        }

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

//    /**
//     * Get index of the best matched spectrum from result string
//     *
//     * @param index
//     */
//    private void getIndex(int index) {
//        //index of best matched spectra for a given target spectrum
//        resultNumber = index;
//
//    }
    /**
     * Displays the comparison result visually on the result panel
     */
    private void displayResult() {

        try {
            if (!res.get(targSpectrumNum).isEmpty()) {
                Spectrum targSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(targSpectrumNum).getPos());

                ArrayList<ComparisonResult> singleResult = res.get(targSpectrumNum);

                if (singleResult != null) {
                    ComparisonResult targMatchedResult = singleResult.get(resultNumber);
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
            LOG.error(exception);
        }

        resultPnl.pnlVisualSpectrum.revalidate();
        resultPnl.pnlVisualSpectrum.repaint();
    }

    /**
     * Fill table with target spectra
     */
    private void fillTargetTable() {

        tblModelTarget.setRowCount(0);
        int targetsize = configData.getExpSpectraIndex().size();

        Spectrum tSpec;
        Object[][] rows = new Object[targetsize][6];
        for (int p = 0; p < targetsize; p++) {
            // name = d.getSpectra1().get(p).getSpectrumTitle();

            tSpec = configData.getExpSpecReader().readAt(configData.getExpSpectraIndex().get(p).getPos());
            rows[p][0] = p + 1;
            rows[p][1] = "ID" + Integer.toString(p + 1);
            rows[p][2] = tSpec.getTitle();
            rows[p][3] = tSpec.getPCMass();
            rows[p][4] = tSpec.getCharge();
            rows[p][5] = tSpec.getNumPeaks();

            tblModelTarget.addRow(rows[p]);
        }

    }

    /**
     * fill table with best matched spectra
     *
     * @param target target spectrum index
     */
    public void fillBestmatchTable(int target) {

        ArrayList<ComparisonResult> singleResult = res.get(target);
        targSpectrumNum = target;

        if (!singleResult.isEmpty()) {
            tblModelResult.setRowCount(0);

            int i = 0;
            Object[][] rows = new Object[10][8];

            for (ComparisonResult r : singleResult) {

                rows[i][0] = i + 1;
                rows[i][1] = "ID" + Integer.toString(i);
                rows[i][2] = r.getTitle();
                rows[i][3] = r.getPrecMass();
                rows[i][4] = r.getCharge();
                rows[i][5] = r.getScore();

                double conf = (r.getScore() / 400) * 100;
                rows[i][6] = Math.round(conf);
                tblModelResult.addRow(rows[i]);
                i++;

            }

        } else {
            tblModelResult.setRowCount(0);
        }
        updateresultview(0);

    }

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
            mainView.prgProgress.setValue(v);
            mainView.prgProgress.setString(Integer.toString(v) + "%");
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
        resultNumber = index;
        displayResult();
    }

    /**
     * Save graphically provided settings to the property file
     */
    public void saveSettings() {

        List<String> validationMessages = validateInput();
        if (!validationMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (String validationMessage : validationMessages) {
                message.append(validationMessage).append(System.lineSeparator());
            }
            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
        } else {

            ReadInputData();
            ConfigHolder.getInstance().setProperty("matching.algorithm", configData.getScoringFunction());
            ConfigHolder.getInstance().setProperty("fragment.tolerance", configData.getfragTol());
            ConfigHolder.getInstance().setProperty("precursor.tolerance", configData.getPrecTol());
            ConfigHolder.getInstance().setProperty("max.charge", configData.getMaxPrecursorCharg());
            ConfigHolder.getInstance().setProperty("db.spectra.path", configData.getSpecLibraryFile());
            ConfigHolder.getInstance().setProperty("target.spectra", configData.getExperimentalSpecFile());
        }

    }

    /**
     * saves the result to user selected file
     */
    public void saveResult() throws InterruptedException {

        if (res != null) {

            JFileChooser fileChooser = new JFileChooser("D:/");
            fileChooser.setDialogTitle("Specify a file to save");

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File filename = fileChooser.getSelectedFile();
                BufferedWriter writer = null;
                try {

                    writer = new BufferedWriter(new FileWriter(filename));
                    for (ArrayList<ComparisonResult> singleTargetResult : res) {
                        for (ComparisonResult r : singleTargetResult) {
                            double confidence = (r.getScore() / 400.0) * 100.0;//value of the score
                            confidence = Math.round(confidence);
                            String op = r.getTitle() + "," + r.getCharge() + "," + r.getScanNum() + "," + Double.toString(r.getPrecMass()) + "," + Double.toString(r.getScore()) + "," + Double.toString(confidence);
                            writer.write(op);
                            writer.write("\n");

                        }
                        writer.write("\n\n");
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

        JFileChooser fileChooser = new JFileChooser("D:/AllDocs/MS-data/");
        fileChooser.setDialogTitle("Target Spectra File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String tempfile = fileChooser.getSelectedFile().getPath();
            tempfile = tempfile.replace('\\', '/');
            if (tempfile.endsWith(".mgf") || tempfile.endsWith(".msp")) {
                settingsPnl.txttargetspec.setText(tempfile);

            } else {
                showMessageDialog("Invalid Data", "Invalid File Format", JOptionPane.WARNING_MESSAGE);
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

            matching.InpArgs(Integer.toString(configData.getMsRobinOption()), Integer.toString(configData.getIntensityOption()), Double.toString(configData.getfragTol()));
            res = new ArrayList<>();
            res = matching.compare(configData.getExpSpecReader(), configData.getExpSpectraIndex(), configData.getLibSpecReader(), configData.getSpectraLibraryIndex(), LOG);
            return null;
        }

        @Override
        protected void done() {
            try {

                if (cencelled) {

                    LOG.info("Process Cancelled.");
                    mainView.prgProgress.setValue(0);
                    mainView.prgProgress.setString(Integer.toString(0) + "%");

                } else {
                    LOG.info("Spectrum Similarity Comparison Completed");
                    mainView.prgProgress.setValue(100);
                    mainView.prgProgress.setString(Integer.toString(100) + "%");

                    if (res != null && res.size() > 0) {

                        fillTargetTable();
                        fillBestmatchTable(0);
                        displayResult();

                    } else {
//                        Log.info("No comparison result.");
                    }

                }

                isBussy = false;
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

}
