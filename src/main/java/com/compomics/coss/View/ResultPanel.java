package com.compomics.coss.View;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.compomics.coss.Controller.MainFrameController;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 *this class creates JPanel on which the comparison result is display
 * @author Genet
 */
public class ResultPanel extends JPanel {

    private JPanel pnlTargetSpectra;
    private JPanel pnlBestMatchs;
    public JPanel pnlVisualSpectrum;
    public JTable tblTargetSpectra;
    public JTable tblBestMatch;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;

    MainFrameController control;

    //Constructor for the current class; when called it initializes all the components
    public ResultPanel(MainFrameController control) {
        this.control = control;
        InitializeComponent();
    }

    //<editor-fold defaultstate="colapsed" desc="Initialize Components">
    /**
     * Initializing components of the panel
     */
    private void InitializeComponent() {

        //tables
        tblBestMatch = new JTable();
        tblTargetSpectra = new JTable();

        tblTargetSpectra.setRowSelectionAllowed(true);
        tblTargetSpectra.setColumnSelectionAllowed(false);

        tblBestMatch.setRowSelectionAllowed(true);
        tblBestMatch.setColumnSelectionAllowed(false);

        jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(tblTargetSpectra);

        jScrollPane2 = new JScrollPane();
        jScrollPane2.setViewportView(tblBestMatch);

        //panels
        pnlBestMatchs = new JPanel();
        pnlBestMatchs.setBorder(javax.swing.BorderFactory.createTitledBorder("Best Match Spectra"));
        
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(pnlBestMatchs);
        pnlBestMatchs.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 830, Short.MAX_VALUE)
                        .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addContainerGap())
        );

        pnlTargetSpectra = new JPanel();
        pnlTargetSpectra.setBorder(javax.swing.BorderFactory.createTitledBorder("Target Spectra"));
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(pnlTargetSpectra);
        pnlTargetSpectra.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                        .addContainerGap())
        );

        pnlVisualSpectrum = new JPanel();
        pnlVisualSpectrum.setBorder(javax.swing.BorderFactory.createTitledBorder("Visual Spectrum Comparison"));
        pnlVisualSpectrum.setLayout(new BorderLayout());
        
     

        
        setLayout(new GridLayout(3, 1));
        add(pnlTargetSpectra);
        add(pnlBestMatchs);
        add(pnlVisualSpectrum);


        //Action Listener
        tblBestMatch.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {

                int index = tblBestMatch.getSelectedRow();

                control.updateresultview(index);

            }
        });

        tblTargetSpectra.getSelectionModel().addListSelectionListener((ListSelectionEvent lse) -> {
            if (!lse.getValueIsAdjusting()) {
                int targSpectrumIndex = tblTargetSpectra.getSelectedRow();
                control.fillBestmatchTable(targSpectrumIndex);
                
                
                
            }
        });

    }
    
    //</editor-fold>

}
