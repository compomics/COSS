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
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.dta_parser.DtaFile;

import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzml_wrapper.MzMlWrapper;
import uk.ac.ebi.pride.tools.jmzreader.model.*;
import uk.ac.ebi.pride.tools.ms2_parser.Ms2File;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.pkl_parser.PklFile;

/**
 * This class generate index file for both experimental and library spectra and
 * configures spectrum reader object to read them
 *
 * @author Genet
 */
public class ConfigSpecReaders {

    private final File fileExperimnt;
    private final File fileLibrary;
    private final ConfigData cfData;
    private boolean isCanceled;

    public ConfigSpecReaders(File expFile, File libFile, ConfigData cData) {
        this.fileExperimnt = expFile;
        this.fileLibrary = libFile;
        this.cfData = cData;
        isCanceled = false;
    }

    public void cancelConfig(boolean cncl) {
        isCanceled = cncl;
    }

    public void startConfig() {
        if (this.fileExperimnt.getName().endsWith("mgf") || this.fileExperimnt.getName().endsWith("msp")) {

            dispatcher();

        } else {

            try {
                dispatcher(true);
            } catch (JMzReaderException ex) {
                Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MzXMLParsingException ex) {
                Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private boolean dispatcher() {
        try {
            ExecutorService executors = Executors.newFixedThreadPool(2);
            Future<List<IndexKey>>[] fut = new Future[2];

            //Generate Index or get from the file if existed for library spectrum
            GetIndexList indexHandler = new GetIndexList(this.fileLibrary, "spectrum");
            fut[0] = executors.submit(indexHandler);

            //Generate Index for experimental spectrum
            indexHandler = new GetIndexList(this.fileExperimnt, "spectrum");
            fut[1] = executors.submit(indexHandler);

            //wait untill the thread is finished and set the index values
            cfData.setSpectralLibraryIndex(fut[0].get());
            cfData.setExpSpectraIndex(fut[1].get());
            executors.shutdown();

            //sort library spectrum
            Collections.sort(cfData.getSpectraLibraryIndex());

            //get ready reader for experimental spectrum file
            if (this.fileExperimnt.getName().endsWith("mgf")) {
                SpectraReader rd = new MgfReader(this.fileExperimnt, cfData.getExpSpectraIndex());
                cfData.setExpSpecReader(rd);

            } else if (this.fileExperimnt.getName().endsWith("msp")) {
                SpectraReader rd = new MspReader(this.fileExperimnt, cfData.getExpSpectraIndex());
                cfData.setExpSpecReader(rd);

            }

            //get reader for spectral library file
            if (this.fileLibrary.getName().endsWith("mgf")) {
                SpectraReader rd = new MgfReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                cfData.setLibSpecReader(rd);

            } else if (this.fileLibrary.getName().endsWith("msp")) {
                SpectraReader rd = new MspReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                cfData.setLibSpecReader(rd);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    private boolean dispatcher(boolean ebiFormats) throws JMzReaderException, MzXMLParsingException {
        try {

            String fileType = FilenameUtils.getExtension(this.fileExperimnt.getName());
            Iterator<Spectrum> spectrumIterator=null;

            ExecutorService executors = Executors.newSingleThreadExecutor();
            Future<List<IndexKey>> fut;

            //Generate Index for library spectrum
            GetIndexList ipHandler = new GetIndexList(this.fileLibrary, "spectrum");
            fut = executors.submit(ipHandler);

            //get Iterator for jmzml spectra reader for experimental spectra file
            JMzReader reader;
            switch (fileType) {
                case "mzml":
                    reader = new MzMlWrapper(new File("/path/to/55merge.mgf"));
                    reader.acceptsFile();
                    spectrumIterator = reader.getSpectrumIterator();
                    break;

                case "ms2":
                    reader = new Ms2File(this.fileExperimnt);
                    reader.acceptsFile();
                    spectrumIterator = reader.getSpectrumIterator();
                    break;
                case "mzxml":
                    reader = new MzXMLFile(new File("/path/to/55merge.mgf"));
                    reader.acceptsFile();
                    spectrumIterator = reader.getSpectrumIterator();
                    break;

                case "mzdata":
                    reader = new MzDataFile(this.fileExperimnt);
                    reader.acceptsFile();
                    spectrumIterator = reader.getSpectrumIterator();
                    break;

                case "dta":
                    reader = new DtaFile(new File("/path/to/55merge.mgf"));
                    reader.acceptsFile();
                    spectrumIterator = reader.getSpectrumIterator();
                    break;

                case "pkl":
                    reader = new PklFile(this.fileExperimnt);
                    reader.acceptsFile();
                    spectrumIterator = reader.getSpectrumIterator();
                    break;
            }

            cfData.setEbiSpecIterator(spectrumIterator);
            cfData.setSpectralLibraryIndex((List<IndexKey>) fut.get());
            //sort library spectrum
            Collections.sort(cfData.getSpectraLibraryIndex());

            //reader for spectral library file
            if (this.fileLibrary.getName().endsWith("mgf")) {
                SpectraReader rd = new MgfReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                cfData.setLibSpecReader(rd);

            } else if (this.fileLibrary.getName().endsWith("msp")) {
                SpectraReader rd = new MspReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                cfData.setLibSpecReader(rd);
            }
            
            

        } catch (InterruptedException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    private class GetIndexList implements Callable<List<IndexKey>> {

        private final File file;
        private final String aboutFile;

        public GetIndexList(File file, String aboutFile) {
            this.file = file;
            this.aboutFile = aboutFile;
        }

        @Override
        public List<IndexKey> call() throws Exception {
            List<IndexKey> indxList = null;

            try {
                if (this.aboutFile.equals("spectrum")) {
                    Indexer giExp = new Indexer(this.file);
                    indxList = giExp.generate();
                } else if (this.aboutFile.equals("index")) {
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
