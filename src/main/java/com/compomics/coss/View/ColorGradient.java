package com.compomics.coss.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JLabel;

/**
 *
 * @author Genet
 */
public class ColorGradient extends JLabel{
    
     public ColorGradient() {
            //setText("Happy");
            setForeground(Color.WHITE);
            setMinimumSize(new Dimension(10, 235));
            setPreferredSize(new Dimension(10, 235));
            setMaximumSize(new Dimension(10, 235));
            setSize(10, 200);
            //setHorizontalAlignment(CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            LinearGradientPaint colorGrad = new LinearGradientPaint(
                    new Point(0, 0), 
                    new Point(0, getHeight()), 
                    new float[]{0.0f, 0.25f, 0.50f, 0.75f, 1.0f}, 
                    new Color[]{Color.RED,Color.YELLOW,Color.GREEN, Color.CYAN, Color.BLUE});
            g2.setPaint(colorGrad);
            g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
            g2.dispose();
            
        }
    
}
