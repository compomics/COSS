package com.compomics.coss.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Genet
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("startRunning");

        //Example generateing decoy spectra and appending on the given library
        String libraryFile = "C:/Users/Genet/OneDrive - UGent/Related files/SpecB.msp";
        String[] ip = {"-dF", libraryFile};
        MainConsoleController instance = new MainConsoleController();
        instance.startRunning(ip);

        //batch processing 
//        String datasetsFolder = "C:/1_pandy_datasets/";
//        File path = new File(datasetsFolder);
//        String libraryFile = "D:/Human.msp";
//
//        List<File> datasets = new ArrayList<>();
//        File[] files = path.listFiles();
//
//        for (File f : files) {
//            if (f.isFile() && (f.getName().endsWith(".mgf") || f.getName().endsWith(".msp"))) { //this line weeds out other directories/folders
//                datasets.add(f);
//            }
//
//        }
//        int dataLen = datasets.size();
//        Thread[] threads = new Thread[dataLen];
//
//        int i = 0;
//        for (File f : datasets) {
//            String file = f.getName();
//            threads[i] = new Thread(new Runnable() {
//                public void run() {
//
//                    System.out.println("startRunning");
//                    String[] ip = {datasetsFolder + file, libraryFile};
//                    MainConsoleController instance = new MainConsoleController();
//                    instance.startRunning(ip);
//
//                }
//            });
//            threads[i].start();
//            i++;
//        }
    }

}
