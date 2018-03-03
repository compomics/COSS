/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.View;

import com.compomics.coss.Controller.MainFrameController;
import com.compomics.coss.Model.ComparisonResult;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;

/**
 *
 * @author Genet
 */
public class ValidationHistogramPanel extends JPanel {

    private final TreeMap<Double, Integer> histTarget;
    private final TreeMap<Double, Integer> histDecoy;
    BufferedImage tempImage;
    Image image;
    public ValidationHistogramPanel(List<ArrayList<ComparisonResult>> resultTarget, List<ArrayList<ComparisonResult>> resultDecoy) {
        this.histTarget = new TreeMap<>();
        this.histDecoy = new TreeMap<>();
        if(resultTarget!=null || resultDecoy!=null){        
            
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
        }
        init();
    }

    private void init() {

    }

    /**
     * paint on this panel
     * @param g graphics object
     */
    @Override
    public void paint(Graphics g) {

        super.paint(g);
        
        if(!this.histDecoy.isEmpty()|| !this.histTarget.isEmpty()){
            Graphics2D g2 = (Graphics2D) g;

            drawDistribution();
            drawCoordinate(g2);
            image=(Image)tempImage;

            if (image != null) {
                g.drawImage(image, 51, 10, null);
            }

            if (image == null ) {
                g.drawString("Couldn't get scores", this.getWidth() / 2 - 10, this.getHeight() / 2 - 10);
            }


            g2.dispose();
        }
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
    private void drawDistribution() {

        double maxScoreT = histTarget.lastEntry().getKey();
        double maxScoreD = histDecoy.lastEntry().getKey();
        int maxHitT=histTarget.lastEntry().getValue();
        int maxHitD=histDecoy.lastEntry().getValue();
        

        double maxScore = maxScoreT > maxScoreD ? maxScoreT : maxScoreD;
        int maxHit= maxHitT>maxHitD? maxHitT : maxHitD;
        
        //int len = this.histDecoy.size();   

        int w =(int)Math.ceil(maxScore)+5;
        int h =maxHit+5;
        tempImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = tempImage.createGraphics();
        g.setPaint(new Color(250, 250, 250));
        g.fillRect(0, 0, w, h);
        //g.setColor(Color.RED);

       // double x_step=maxScore/(double)w;
        //double y_step= maxHit/(double)h;
        int xPos;
        int yPos;

        //drawing target db scores with green color
       g.setColor(Color.green);
        for (Map.Entry<Double,Integer> entry : histTarget.entrySet()) {
            xPos = entry.getKey().intValue();
            yPos = h - entry.getValue();        
            
            g.drawLine(xPos, h, xPos, yPos);
        }

        //draing decoy scores with red color
        g.setColor(Color.red);
         for (Map.Entry<Double,Integer> entry : histDecoy.entrySet()) {
            xPos = entry.getKey().intValue();
            yPos = h - entry.getValue();   
            
            g.drawLine(xPos, h, xPos, yPos);
        }
        g.dispose();

    }

    /**
     * Draws the x and y coordinates for spectrum display
     *
     * @param g2 graphical object of the panel
     */
    private void drawCoordinate(Graphics2D g2) {

//        int y;
//        int x;
//        int xStep;
//        int yStep;
//
//        double mzPoint = 0;
//        int currentX = 0;

        //int picWidth = this.getWidth() - 50; // 50 pixels for left(50) and right(10) margin
        int picHeight = this.getHeight() - 50;// 50 pixels for top(10) and bottom(50) margin

        int picLeft = 40;// left margin, 40 pixels
        int picRight = this.getWidth() - 10;// right margin,10 pixels
        int picTop = 10; // top margin, 10 pixels
        int picBottom = this.getHeight() - 40; //lower coordinate position of the picture
        int picMid = picHeight / 2 + picTop;//Mid coordinate position of the picture

//        xStep = picWidth / 10; //divide the pcture in 10 steps horizontally
//        yStep = picHeight / 5; //divide the picture in 5 steps vertically

        //DrawCoordinate(this.getHeight()-10, this.getWidth()-10, 20, 20, new Point(10, 10), grph);
        g2.setColor(Color.black);
        //draw vertical coordinate line
        g2.drawLine(picLeft - 1, picTop, picLeft - 1, picBottom + 1);

        g2.drawString("Hits", 0, picMid);
        //g2.drawString("(%)", 0, picMid + 10);

        //draw horizontal coordinate line, measurements and lable 
        g2.drawLine(picLeft - 1, picBottom + 1, picRight, picBottom + 1);
//        for (x = picLeft - 1; x <= picRight; x += xStep) {
//
//            g2.drawLine(x, picBottom, x, picBottom + 5);// horizontal ticks
//            mzPoint = currentX * xMap;
//            mzPoint = Precision.round(mzPoint, 2);
//            g2.drawString(Double.toString(mzPoint), x - 10, picBottom + 17); //10(string size) + 2(gap with ticks) + 5(bottompic pos) = 17 pixels
//            currentX += xStep;
//        }
        g2.drawString("Score", (this.getWidth() / 2) - 5, this.getHeight() - 17);

//        //vertical ticks of the coordinate
//        int currentY = 0;
//        //int intensityPoint = 0;
//        // double percentage=100/(double)picHeight;
//
//        for (y = picBottom + 1; y >= picTop; y -= yStep) {
//
//            g2.drawLine(picLeft - 1, y, picLeft - 6, y);
//            //intensityPoint =(int)( currentY * percentage);                
//            g2.drawString(Integer.toString(currentY), 20, y + 3);
//            currentY += 20;
//
//        }

    }

}
