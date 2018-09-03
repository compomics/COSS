package com.compomics.coss.View;

import com.compomics.ms2io.Spectrum;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.apache.commons.math3.util.Precision;

/**
 * Creates panel and paint spectrum on it
 *
 * @author Genet
 */
public class SpecPanel extends JPanel implements MouseListener {

    //initialize SpecPanel with the spectrum whose histogram to be drawn
    Spectrum spectrum, mirrSpectrum;

    boolean withMirror = false;

    Image image;
    Image mirrImage;
    BufferedImage tempImage;

    Point cursorLocation = new Point();
    double xMap = 0;
    double yMap = 0;
    int maxInt = 0;
    int maxMZ = 0;

    /**
     * constructor of this class
     *
     * @param spectrum the spectrum to be displayed
     */
    public SpecPanel(Spectrum spectrum) {
        this.spectrum = spectrum;
        withMirror = false;

        init();
    }

    /**
     * Constructor of this class
     *
     * @param spectrum the spectrum to be displayed
     * @param mrrspectrum the spectrum to be displayed as mirror
     */
    public SpecPanel(Spectrum spectrum, Spectrum mrrspectrum) {
        this.spectrum = spectrum;
        this.mirrSpectrum = mrrspectrum;
        withMirror = true;

        init();

    }

    /**
     * initialize this component, panel SpecPanel
     */
    private void init() {

        // drawImage();
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent m) {
                cursorLocation = m.getPoint();

                repaint();
            }
        });

    }

    /**
     * Drawing the image(spectrum), coordinate and cursor on the panel
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        drawImage();
        drawCoordinate(g2);
        if (image != null) {
            g.drawImage(image, 51, 10, null);
        }

        if (mirrImage != null) {

            g.drawImage(mirrImage, 51, (this.getHeight() - 60) / 2 + 11, null);
        }

        if (cursorLocation.x >= 50 && cursorLocation.x <= this.getWidth() - 10 && cursorLocation.y >= 10 && cursorLocation.y <= this.getHeight() - 50) {

            if (!withMirror) {
                cursorCoordinate(g2);
            } else {
                cursorCoordinateMrr(g2);
            }
        }

        if (image == null && mirrImage == null) {
            g.drawString("Spectrum Display", this.getWidth() / 2 - 10, this.getHeight() / 2 - 10);
        }
        g2.dispose();

    }

    /**
     * Displaying mouse current location when user moves the mouse over the
     * panel
     *
     * @param grph graphic object to the panel
     *
     */
    private void cursorCoordinate(Graphics2D grph) {
        double x, y;
        x = Precision.round((cursorLocation.x - 50) * xMap, 2);
        y = Precision.round((this.getHeight() - 50 - cursorLocation.y) * yMap, 2);

        //maping coordinates to real mz and intensity values
        grph.drawString("[" + Double.toString(x) + ", " + Double.toString(y) + "]", cursorLocation.x, cursorLocation.y);
    }

    /**
     * Displaying mouse current location when user moves the mouse over the
     * panel
     *
     * @param grph graphic object to the panel
     *
     */
    private void cursorCoordinateMrr(Graphics2D grph) {

        int picHeight = this.getHeight() - 60;// 60 pixels for top(10) and bottom(50) margin
        int picTop = 10; // top margin, 10 pixels
        int picMid = picHeight / 2 + picTop;//Mid coordinate position of the picture

        double x, y;
        x = Precision.round((cursorLocation.x - 50) * xMap, 2);

        if (cursorLocation.y <= picMid) {
            y = Precision.round((picMid - cursorLocation.y) * yMap * 2, 2);
        } else {
            y = Precision.round((cursorLocation.y - picMid) * yMap * 2, 2);
        }

        grph.drawString("[" + Double.toString(x) + ", " + Double.toString(y) + "]", cursorLocation.x, cursorLocation.y);
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

        int picWidth = this.getWidth() - 60; // 60 pixels for left(50) and right(10) margin
        int picHeight = this.getHeight() - 60;// 60 pixels for top(10) and bottom(50) margin

        int picLeft = 50;// left margin, 50 pixels
        int picRight = this.getWidth() - 10;// right margin,10 pixels
        int picTop = 10; // top margin, 10 pixels
        int picBottom = this.getHeight() - 50; //lower coordinate position of the picture
        int picMid = picHeight / 2 + picTop;//Mid coordinate position of the picture

        xStep = picWidth / 10; //divide the pcture in 10 steps horizontally
        yStep = picHeight / 5; //divide the picture in 5 steps vertically

        //DrawCoordinate(this.getHeight()-10, this.getWidth()-10, 20, 20, new Point(10, 10), grph);
        g2.setColor(Color.black);
        //draw vertical coordinate line
        g2.drawLine(picLeft - 1, picTop, picLeft - 1, picBottom + 1);

        g2.drawString("Int", 0, picMid);
        g2.drawString("(%)", 0, picMid + 10);

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

        if (!withMirror) {

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
        } else {

            //vertical tichs of the coordinate
            int currentY = 0;
            //int intensityPoint = 0;

            yStep = picHeight / 10;
            //double percentage=(200)/(double)picHeight;
            for (y = picMid; y >= picTop; y -= yStep) {

                g2.drawLine(picLeft - 1, y, picLeft - 6, y);

                //intensityPoint = (int)(currentY * percentage);
                g2.drawString(Integer.toString(currentY), 20, y + 3);
                currentY += 20;
            }

            currentY = 0;
            for (y = picMid; y <= this.getHeight() - 50; y += yStep) {

                g2.drawLine(picLeft - 1, y, picLeft - 6, y);

                //intensityPoint = (int)(currentY * percentage);
                g2.drawString(Integer.toString(currentY), 20, y + 3);
                currentY += 20;
            }

            g2.drawLine(picLeft - 1, picMid, picRight, picMid);
        }

    }

    private void drawImage() {

        int tempMaxInt;
        int tempMaxMZ;
        if (!withMirror) {

            if (spectrum != null) {

                maxInt = (int) spectrum.getMaxIntensity();
                maxMZ = (int) spectrum.getMaxMZ();

                drawSpectrum(spectrum, Color.red);
                image = tempImage.getScaledInstance(this.getWidth() - 60, this.getHeight() - 60, Image.SCALE_DEFAULT);

            }

        } else if (spectrum != null && mirrSpectrum != null) {

            maxInt = (int) spectrum.getMaxIntensity();
            maxMZ = (int) spectrum.getMaxMZ();
            tempMaxInt = (int) mirrSpectrum.getMaxIntensity();
            tempMaxMZ = (int) mirrSpectrum.getMaxMZ();
            maxMZ = (tempMaxMZ > maxMZ) ? tempMaxMZ : maxMZ;
            maxInt = (tempMaxInt > maxInt) ? tempMaxInt : maxInt;
            drawSpectrum(spectrum, Color.red);
            image = tempImage.getScaledInstance(this.getWidth() - 60, (this.getHeight() - 60) / 2, Image.SCALE_DEFAULT);

            drawSpectrum(mirrSpectrum, Color.blue);
            tempImage = flipImage(tempImage);
            mirrImage = tempImage.getScaledInstance(this.getWidth() - 60, (this.getHeight() - 60) / 2, Image.SCALE_DEFAULT);

        }

        xMap = (maxMZ + 5) / (double) (this.getWidth() - 60);
        yMap = (100.0) / (double) (this.getHeight() - 60);
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
    private void drawSpectrum(Spectrum spec, Color clr) {

        double intFactor = 100.0 / maxInt;//normalizing intensity to 100(max)
        int totalPeaks = spec.getPeakList().size();
        double[] mz = new double[totalPeaks];
        double[] intensity = new double[totalPeaks];
        for (int p = 0; p < totalPeaks; p++) {
            mz[p]=spec.getPeakListDouble()[p][0];
            intensity[p]=spec.getPeakListDouble()[p][1];
            
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
     * This function mirror the given image on the horizontal axis
     *
     * @param originalImg image to be flopped
     * @return flipped version of the original image
     */
    private BufferedImage flipImage(BufferedImage originalImg) {
        BufferedImage image = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        op.filter(originalImg, image);
        return image;

    }

    @Override
    public void mouseClicked(MouseEvent me) {

    }

    @Override
    public void mousePressed(MouseEvent me) {

    }

    @Override
    public void mouseReleased(MouseEvent me) {

    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {

    }

}
