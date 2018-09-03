/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.View;

import com.compomics.coss.Controller.MainConsolControler;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import com.compomics.coss.Controller.MainFrameController;

/**
 *
 * @author Genet
 */
public class SearchCommandPnl extends JPanel{

    MainFrameController control;
    public SearchCommandPnl(MainFrameController control) {
        this.control=control;
        init();
    }
    
    private void init(){
        
        btnConfigReader=new JButton("Config Spec. Reader");
        btnStartSearch=new JButton("Start Searching");
        JButton btnCancel=new  JButton("Cancel");       
        prgProgress=new JProgressBar(0, 100);  
        
        JPanel innerControlPanel=new JPanel(new FlowLayout());
        innerControlPanel.add(btnConfigReader);
        innerControlPanel.add(btnStartSearch);
        innerControlPanel.add(btnCancel);
        
             //start search button listenr
        btnConfigReader.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                       
                        
                        control.configReader();

                    }
                }
                );
        
         //start search button listenr
        btnStartSearch.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                       
                        
                        control.startSearch();

                    }
                }
                );
        
        
        //cancel button listener
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {

                control.stopSearch();

            }
        }
        );
        
        
        setLayout(new BorderLayout());
        add(innerControlPanel, BorderLayout.NORTH);
        add(prgProgress, BorderLayout.SOUTH);
        
        
    }
    
    public JButton btnStartSearch;    
    public JButton btnConfigReader; 
    public JProgressBar prgProgress;
    
    
}
