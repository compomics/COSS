package com.compomics.main;

import com.compomics.coss.controller.MainConsolControler;
import com.compomics.coss.controller.MainFrameController;
import java.awt.Dimension;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

/**
 * The main class to run the project
 *
 * @author Genet
 */
public class ProjectMain {

    /**
     * Logger instance.
     */
    private static Logger LOG;

    /**
     * The startup error message.
     */
    private static final String ERROR_MESSAGE = "An error occured during startup, please try again."
            + System.lineSeparator() + "If the problem persists, contact your administrator or post an issue on the google code page.";

    // private DrawRaster  visualize=new DrawRaster();
    /**
     * No-arg constructor.
     */
    public ProjectMain() {
    }

    /**
     * Main method.
     *
     * @param args the main method arguments
     */
    public static void main(final String[] args) {
        LOG = Logger.getLogger(ProjectMain.class.getName());
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
//            LOG.error(ex.getMessage(), ex);
//        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProjectMain initiateProject = new ProjectMain();
                initiateProject.launch(args);
            }
        });

    }

    /**
     * Launch the GUI.
     */
    private void launch(String[] args) {
        try {

            /**
             * The GUI main controller.
             */
            if (args.length == 0) {
                MainFrameController controller = new MainFrameController();
                controller.init();
                controller.showMainFrame();
            } else if (args.length != 0) {

                MainConsolControler controller = new MainConsolControler();
                controller.startRunning(args);
                

            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            //add message to JTextArea
            JTextArea textArea = new JTextArea(ERROR_MESSAGE + System.lineSeparator() + System.lineSeparator() + ex.getMessage());
            //put JTextArea in JScrollPane
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 200));
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JOptionPane.showMessageDialog(null, scrollPane, "COSS startup error", JOptionPane.ERROR_MESSAGE);

        }
    }

}
