package com.compomics.coss.View;

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
import com.compomics.coss.Controller.MainFrameController;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
        scoringFun[0] = "MsRobin";
        scoringFun[1] = "Euclidian";
        scoringFun[2] = "Wavlete";

        Object[] transform = new Object[2];
        scoringFun[0] = "Log";
        scoringFun[1] = "Linear";

        //Buttons
        JButton btntargetspec = new JButton("...");
        JButton btnApplyPreprocess = new JButton("Apply preprocess");
        JButton btnSave = new JButton("Save My Settings");
        JButton btnClear = new JButton("Clear Inputs");
        JButton btnLoad = new JButton("Loadd from Fiile");

        //Labels
        JLabel lbldblib = new JLabel("Spectral Library");
        JLabel lbltargetspec = new JLabel("Target Spectra");
        JLabel lblAlgorithmType = new JLabel("Scoring Function");
        JLabel lblCutOff = new JLabel("CutOff");
        JLabel lblFragmentTolerance = new JLabel("Fragment Tolerance");
        JLabel lblPrecursorCharge = new JLabel("Precursor Charge");
        JLabel lblPrecursorTolerance = new JLabel("Precursor Tolerance");

        //Text field
        //txtdbspec = new JTextField();
        txttargetspec = new JTextField();
        txtCutOff = new JTextField();
        txtFragmentTolerance = new JTextField();
        txtPrecursorCharge = new JTextField();
        txtPrecursorTolerance = new JTextField();

        //Check Boxes
        chkFilter = new JCheckBox("Filter");
        chkRemovePrecursor = new JCheckBox("Remove Precursor");
        chkTransform = new JCheckBox("Transformation Type");
        //Combo Boxes

        //cboSpectraLibrary = new JComboBox();
        txtLibrary = new JTextField("   ");
        txtLibrary.setEnabled(false);
        cboScoringFun = new JComboBox(scoringFun);
        cboTransformType = new JComboBox(transform);

        //Panels
        JPanel pnlInputs = new JPanel();
        TreeModel fileModel = new FileTreeModel(this.libDirectory);
        //getContentPane().setLayout(new GridLayout(0, 3, 0, 0));

       JScrollPane scrollPane = new JScrollPane();
        JTree srcDirTree = new JTree(fileModel);
        srcDirTree.setEditable(false);
        srcDirTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        srcDirTree.setShowsRootHandles(true);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Choose Spectral Library Path"));
        
        
        scrollPane.setViewportView(srcDirTree);

        srcDirTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {

                Object object = e.getPath().getLastPathComponent();
                if (object instanceof File) {
                    File file = (File) object;
                    String path=file.getPath().replace('\\', '/');
                    txtLibrary.setText(path);
                }
               
            }
        });


        //Input settings panel layout
        JPanel pnlInputParam = new JPanel();
        pnlInputParam.setBorder(BorderFactory.createTitledBorder("Input Settings"));

        GroupLayout pnlInput1Layout = new javax.swing.GroupLayout(pnlInputParam);
        pnlInputParam.setLayout(pnlInput1Layout);
        pnlInput1Layout.setHorizontalGroup(
                pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlInput1Layout.createSequentialGroup()
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblPrecursorTolerance)
                                .addComponent(lblFragmentTolerance)
                                .addComponent(lblPrecursorCharge)
                                .addComponent(lbltargetspec)
                                .addComponent(lbldblib))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPrecursorCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtFragmentTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtPrecursorTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txttargetspec)
                                .addComponent(txtLibrary))
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlInput1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(btntargetspec))
                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGap(39, 39, 39))
        );
        pnlInput1Layout.setVerticalGroup(
                pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlInput1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lbltargetspec)
                                .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txttargetspec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btntargetspec)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtLibrary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lbldblib)))
                        .addGap(24, 24, 24)
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblPrecursorTolerance)
                                .addComponent(txtPrecursorTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtFragmentTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblFragmentTolerance))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlInput1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPrecursorCharge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblPrecursorCharge)))
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
                                .addComponent(chkFilter)
                                .addComponent(chkRemovePrecursor)
                                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                                        .addComponent(chkTransform)
                                        .addGap(18, 18, 18)
                                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(btnApplyPreprocess)
                                                .addComponent(cboTransformType, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlPreprocessingLayout.setVerticalGroup(
                pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlPreprocessingLayout.createSequentialGroup()
                        .addComponent(chkFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblCutOff)
                                .addComponent(txtCutOff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkRemovePrecursor)
                        .addGap(18, 18, 18)
                        .addGroup(pnlPreprocessingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(chkTransform)
                                .addComponent(cboTransformType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnApplyPreprocess)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(cboScoringFun, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(134, Short.MAX_VALUE))
        );
        pnlMatchingLayout.setVerticalGroup(
                pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMatchingLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAlgorithmType)
                                .addComponent(cboScoringFun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        //End of Matching panel layout
        //this panel: the layout
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


        btntargetspec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                control.chooseTargetFile();

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
        cboScoringFun.setSelectedIndex(0);
        txtPrecursorCharge.setText("");
        txtPrecursorTolerance.setText("");
        txtFragmentTolerance.setText("");

    }

    //Component Declaration
    //Text field
    public JTextField txttargetspec;
    public JTextField txtLibrary;
    public JTextField txtCutOff;
    public JTextField txtFragmentTolerance;
    public JTextField txtPrecursorCharge;
    public JTextField txtPrecursorTolerance;

    //Check Boxes
    public JCheckBox chkFilter;
    public JCheckBox chkRemovePrecursor;
    public JCheckBox chkTransform;

    //Combo Boxs   
    //public JComboBox cboSpectraLibrary;
    public JComboBox cboScoringFun;
    public JComboBox cboTransformType;

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
