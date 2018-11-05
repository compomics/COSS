package com.compomics.coss.controller;

import com.compomics.coss.view.MainGUI;
import javax.swing.SwingUtilities;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender class for writing log messages to a JTextArea.
 *
 * @author Niels Hulstaert
 */
public class LogTextAreaAppender extends WriterAppender {

    /**
     * The dialog to log to.
     */
    private MainGUI frm;

    public void setLogArea(MainGUI frame) {
        this.frm = frame;
        this.frm.txtlog.setText("");
    }

    @Override
    public void append(LoggingEvent event) {
        final String message = this.layout.format(event);

        SwingUtilities.invokeLater(() -> {
            frm.txtlog.append(message);
            frm.validate();
            frm.repaint();
        });
    }

}
