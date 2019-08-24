package com.compomics.main;

import java.io.File;

/**
 *
 * @author Genet
 */
public class BatchExecution {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File dir = new File("C:/Users/Genet/OneDrive - UGent/0_pandi_piroCAT/batch/kuster1-1");
        String lib_file="C:/human_hcd_itraq/human_hcd_selected_TD(randMzShift).msp";
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String curr_file=child.toString();
                 if(curr_file.endsWith("mgf") || curr_file.endsWith("mgf")){
                     String[] args1 = new String[2];
                     args1[0]=curr_file;
                     args1[1]=lib_file;
                     ProjectMain.main(args1);
                     
                 }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
    }

}
