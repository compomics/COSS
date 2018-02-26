/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.View;

import com.compomics.coss.Model.ComparisonResult;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import org.apache.commons.math3.util.Precision;

/**
 *
 * @author Genet
 */
public class ValidationPanel extends JPanel {

    private  Map<Double, Integer> histTarget;
    private final Map<Double, Integer> histDecoy;
    
    
    public ValidationPanel(List<ArrayList<ComparisonResult>> resultTarget, List<ArrayList<ComparisonResult>> resultDecoy) {
        this.histTarget = new TreeMap<>();
        this.histDecoy = new TreeMap<>();
        
        resultDecoy.stream().map((rt) -> rt.get(0).getScore()).map((score) -> {
            histDecoy.computeIfPresent(score, (k, v) -> v + 1);
            return score;
        }).forEach((score) -> {
            histDecoy.putIfAbsent(score, 1);
        });
        
        
        resultTarget.stream().map((rt) -> rt.get(0).getScore()).map((score) -> {
            histTarget.computeIfPresent(score, (k, v) -> v + 1);
            return score;
        }).forEach((score) -> {
            histTarget.putIfAbsent(score, 1);
        });
        
        
        init();
    }

    private void init() {
            

    }

    /**
     * paint on this panel
     */
    @Override
    public void paint(Graphics g) {

    }
    
    
     /**
     * Draws the spectrum provided on temporary buffered image, the size of the
     * image equals maxMZ by maxIntensity which eliminates the need for floating
     * point calculation for each mz and intensity values of the coordinate to
     * map on the actual panel. Finally the buffered image is then scaled to fit
     * the panel
     *
     * @param spec the spectrum to be drawn
     * @param clr drawing color
     */
    private void drawDistribution(double[] targetScore, double[] decoyscore) {

        
        
        double maxScoreT=histTarget.lastEntry();
        double maxScoreD=histDecoy.lastEntry();
        
        double maxHeight=maxScoreT>maxScoreD?maxScoreT:maxScoreD;
        
        int totalNum=this.histDecoy.size();
       // double intFactor = 100.0 / maxInt;//normalizing intensity to 100(max)
        //int totalPeaks = spec.getPeakList().size();
        //double[] mz = new double[totalPeaks];
        //double[] intensity = new double[totalPeaks];
        for (int p = 0; p < totalNum; p++) {
            double x=
            mz[p]=spec.getPeakListDouble()[0][p];
            intensity[p]=spec.getPeakListDouble()[1][p];
            
        }

        int h = 100;
        int w = maxMZ + 5;
        tempImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = tempImage.createGraphics();
        g.setPaint(new Color(250, 250, 250));
        g.fillRect(0, 0, w, h);
        //g.setColor(Color.RED);

        int xPos;
        int yPos;
        int barHeight;

        //int max = 0;
        for (int i = 0; i < totalPeaks; i++) {
            xPos = (int) mz[i];
            barHeight = (int) (intensity[i] * intFactor);
            if (barHeight <= 0) {
                barHeight += 3;
            }
            yPos = h - barHeight;

            g.setColor(clr);
            g.drawLine(xPos, yPos, xPos, h);
        }

        g.dispose();

    }
    
    

    /**
     * Draws the x and y coordinates for spectrum display
     *
     * @param g2 graphical object of the panel
     */
    private void drawCoordinate(Graphics2D g2) {

        int y;
        int x;
        int xStep;
        int yStep;

        double mzPoint = 0;
        int currentX = 0;

        int picWidth = this.getWidth() - 50; // 60 pixels for left(50) and right(10) margin
        int picHeight = this.getHeight() - 50;// 60 pixels for top(10) and bottom(50) margin

        int picLeft = 40;// left margin, 50 pixels
        int picRight = this.getWidth() - 10;// right margin,10 pixels
        int picTop = 10; // top margin, 10 pixels
        int picBottom = this.getHeight() - 40; //lower coordinate position of the picture
        int picMid = picHeight / 2 + picTop;//Mid coordinate position of the picture

        xStep = picWidth / 10; //divide the pcture in 10 steps horizontally
        yStep = picHeight / 5; //divide the picture in 5 steps vertically

        //DrawCoordinate(this.getHeight()-10, this.getWidth()-10, 20, 20, new Point(10, 10), grph);
        g2.setColor(Color.black);
        //draw vertical coordinate line
        g2.drawLine(picLeft - 1, picTop, picLeft - 1, picBottom + 1);

        g2.drawString("Hits", 0, picMid);
        //g2.drawString("(%)", 0, picMid + 10);

        //draw horizontal coordinate line, measurements and lable 
        g2.drawLine(picLeft - 1, picBottom + 1, picRight, picBottom + 1);
        for (x = picLeft - 1; x <= picRight; x += xStep) {

            g2.drawLine(x, picBottom, x, picBottom + 5);// horizontal ticks
            mzPoint = currentX * xMap;
            mzPoint = Precision.round(mzPoint, 2);
            g2.drawString(Double.toString(mzPoint), x - 10, picBottom + 17); //10(string size) + 2(gap with ticks) + 5(bottompic pos) = 17 pixels
            currentX += xStep;
        }
        g2.drawString("m/z", (this.getWidth() / 2) - 5, this.getHeight() - 17);

        //vertical ticks of the coordinate
        int currentY = 0;
        //int intensityPoint = 0;
        // double percentage=100/(double)picHeight;

        for (y = picBottom + 1; y >= picTop; y -= yStep) {

            g2.drawLine(picLeft - 1, y, picLeft - 6, y);
            //intensityPoint =(int)( currentY * percentage);                
            g2.drawString(Integer.toString(currentY), 20, y + 3);
            currentY += 20;

        }

    }

}
