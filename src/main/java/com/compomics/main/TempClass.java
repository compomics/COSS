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
            File file_lib=new File("C:/1_pandi_part/Adult_Spinalcord_Gel_Elite_67_f01_piroCAT.mgf");
            File file_decoy=new File("C:/human_hcd_itraq/Velos005137_Pirococus.mgf");

            System.out.println("Appending decoy to library");
            MergeFiles m = new MergeFiles(file_lib, file_decoy);
            m.Merge();
            System.out.println("\nDecoy spectra appended to file " + file_lib.getName());
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }

}
