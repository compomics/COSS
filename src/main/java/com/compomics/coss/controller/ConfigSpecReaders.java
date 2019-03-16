/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller;

import com.compomics.coss.model.ConfigData;
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
import com.compomics.ms2io.MgfReader;
import com.compomics.ms2io.MspReader;
import com.compomics.ms2io.SpectraReader;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.dta_parser.DtaFile;

import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzml_wrapper.MzMlWrapper;

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
    private final String configFiles;

    /**
     * constructor for the class
     *
     * @param cData
     */
    public ConfigSpecReaders(ConfigData cData) {
        this.fileExperimnt = cData.getExperimentalSpecFile();
        this.fileLibrary = cData.getSpecLibraryFile();
        this.cfData = cData;
        isCanceled = false;
        configFiles = "both";// fileTobeConfig;
    }

    /**
     * cancel this job if event occure
     *
     * @param cncl
     */
    public void cancelConfig(boolean cncl) {
        isCanceled = cncl;
    }

    /**
     * start indexing and create reader object to read the spectra
     */
    public void startConfig() {
        if ((this.fileExperimnt.getName().endsWith("mgf") || this.fileExperimnt.getName().endsWith("msp")) && (this.fileLibrary.getName().endsWith("mgf") || this.fileLibrary.getName().endsWith("msp") || this.fileLibrary.getName().endsWith("sptxt"))) {

            cfData.setExpFileformat("ms2io");
            switch (configFiles) {
                case "both":
                    dispatcher();//read and configure both experimental and libray spectrum file
                    //get ready reader for experimental spectrum file
                    if (this.fileExperimnt.getName().endsWith("mgf")) {
                        SpectraReader rd = new MgfReader(this.fileExperimnt, cfData.getExpSpectraIndex());
                        cfData.setExpSpecReader(rd);

                    } else if (this.fileExperimnt.getName().endsWith("msp") || this.fileExperimnt.getName().endsWith("sptxt")) {
                        SpectraReader rd = new MspReader(this.fileExperimnt, cfData.getExpSpectraIndex());
                        cfData.setExpSpecReader(rd);

                    }
                    //get reader for spectral library file
                    if (this.fileLibrary.getName().endsWith("mgf")) {
                        SpectraReader rd = new MgfReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                        cfData.setLibSpecReader(rd);

                    } else if (this.fileLibrary.getName().endsWith("msp") || this.fileLibrary.getName().endsWith("sptxt")) {
                        SpectraReader rd = new MspReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                        cfData.setLibSpecReader(rd);
                    }
                    break;
                case "expSpec": //configure only expspectrum file
                    dispatcher(cfData.getExperimentalSpecFile());
                    //get ready reader for experimental spectrum file
                    if (this.fileExperimnt.getName().endsWith("mgf")) {
                        SpectraReader rd = new MgfReader(this.fileExperimnt, cfData.getExpSpectraIndex());
                        cfData.setExpSpecReader(rd);

                    } else if (this.fileExperimnt.getName().endsWith("msp") || this.fileExperimnt.getName().endsWith("sptxt")) {
                        SpectraReader rd = new MspReader(this.fileExperimnt, cfData.getExpSpectraIndex());
                        cfData.setExpSpecReader(rd);

                    }
                    break;
                case "libSpec": //read and configure only library spectrum because exp spectrum is already configured
                    dispatcher(cfData.getExperimentalSpecFile());
                    //get reader for spectral library file
                    if (this.fileLibrary.getName().endsWith("mgf")) {
                        SpectraReader rd = new MgfReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                        cfData.setLibSpecReader(rd);

                    } else if (this.fileLibrary.getName().endsWith("msp") || this.fileExperimnt.getName().endsWith("sptxt")) {
                        SpectraReader rd = new MspReader(this.fileLibrary, cfData.getSpectraLibraryIndex());
                        cfData.setLibSpecReader(rd);
                    }
                    break;

                default:
                    break;
            }
            if(cfData.getExpSpectraIndex()!=null){
                cfData.setExpSpecCount(cfData.getExpSpectraIndex().size());
            }

        } else {

            try {
                dispatcher(true);
            } catch (JMzReaderException | MzXMLParsingException ex) {
                Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * dispatch reader threads for both file readers: experimental and library
     *
     * @return true if successful.
     */
    private boolean dispatcher() {
        try {

            ExecutorService executors = Executors.newFixedThreadPool(2);
            Future<List<IndexKey>>[] fut = new Future[2];

            String fName = this.fileLibrary.getName().substring(0, this.fileLibrary.getName().indexOf("."));
            String path = this.fileLibrary.getParent();
            File tempFile = new File(path + "\\" + fName + ".idx");
            int indxFile = 0; // index file is not found and going to be created
            if (tempFile.exists()) {
                indxFile = 1;//index file already found and used for reading only
            }

            //Generate index list of spectrum library file
            GetIndexList indexHandler = new GetIndexList(this.fileLibrary, indxFile, "library");
            fut[0] = executors.submit(indexHandler);

            //Generate Index for experimental spectrum
            indexHandler = new GetIndexList(this.fileExperimnt, 0, "experiment");
            fut[1] = executors.submit(indexHandler);

            //wait untill the thread is finished and set the index values
            cfData.setSpectralLibraryIndex(fut[0].get());
            cfData.setExpSpectraIndex(fut[1].get());

            //sort library spectrum based on precursor mass - increasing order
            Collections.sort(cfData.getSpectraLibraryIndex());
            executors.shutdown();

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * dispatcher for initiating only one given file reader
     *
     * @param file: a file to be read
     * @return
     */
    private boolean dispatcher(File file) {
        try {

            ExecutorService executors = Executors.newSingleThreadExecutor();
            Future<List<IndexKey>>[] fut = new Future[1];

            if (file.equals(this.fileExperimnt)) {
                GetIndexList indexHandler = new GetIndexList(file, 0, "experiment");
                fut[0] = executors.submit(indexHandler);
                cfData.setExpSpectraIndex(fut[0].get());
            } else if (file.equals(this.fileLibrary)) {
                String fName = this.fileLibrary.getName().substring(0, this.fileLibrary.getName().indexOf("."));
                String path = this.fileLibrary.getParent();
                File tempFile = new File(path + "\\" + fName + ".idx");
                int indxFile = 0;
                if (tempFile.exists()) {
                    indxFile = 1;
                }

                GetIndexList indexHandler = new GetIndexList(file, indxFile, "library");
                fut[0] = executors.submit(indexHandler);
                cfData.setSpectralLibraryIndex(fut[0].get());
                //sort library spectrum
                Collections.sort(cfData.getSpectraLibraryIndex());
            }
            executors.shutdown();

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * initiate dispatcher thread to read file formats that use ebi spectra
     * reader library
     *
     * @param ebiFormats: true for ebi file formats
     * @return: return true if reader is success
     * @throws JMzReaderException
     * @throws MzXMLParsingException
     */
    private boolean dispatcher(boolean ebiFormats) throws JMzReaderException, MzXMLParsingException {
        try {
            if (ebiFormats) {
                String fileType = FilenameUtils.getExtension(this.fileExperimnt.getName());

                ExecutorService executors = Executors.newSingleThreadExecutor();
                Future<List<IndexKey>> fut;

                //Generate Index for library spectrum: Library spectrum file only
                //allowed in .msp and .mgf format
                GetIndexList ipHandler = new GetIndexList(this.fileLibrary, 0, "library");
                fut = executors.submit(ipHandler);

                //get Iterator for jmzml spectra reader for experimental spectra file
                JMzReader reader = null;
                switch (fileType) {
                    case "mzML":
                        reader = new MzMlWrapper(this.fileExperimnt);
                        reader.acceptsFile();
                        break;

                    case "ms2":
                        reader = new Ms2File(this.fileExperimnt);
                        reader.acceptsFile();
                        break;

                    case "mzXML":
                        reader = new MzXMLFile(this.fileExperimnt);
                        reader.acceptsFile();
                        break;

                    case "mzdata":
                        reader = new MzDataFile(this.fileExperimnt);
                        reader.acceptsFile();
                        break;

                    case "dta":
                        reader = new DtaFile(this.fileExperimnt);
                        reader.acceptsFile();
                        break;

                    case "pkl":
                        reader = new PklFile(this.fileExperimnt);
                        reader.acceptsFile();
                        break;
                }

                cfData.setExpFileformat("ebi");
                cfData.setExpSpecCount(reader.getSpectraCount());
                cfData.setEbiReader(reader);
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
            }

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * generate index and return as index list
     */
    private class GetIndexList implements Callable<List<IndexKey>> {

        private final File file;
        private final int indxFile;
        private final String thrName;

        public GetIndexList(File file, int indxFile, String threadName) {
            this.file = file;
            this.indxFile = indxFile;
            this.thrName = threadName;
        }

        @Override
        public List<IndexKey> call() throws Exception {
            List<IndexKey> indxList = null;

            try {

                if (this.thrName.equals("library")) {
                    if (this.indxFile == 0) {
                        Indexer giExp = new Indexer(this.file);
                        indxList = giExp.generate();
                        //giExp.saveIndex2File(this.file);
                    } else if (this.indxFile == 1) {
                        Indexer indxer = new Indexer();
                        indxList = indxer.readFromFile(this.file);
                    }
                } else if (this.thrName.equals("experiment")) {
                    Indexer giExp = new Indexer(this.file);
                    indxList = giExp.generate();

                }

            } catch (IOException e) {
                Logger.getLogger(ConfigSpecReaders.class.getName()).log(Level.SEVERE, null, e);
            }

            return indxList;
        }

    }

}
