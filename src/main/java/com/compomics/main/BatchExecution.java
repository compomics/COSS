package com.compomics.main;

import com.compomics.coss.controller.MainFrameController;
import com.compomics.coss.controller.SpectrumAnnotation.Annotation;
import com.compomics.coss.controller.decoyGeneration.GenerateDecoy;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Genet
 */
public class BatchExecution {

    /**
     * @param args the command line arguments
     */
    private static final Logger LOG = Logger.getLogger(MainFrameController.class);

    public static void main(String[] args) {
        File dir = new File("C:/Users/Genet/OneDrive - UGent/1_pandy_datasets/batch"); //"C:/human_hcd/batch");
        String lib_file="C:/human_hcd/MassIVE_realNsynthetic_Annotated_TargetDecoy.msp";
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
//            
            for (File child : directoryListing) {
                 String curr_file=child.toString();

//                try {
//
////                    Annotation annot = new Annotation(child, 0.5);
////                    annot.annotateSpecFile(false);
//
////                    GenerateDecoy gen = new ReverseSequence(child, LOG);
////                    gen.generate();
//
//                    //} catch (Exception ex) {//InterruptedException ex) {
//                    //    System.out.println(ex.toString());
//                    // }
//                } catch (Exception ex) {
//                    java.util.logging.Logger.getLogger(BatchExecution.class.getName()).log(Level.SEVERE, null, ex);
//                }

                if(curr_file.endsWith("mgf") || curr_file.endsWith("mgf")){
                     String[] args1 = new String[3];
                     args1[0]= curr_file;
                     args1[1]= lib_file;
                     args1[2] = "0";
                     ProjectMain.main(args1);
                     
                 }
            }
        }
        //else {
//            // Handle the case where dir is not really a directory.
//            // Checking dir.isDirectory() above would not be sufficient
//            // to avoid race conditions with another process that deletes
//            // directories.
//        }
    }

}
