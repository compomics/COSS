/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.view;

import com.compomics.coss.model.ComparisonResult;
import java.awt.BasicStroke;
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
        if(resultTarget!=null && resultDecoy!=null){      
            
            for(ArrayList<ComparisonResult> resT: resultTarget){
                if(!resT.isEmpty())
                {
                    double score=resT.get(0).getTopScore();
                
                    if(histTarget.containsKey(score)){
                        int val=histTarget.get(score)+1;
                        histTarget.replace(score, val);
                    }
                    histTarget.put(score, 1);
                }
              
                    
                
            }
            
             for(ArrayList<ComparisonResult> resD: resultDecoy){
                 
                 if(!resD.isEmpty())
                 {
                    double score=resD.get(0).getTopScore();                
                    if(histDecoy.containsKey(score)){
                        int val=histDecoy.get(score)+1;
                        histDecoy.replace(score, val);
                    }
                    histDecoy.put(score, 1); 
                 }
                 
                
            }
            
//            resultDecoy.stream().map((rt) -> rt.get(0).getScore()).map((score) -> {
//                histDecoy.computeIfPresent(score, (k, v) -> v + 1);
//                return score;
//            }).forEach((score) -> {
//                histDecoy.putIfAbsent(score, 1);
//            });
//
//            resultTarget.stream().map((rt) -> rt.get(0).getScore()).map((score) -> {
//                histTarget.computeIfPresent(score, (k, v) -> v + 1);
//                return score;
//            }).forEach((score) -> {
//                histTarget.putIfAbsent(score, 1);
//            });
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
        Graphics2D g2 = (Graphics2D) g;
        drawCoordinate(g2);
        if(!this.histDecoy.isEmpty()|| !this.histTarget.isEmpty()){
            drawDistribution();
            
            image=(Image)tempImage;
            image = image.getScaledInstance(this.getWidth()-35, this.getHeight()-15, Image.SCALE_DEFAULT);

            if (image != null) {
                g.drawImage(image, 24, 0, null);
            }

            

            g2.dispose();
        }
        if (image == null ) {
                g.drawString("Result not provided", this.getWidth() / 2 - 10, this.getHeight() / 2 - 10);
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
        double maxScoreD=0;
        if(!histDecoy.isEmpty()){
             maxScoreD = histDecoy.lastEntry().getKey();
        }
       
       // int maxHitT=histTarget.lastEntry().getValue();
        //int maxHitD=histDecoy.lastEntry().getValue();
        

        double maxScore = maxScoreT > maxScoreD ? maxScoreT : maxScoreD;
        //int maxHit= maxHitT>maxHitD? maxHitT : maxHitD;
        
        //int len = this.histDecoy.size();   

        int w =(int)Math.ceil(maxScore)+5;
        int h = this.getHeight()-15;// 50;//maxHit+5;
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
        
        if(!histTarget.isEmpty()){
            g.setColor(Color.blue);
            g.setStroke(new BasicStroke(3));
            for (Map.Entry<Double,Integer> entry : histTarget.entrySet()) {
                xPos = entry.getKey().intValue();
                yPos = h - entry.getValue()*50;   


                g.drawLine(xPos, h, xPos, yPos);
            }

        }
     
        //draing decoy scores with red color
        if(!histDecoy.isEmpty()){
            g.setColor(Color.red);
             g.setStroke(new BasicStroke());
             for (Map.Entry<Double,Integer> entry : histDecoy.entrySet()) {
                xPos = entry.getKey().intValue();
                yPos = h - entry.getValue()*4;   

                g.drawLine(xPos, h, xPos, yPos);
            }

        }
        
       
    
        g.dispose();

    }

    /**
     * Draws the x and y coordinates for spectrum display
     *
     * @param g2 graphical object of the panel
     */
    private void drawCoordinate(Graphics2D g2) {
        
//int picWidth = this.getWidth() - 50; // 50 pixels for left(50) and right(10) margin
        int picHeight = this.getHeight() - 15;// 50 pixels for top(10) and bottom(50) margin

        int picLeft = 23;
        int picRight = this.getWidth() - 10;
        int picTop = 0; 
        int picBottom = this.getHeight() - 15;
        int picMid = picHeight / 2 + picTop;

        //DrawCoordinate(this.getHeight()-10, this.getWidth()-10, 20, 20, new Point(10, 10), grph);
        g2.setColor(Color.black);
        //draw vertical coordinate line
        g2.drawLine(picLeft - 1, picTop, picLeft - 1, picBottom + 1);

        g2.drawString("Hits", 0, picMid);
        //draw horizontal coordinate line, measurements and lable 
        g2.drawLine(picLeft - 1, picBottom + 1, picRight, picBottom + 1);

        g2.drawString("Score", (this.getWidth() / 2) - 5, this.getHeight() - 3);

    }

}
