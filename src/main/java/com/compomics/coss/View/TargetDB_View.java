
package com.compomics.coss.View;

import com.compomics.coss.Controller.MainFrameController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Genet
 */
public class TargetDB_View extends JPanel {
    

    
    public JTextField txtRtTime;
    public JTextField txtScanno;
    public JTextField txtmaxmz;
    public JTextField txtminmz;
    public JTextField txtnumpeaks;
    
    public  JSpinner spnSpectrum;
    public JTextField txtTotalSpec;
    public JPanel pnlVizSpectrum;
    public JPanel pnlRaster;
    
    MainFrameController contrl;
    
    public TargetDB_View(MainFrameController control){
        this.contrl=control;
        initComponent();
    }

    private void initComponent() {
        
        
        spnSpectrum = new JSpinner();
        
        JLabel lblnumpeaks = new JLabel();
        JLabel lblRtTime = new JLabel();
        JLabel lblScan = new JLabel();
        JLabel lblMaxmz = new JLabel();
        JLabel lblminmz = new JLabel();
        txtnumpeaks = new JTextField();
        txtRtTime = new JTextField();
        txtScanno = new JTextField();
        txtmaxmz = new JTextField();
        txtminmz = new JTextField();
        txtTotalSpec=new JTextField("/0");
        
        txtTotalSpec.setEditable(false);
        lblnumpeaks.setText("No. Peaks");

        lblRtTime.setText("Ret. Time");

        lblScan.setText("Scan No.");

        lblMaxmz.setText("Max. Mz");

        lblminmz.setText("Min. Mz");

        JPanel pnlRasterBase=new JPanel();// panel holding raster and color bar
        
        
        txtnumpeaks.setEditable(false);
        txtnumpeaks.setText("0");

        txtRtTime.setEditable(false);
        txtRtTime.setText("0");

        txtScanno.setEditable(false);
        txtScanno.setText("0");

        txtmaxmz.setEditable(false);
        txtmaxmz.setText("0");

        txtminmz.setEditable(false);
        txtminmz.setText("0");
        
        pnlRaster = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlRaster.setLayout(new BorderLayout());
        
        pnlVizSpectrum = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlVizSpectrum.setLayout(new BorderLayout());
        
        JPanel pnlSpecInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //pnlSpecInfo.setBorder(BorderFactory.createTitledBorder("Target Spectra Info"));
        
                
        javax.swing.GroupLayout pnlInfoLayout = new javax.swing.GroupLayout(pnlSpecInfo);
        pnlSpecInfo.setLayout(pnlInfoLayout);
        pnlInfoLayout.setHorizontalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInfoLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInfoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlInfoLayout.createSequentialGroup()
                        .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblnumpeaks)
                            .addComponent(lblRtTime)
                            .addComponent(lblScan)
                            .addComponent(lblMaxmz)
                            .addComponent(lblminmz))
                        .addGap(18, 18, 18)
                        .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtnumpeaks)
                            .addComponent(txtRtTime)
                            .addComponent(txtScanno)
                            .addComponent(txtmaxmz)
                            .addComponent(txtminmz, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))))
                    .addGroup(pnlInfoLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(spnSpectrum, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalSpec, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlInfoLayout.setVerticalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInfoLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblnumpeaks)
                    .addComponent(txtnumpeaks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblScan)
                    .addComponent(txtScanno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRtTime)
                    .addComponent(txtRtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMaxmz)
                    .addComponent(txtmaxmz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblminmz)
                    .addComponent(txtminmz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                //.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalSpec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnSpectrum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, 30))
        );
        
        ColorGradient clrGradLabel=new ColorGradient();
        pnlRasterBase.setLayout(new BorderLayout());
        pnlRasterBase.add(pnlRaster, BorderLayout.CENTER);
        pnlRasterBase.add(clrGradLabel, BorderLayout.EAST);
        

        
        spnSpectrum.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                
                contrl.updateInputInfo();
                
            }
        });
        
         setBorder(BorderFactory.createTitledBorder("Target Spectra Information"));
         setLayout(new GridLayout(3, 1));
         add(pnlSpecInfo);
         add(pnlVizSpectrum);
         add(pnlRasterBase);
    }
    
}
