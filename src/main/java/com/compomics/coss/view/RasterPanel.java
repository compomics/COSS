package com.compomics.coss.view;

import com.compomics.ms2io.IndexKey;
import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.SpectraReader;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.apache.commons.math3.util.Precision;

/**
 * This class draws the raster of color coded intensity of the given spectra
 *
 * @author Genet
 */
public class RasterPanel extends JPanel implements MouseListener, MouseMotionListener {

    List<IndexKey> iKey;
    SpectraReader rd;
    double scale = 1;
    double retnTime = 0;
    BufferedImage temp;
    Image image;
    BufferedImage zoomedImage;
    Point p1 = new Point(0, 0);
    Point p2 = p1;
    boolean onDrawing = false;

    double maxMz;
    int totalNumofSpectra;
    double maxIntensity;

    /**
     * initialize the class
     *
     * @param key index key to read the spectrum
     * @param rd spectrum reader object
     */
    public RasterPanel(List<IndexKey> key, SpectraReader rd) {
        this.iKey = key;
        this.rd=rd;

        addMouseWheelListener(new MouseAdapter() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.05f * e.getPreciseWheelRotation();

                scale += delta;
                if (scale < 1) {
                    scale = 1;
                }
                drawRaster((int) maxMz, totalNumofSpectra, maxIntensity);
                revalidate();
                repaint();
            }

        });

        addMouseListener(this);
        addMouseMotionListener(this);

        if (this.iKey != null) {

            maxMz = 0;
            totalNumofSpectra = 0;
            maxIntensity = 0;
            
            totalNumofSpectra = iKey.size();//this determins the height of the image     
            double temp1, temp2;

            //Read maximum m/z and intensity values of the given spectrum
            Spectrum spec;
            for (IndexKey k : iKey) {
                spec = this.rd.readAt(k.getPos());
                temp1 = spec.getMaxMZ();
                maxMz = (maxMz < temp1) ? temp1 : maxMz;
                temp2 = spec.getMaxIntensity();
                maxIntensity = (maxIntensity < temp2) ? temp2 : maxIntensity;

            }

            drawRaster((int) maxMz, totalNumofSpectra, maxIntensity);

        }

    }

    /**
     * paint function to paint the panel
     *
     * @param g graphics object
     */
    @Override
    public void paint(Graphics g) {

        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = this.getWidth();
        int height = this.getHeight();

        if (image != null) {
            drawCoordinate(g2, width, height, maxMz);
            //scale image to fit the panel
            image = image.getScaledInstance((this.getWidth()) - 65, this.getHeight() - 60, Image.SCALE_DEFAULT);

            g2.drawImage(image, 51, 10, null);

        } else {
            drawCoordinate(g2, width, height, -5);
            g2.drawString("Spectra Color Code Display", width / 2 - 20, height / 2 - 5);

        }

        g2.setColor(Color.RED);
        g2.drawRect(p1.x, p1.y, p2.x, p2.y);
        g2.dispose();

    }

    private void zoomImage(Point rp1, Point rp2) {

        double xmap, ymap;
        xmap = temp.getWidth() / (double) image.getWidth(null);
        ymap = temp.getHeight() / (double) image.getHeight(null);

        int x1, y1, x2, y2;
        x1 = (int) ((p1.x - 51) * xmap);
        x2 = (int) ((p2.x - 51) * xmap);
        y1 = (int) ((p1.y - 10) * ymap);
        y2 = (int) ((p2.y - 10) * ymap);

        image = temp.getSubimage(x1, y1, x2, y2);
        //image = zoomedImage.getScaledInstance((this.getWidth()) - 65, this.getHeight() - 60, Image.SCALE_DEFAULT);

    }

    /**
     * Draws the x and y coordinates for spectrum display
     *
     * @param g2 graphical object of the panel
     * @param width width of the panel
     * @param height height of the panel
     * @param masMZ the maximum m/z value of the spectrum
     */
    private void drawCoordinate(Graphics2D g2, int width, int height, double maxMz) {

        double xCoordinateScale = (maxMz + 5) / (width - 60);
        //double yCoordinateScale=(totalSpectrum+5)/(height-60);

        //DrawCoordinate(this.getHeight()-10, this.getWidth()-10, 20, 20, new Point(10, 10), grph);
        g2.setColor(Color.black);
        //draw vertical coordinate line
        g2.drawLine(50, 10, 50, height - 50);
        //draw horizontal coordinate line
        g2.drawLine(50, height - 50, width - 10, height - 50);
        int y;
        int x;
        int tempH = height - 50;
        double mzPoint = 0;
        int currentX = 0;

        //horizontal ticks of the coordinate
        for (x = 50; x < width - 10; x += 70) {

            g2.drawLine(x, tempH, x, tempH + 5);
            mzPoint = currentX * xCoordinateScale;
            mzPoint = Precision.round(mzPoint / scale, 2);
            g2.drawString(Double.toString(mzPoint), x - 8, tempH + 20);
            currentX += 70;
        }

        //vertical ticks of the coordinate
        int currentY = 0;

        for (y = tempH; y > 10; y -= 50) {

            g2.drawLine(50, y, 45, y);
            g2.drawString(Double.toString((retnTime * currentY) / scale), 15, y + 3);
            currentY += 50;
        }

        //Draw Horizontal and Vertical labels of the coordinate
        g2.drawString("m/z", (width / 2) - 5, height - 17);
        g2.drawString("R.Time", 15, 10);

    }

    /**
     * Draws the color coded intensity of the given spectrum
     *
     * @param imgWidth
     * @param imgHeight
     * @param maxIntensity
     * @return
     */
    private void drawRaster(int imgWidth, int imgHeight, double maxIntensity) {

        int xVal;
        double intensity;

        Spectrum spectrum;
        double normalizedIntensity;

        temp = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Color rgb = getColor(1);
        Graphics2D g = temp.createGraphics();
        g.setPaint(rgb);
        g.fillRect(0, 0, imgWidth, imgHeight);
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        g.setTransform(at);
        int q = 0;
        double[][] specValues;
        for (int i = imgHeight - 1; i >= 0; i--) {//keeps track of the y coordinate: equal to no. of spectra
            
            long p=iKey.get(q).getPos();
            spectrum = rd.readAt(p);
            specValues = spectrum.getPeakListDouble();
            normalizedIntensity = 255.0 / spectrum.getMaxIntensity();//normalization with in specific spectrum intensity
            
            for (int j = 0; j < spectrum.getNumPeaks(); j++) {//for each width equal to max m/z
                xVal = (int) specValues[0][j] - 2;
                
                intensity = specValues[1][j] * normalizedIntensity;
                rgb = getColor((int) intensity);
                
                g.setColor(rgb);
                g.fillRect(xVal, i, 7, 5);
            }
            
            q++;
        }
        g.dispose();

        //resized image to fit to the jpanel display area    
        image = temp;

    }

    /**
     * Encoding intensity value of the spectrum to color
     *
     * @param intensity of the spectrum to be encoded
     * @return
     */
    private Color getColor(int intensity) {
        int R = 0, G = 0, B = 0;

        intensity = (intensity == 0) ? 1 : intensity;

        int x = (int) (100 * Math.log(intensity)) / 16;

        //color change from blue towads cyan
        if (0 <= x && x <= 3) { //intensity b/n 0 and 64
            R = 0;
            G = 64 * (x + 1) - 1;
            B = 255;
        }
        //color change from cyan towards greed
        if (5 <= x && x <= 8) { // intensity b/n 64 and 128
            R = 0;
            G = 255;
            B = 256 - (x - 4) * 64;
        }
        //color change from green towards yellow
        if (9 <= x && x <= 12) { //intensity b/n 128 and 192
            R = (x - 8) * 64 - 1;
            G = 255;
            B = 0;
        }
        //color change from yellow towards red
        if (13 <= x && x <= 16) { //intensity b/n 192 and 155
            R = 255;
            G = 256 - (x - 12) * 64;
            B = 0;
        }

        return (new Color(R, G, B));
    }

    @Override
    public void mouseClicked(MouseEvent me) {

        if (me.getClickCount() == 2) {
            scale = 1;
            drawRaster((int) maxMz, totalNumofSpectra, maxIntensity);
            revalidate();
            repaint();

        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
//        onDrawing = true;
//        p1 = me.getPoint();
//        p2 = p1;
//        repaint();

    }

    @Override
    public void mouseReleased(MouseEvent me) {

//        //redraw resized image
//        onDrawing = false;
//        zoomImage(p1, p2);
//        p2.x = 0;
//        p2.y = 0;
//        p1.x = 0;
//        p1.y = 0;
//        repaint();

        //erraze rectangle
    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {

    }

    @Override
    public void mouseDragged(MouseEvent me) {
//        if (onDrawing) {
//            p2 = me.getPoint();
//            p2.x = p2.x - p1.x;
//            p2.y = p2.y - p1.y;
//            repaint();
//
//        }

        //draw rectangle
    }

    @Override
    public void mouseMoved(MouseEvent me) {

        //draw rectangle
    }

}
