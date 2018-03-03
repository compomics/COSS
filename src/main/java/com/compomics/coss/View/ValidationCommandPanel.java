/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.View;

import com.compomics.coss.Controller.MainFrameController;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Genet
 */
public class ValidationCommandPanel extends JPanel {
     
    MainFrameController control;
    public ValidationCommandPanel(MainFrameController control){
        
        this.control=control;
        init();
        
    }
    
    private void init(){
        JLabel lblFdr=new JLabel("FDR");
        Object[] fdr = new Object[5];
        fdr[0] = "0.1%";
        fdr[1] = "0.5%";
        fdr[2] = "0.2%";
        fdr[3] = "0.3%";
        fdr[2] = "0.4%";
        
        JComboBox cmbFdr=new JComboBox(fdr);
        cmbFdr.setSelectedIndex(0);
        JLabel lblVaidResul=new JLabel("No. of Valid Result");
        JTextField txtValidResult=new JTextField(Integer.toString(0));
        txtValidResult.setEditable(false);
        JButton btnValidate=new JButton("Validate Result");        
       // JPanel pnlValdninnerControl = new JPanel(new FlowLayout());
        
        javax.swing.GroupLayout pnlValdninnerControlLayout = new javax.swing.GroupLayout(this);
        setLayout(pnlValdninnerControlLayout);
        pnlValdninnerControlLayout.setHorizontalGroup(
            pnlValdninnerControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValdninnerControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlValdninnerControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlValdninnerControlLayout.createSequentialGroup()
                        .addComponent(lblFdr)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbFdr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(38, Short.MAX_VALUE))
                    .addGroup(pnlValdninnerControlLayout.createSequentialGroup()
                        .addGroup(pnlValdninnerControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnValidate)
                            .addGroup(pnlValdninnerControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblVaidResul, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtValidResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pnlValdninnerControlLayout.setVerticalGroup(
            pnlValdninnerControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValdninnerControlLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlValdninnerControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFdr)
                    .addComponent(cmbFdr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblVaidResul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValidResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnValidate)
                .addGap(119, 119, 119))
        );
        
        
        
        
    }
    
}
