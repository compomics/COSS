package com.compomics.coss.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.compomics.coss.controller.MainFrameController;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import javax.swing.JScrollPane;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * This class creates panel for User settings
 *
 * @author Genet
 */
public class SettingPanel extends JPanel {

    MainFrameController control;
    File libDirectory;

    public SettingPanel(MainFrameController control, File dir) {
        this.control = control;
        this.libDirectory = dir;

        init();

    }

    //<editor-fold defaultstate="colapsed" desc="Initialize component">
    /**
     * This method initializes components
     */
    private void init() {

        //Initializing and setting of component properties
        Object[] scoringFun = new Object[3];
        scoringFun[0] = "MSROBIN";
        scoringFun[1] = "COSINE";
        scoringFun[2] = "MSE";

        Object[] transform = new Object[4];
        transform[0] = "Log2";
        transform[1] = "Log10";
        transform[2] = "Linear";
        transform[3] = "SquareRoot";
        
        Object[] filter = new Object[2];
        filter[0] = "HighPass";
        filter[1] = "   ";

        //Buttons
        JButton btntargetspec = new JButton("...");
        JButton btnLibspec = new JButton("...");
        JButton btnApplyPreprocess = new JButton("Apply preprocessing");
        JButton btnSave = new JButton("Save My Settings");
        JButton btnClear = new JButton("Clear Inputs");
        JButton btnLoad = new JButton("Load from Fiile");
        //btnApplyPreprocess.setEnabled(false);

        //Labels
        JLabel lbldblib = new JLabel("Spectral Library");
        JLabel lbltargetspec = new JLabel("Target Spectra");
        JLabel lblAlgorithmType = new JLabel("Scoring Function");
        JLabel lblCutOff = new JLabel("CutOff");
        JLabel lblMassWindow=new JLabel("Filter WindowSize");
        JLabel lblFragmentTolerance = new JLabel("Fragment Tolerance");
        JLabel lblPrecursorCharge = new JLabel("Precursor Charge");
        JLabel lblPrecursorTolerance = new JLabel("Precursor Tolerance");
        cmbPrcTolUnit = new JComboBox();
        cmbFragTolUnit = new JComboBox();
        cmbPrcTolUnit.addItem("Dalton");
        cmbPrcTolUnit.addItem("PPM");
        cmbFragTolUnit.addItem("Dalton");
        cmbFragTolUnit.addItem("PPM");

        cmbPrcTolUnit.setSelectedIndex(1);
        cmbFragTolUnit.setSelectedIndex(0);
        //Text field
        //txtdbspec = new JTextField();
        txttargetspec = new JTextField();
        txtMassWindow=new JTextField();
        //txtMassWindow.setEnabled(false);
        txtCutOff = new JTextField();
        //txtCutOff.setEnabled(false);
        txtFragmentTolerance = new JTextField();
        txtPrecursorCharge = new JTextField();
        txtPrecursorTolerance = new JTextField();

        //Check Boxes
        chkFilter = new JCheckBox("Filter");
        //chkFilter.setSelected(true);
       // chkFilter.setEnabled(false);
        chkRemovePrecursor = new JCheckBox("Remove Precursor");
        //chkRemovePrecursor.setEnabled(false);
        chkTransform = new JCheckBox("Transformation Type");
        chkTransform.setSelected(false);
        //chkTransform.setEnabled(false);
        //Combo Boxes

        //cboSpectraLibrary = new JComboBox();
        txtLibrary = new JTextField();
        //txtLibrary.setEnabled(false);
        cmbScoringFun = new JComboBox(scoringFun);
        //cmbScoringFun.setEditable(false);

        cmbTransformType = new JComboBox(transform);
        //cmbTransformType.setEnabled(false);
        
        cmbFilterType = new JComboBox(filter);
        cmbFilterType.setEnabled(false);

        //Panels
        JPanel pnlInputs = new JPanel();
        // TreeModel fileModel = new FileTreeModel(this.libDirectory);
        //getContentPane().setLayout(new GridLayout(0, 3, 0, 0));

        JScrollPane scrollPane = new JScrollPane();
        //JTree srcDirTree = new JTree(fileModel);
        //srcDirTree.setEditable(false);
        //srcDirTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //srcDirTree.setShowsRootHandles(true);
        //scrollPane.setBorder(BorderFactory.createTitledBorder("Choose Spectral Library Path"));

        //scrollPane.setViewportView(srcDirTree);
//        srcDirTree.addTreeSelectionListener(new TreeSelectionListener() {
//
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//
//                Object object = e.getPath().getLastPathComponent();
//                if (object instanceof File) {
//                    File file = (File) object;
//                    String path = file.getPath().replace('\\', '/');
//                    txtLibrary.setText(path);
//                }
//
//            }
//        });
        //Input settings panel layout
        JPanel pnlInputParam = new JPanel();
        pnlInputParam.setBorder(BorderFactory.createTitledBorder("Input Settings"));

        GroupLayout jPanel1Layout = new javax.swing.GroupLayout(pnlInputParam);
        pnlInputParam.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblFragmentTolerance)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtFragmentTolerance, 40, 40, 40)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbFragTolUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(lbldblib, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(lbltargetspec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txttargetspec, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                                .addComponent(txtLibrary))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(btntargetspec, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(btnLibspec, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                //.addComponent(btntargetspec, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)

                                )
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblPrecursorTolerance)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtPrecursorTolerance, 40, 40, 40)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbPrcTolUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblPrecursorCharge)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtPrecursorCharge, 40, 40, 40)))
                        .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lbltargetspec)
                                .addComponent(txttargetspec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btntargetspec))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lbldblib)
                                .addComponent(txtLibrary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnLibspec))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblPrecursorTolerance)
                                .addComponent(txtPrecursorTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbPrcTolUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblFragmentTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtFragmentTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbFragTolUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblPrecursorCharge)
                                .addComponent(txtPrecursorCharge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        //End of setting panel 
        //Preprocessing panel 
        JPanel pnlPreprocess = new JPanel();
        pnlPreprocess.setBorder(BorderFactory.createTitledBorder("Preprocessing Options"));
        GroupLayout pnlPreprocessingLayout = new javax.swing.GroupLayout(pnlPreprocess);
        pnlPreprocess.setLayout(pnlPreprocessingLayout);
        pnlPreprocessingLayout.setHorizontalGroup(
                pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(lblCutOff)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtCutOff, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnlPreprocessingLayout.createSequentialGroup()                                       
                                        .addComponent(lblMassWindow)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtMassWindow, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                                        .addComponent(chkFilter)
                                        .addGap(20, 20, 20)
                                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cmbFilterType, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                
                                .addComponent(chkRemovePrecursor)
                                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                                        .addComponent(chkTransform)
                                        .addGap(18, 18, 18)
                                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(btnApplyPreprocess)
                                                .addComponent(cmbTransformType, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlPreprocessingLayout.setVerticalGroup(
                pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(chkFilter)
                                .addComponent(cmbFilterType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                       
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblCutOff)
                                .addComponent(txtCutOff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblMassWindow)
                                .addComponent(txtMassWindow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(chkRemovePrecursor)
                        .addGap(18, 18, 18)
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(chkTransform)
                                .addComponent(cmbTransformType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addComponent(btnApplyPreprocess)
                        .addGap(5, 5, 5))
        );

        //End of preprocessing panel layout
        //Matching panel        
        JPanel pnlMatching = new JPanel();
        pnlMatching.setBorder(BorderFactory.createTitledBorder("Scoring Function"));
        GroupLayout pnlMatchingLayout = new javax.swing.GroupLayout(pnlMatching);
        pnlMatching.setLayout(pnlMatchingLayout);
        pnlMatchingLayout.setHorizontalGroup(
                pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMatchingLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblAlgorithmType)
                        .addGap(18, 18, 18)
                        .addComponent(cmbScoringFun, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(134, Short.MAX_VALUE))
        );
        pnlMatchingLayout.setVerticalGroup(
                pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMatchingLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAlgorithmType)
                                .addComponent(cmbScoringFun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        //End of Matching panel layout
        //base input panel layout: panel to hold all the three different panels together
        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlInputs);
        pnlInputs.setLayout(pnlMainLayout);

        pnlMainLayout.setHorizontalGroup(
                pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlMainLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(pnlInputParam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(pnlPreprocess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(pnlMatching, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(pnlMainLayout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(btnSave)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnLoad)
                                        .addGap(28, 28, 28)
                                        .addComponent(btnClear)))
                //.addContainerGap(240, Short.MAX_VALUE)
                )
        );
        pnlMainLayout.setVerticalGroup(
                pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlInputParam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(pnlPreprocess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(pnlMatching, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSave)
                                .addComponent(btnLoad)
                                .addComponent(btnClear))
                        .addContainerGap(121, Short.MAX_VALUE))
        );

        setLayout(new BorderLayout());
        add(pnlInputs, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.EAST);

        cmbScoringFun.addActionListener((ActionEvent e) -> {
//            JComboBox src = (JComboBox) e.getSource();
//            if (src.getSelectedIndex() != 0) {
//                JOptionPane.showMessageDialog(null,
//                        "Not applicable at the moment", "ERROR",
//                        JOptionPane.ERROR_MESSAGE);
//                cmbScoringFun.setSelectedIndex(0);
//            }

        });

        btntargetspec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                control.chooseTargetFile("target");

            }
        }
        );

        btnLibspec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                control.chooseTargetFile("library");

            }
        }
        );

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                control.saveSettings();

            }
        });

        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                control.LoadData();

            }
        });

        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                clearSettings();

            }
        });

        //End of the main class
    }
    //</editor-fold>

    /**
     * Clear the values of GUI control values
     */
    private void clearSettings() {
        txttargetspec.setText("");
        //cboSpectraLibrary.setSelectedIndex(0);
        cmbScoringFun.setSelectedIndex(0);
        txtPrecursorCharge.setText("");
        txtPrecursorTolerance.setText("");
        txtFragmentTolerance.setText("");

    }

    //Component Declaration
    //Text field
    public JTextField txttargetspec;
    public JTextField txtLibrary;
    public JTextField txtCutOff;
    public JTextField txtMassWindow;
    public JTextField txtFragmentTolerance;
    public JTextField txtPrecursorCharge;
    public JTextField txtPrecursorTolerance;

    //Check Boxes
    public JCheckBox chkFilter;
    public JCheckBox chkRemovePrecursor;
    public JCheckBox chkTransform;

    //Combo Boxs   
    //public JComboBox cboSpectraLibrary;
    public JComboBox cmbScoringFun;
    public JComboBox cmbFilterType;
    public JComboBox cmbTransformType;
    public JComboBox cmbPrcTolUnit;
    public JComboBox cmbFragTolUnit;

    private class FileTreeModel implements TreeModel {

        /**
         * Creating an object of this class and setting its root to the provided
         * File object.
         *
         * The root is the highest directory available in an object of this
         * class.
         *
         * @param file - an object of type File, giving the root directory for
         * an object of type FileTreeModel.
         */
        public FileTreeModel(File file) {
            this.root = file;
        }

        @Override
        public Object getRoot() {
            return this.root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            File f = (File) parent;
            return f.listFiles()[index];
        }

        @Override
        public int getChildCount(Object parent) {
            File f = (File) parent;

            try {
                if (!f.isDirectory() && f.list() != null) {
                    return 0;
                } else {
                    return f.list().length;
                }
            } catch (NullPointerException ex) {
                return 0;
            }
        }

        @Override
        public boolean isLeaf(Object node) {
            File f = (File) node;
            return !f.isDirectory();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            File par = (File) parent;
            File ch = (File) child;
            return Arrays.asList(par.listFiles()).indexOf(ch);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            // TODO Auto-generated method stub
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            // TODO Auto-generated method stub
        }

        private File root;
    }

}
