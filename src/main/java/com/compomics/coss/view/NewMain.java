/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Genet
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        TestFrame frm = new TestFrame();
        RectDraw drw=new RectDraw();
        frm.add(drw);
        frm.setVisible(true);
    }

    private static class RectDraw extends JPanel {

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawRect(230, 80, 10, 10);
            g.setColor(Color.RED);
            g.fillRect(230, 80, 10, 10);
        }

//        public Dimension getPreferredSize() {
//            return new Dimension(PREF_W, PREF_H); // appropriate constants
//        }
    }

}
