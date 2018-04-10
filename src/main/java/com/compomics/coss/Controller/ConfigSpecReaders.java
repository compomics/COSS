/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.ms2io.IndexKey;
import com.compomics.ms2io.Indexer;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.compomics.coss.Model.*;
import com.compomics.ms2io.MgfReader;
import com.compomics.ms2io.MspReader;
import com.compomics.ms2io.SpectraReader;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *This class generate index file for both experimental and library spectra and 
 * configures spectrum reader object to read them
 * @author Genet
 */
public class ConfigSpecReaders {

    private final File fileExperimnt;
    private final File fileLibrary;
    private boolean isCanceled;

    public ConfigSpecReaders(File expFile, File libFile) {
        this.fileExperimnt = expFile;
        this.fileLibrary = libFile;
        isCanceled=false;
    }
    
    public void cancelConfig(boolean cncl){
        isCanceled=cncl;
        
    }

    public boolean readFile(ConfigData cfData) {

        try {
            ExecutorService executors = Executors.newFixedThreadPool(2);
            Future[] fut = new Future[2];
            
            //Generate Index for experimental spectrum
            InputHandler ipHandler = new InputHandler(this.fileExperimnt, "spectrum");
            fut[0] = executors.submit(ipHandler);

            
            //generating index for library spectrum if it doesn't exist and read if it does.
           // String libname = this.fileLibrary.getName().substring(0, this.fileLibrary.getName().lastIndexOf("."));
           // File lib_indxfile = new File(this.fileLibrary.getParent(), libname + ".idx");
           // if(!lib_indxfile.exists()){
                ipHandler = new InputHandler(this.fileLibrary, "spectrum" );
                
//            }
//            
//            else{
//                ipHandler = new InputHandler(this.fileLibrary, "index" );
//                
//            }
            fut[1] = executors.submit(ipHandler);
            executors.shutdown();

            cfData.setExpSpectraIndex((List<IndexKey>) fut[0].get());
            cfData.setSpectralLibraryIndex((List<IndexKey>) fut[1].get());
            
            //sort library spectrum
            Collections.sort(cfData.getSpectraLibraryIndex());
            
            
            
            
            //reader for experimental spectrum file
            if (cfData.getExperimentalSpecFile().getName().endsWith("mgf")) {
                SpectraReader rd = new MgfReader(cfData.getExperimentalSpecFile(), cfData.getExpSpectraIndex());
                cfData.setExpSpecReader(rd);

            } else if (cfData.getExperimentalSpecFile().getName().endsWith("msp")) {
                SpectraReader rd = new MspReader(cfData.getExperimentalSpecFile(), cfData.getExpSpectraIndex());
                cfData.setExpSpecReader(rd);

            }

            //reader for spectral library file
            if (cfData.getSpecLibraryFile().getName().endsWith("mgf")) {
                SpectraReader rd = new MgfReader(cfData.getSpecLibraryFile(), cfData.getSpectraLibraryIndex());
                cfData.setLibSpecReader(rd);

            } else if (cfData.getSpecLibraryFile().getName().endsWith("msp")) {
                SpectraReader rd = new MspReader(cfData.getSpecLibraryFile(), cfData.getSpectraLibraryIndex());
                cfData.setLibSpecReader(rd);
            }
            
            
            
            
            

        } catch (InterruptedException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    private class InputHandler implements Callable<List<IndexKey>> {

        private final File file;
        private final String aboutFile;

        public InputHandler(File file, String aboutFile) {
            this.file = file;
            this.aboutFile=aboutFile;
        }

     

        @Override
        public List<IndexKey> call() throws Exception {
            List<IndexKey> indxList = null;

            try {
                if (this.aboutFile.equals("spectrum")) {
                    Indexer giExp = new Indexer(this.file);
                    indxList = giExp.generate();
                }
                else if(this.aboutFile.equals("index")){
                    Indexer indxer = new Indexer();
                    indxList = indxer.readFromFile(this.file);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return indxList;
        }

    }

}
