package com.compomics.main;

import com.compomics.coss.controller.decoyGeneration.MergeFiles;
import java.io.File;

/**
 *
 * @author Genet
 */
public class TempClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            File file_lib=new File("C:/human_hcd_itraq/human_hcd_selected_ExternalShuffleIntensities.msp");
            File file_decoy=new File("C:/human_hcd_itraq/human_hcd_selected.msp_decoy.msp");

            System.out.println("Appending decoy to library");
            MergeFiles m = new MergeFiles(file_lib, file_decoy);
            m.Merge();
            System.out.println("\nDecoy spectra appended to file " + file_lib.getName());
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }

}
