/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.LayoutManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Genet
 */
public class layoutView extends JFrame{

   public layoutView(){
       
   }
   
   
   
   public void initcomo(){
       setDefaultCloseOperation(WIDTH);
       setName("Test Layout");
       setSize(300, 300);
       setLayout(new BorderLayout());
       
      JPanel panel = new JPanel();
      panel.setBackground(Color.DARK_GRAY);
      panel.setSize(300,300);
      BorderLayout layout = new BorderLayout();
      layout.setHgap(10);
      layout.setVgap(10);
      
      panel.setLayout(layout);        
      panel.add(new JButton("Center"),BorderLayout.CENTER);
      panel.add(new JButton("Line Start"),BorderLayout.LINE_START); 
      panel.add(new JButton("Line End"),BorderLayout.LINE_END);
      panel.add(new JButton("East"),BorderLayout.EAST);   
      panel.add(new JButton("West"),BorderLayout.WEST); 
      panel.add(new JButton("North"),BorderLayout.NORTH); 
      panel.add(new JButton("South"),BorderLayout.SOUTH); 
      
      
      this.add(panel);
      this.pack();
       
   }
    
    
    
    
}
