/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.decoyGeneration;

import com.compomics.coss.controller.UpdateListener;
import java.io.File;

/**
 *
 * @author Genet
 */
public class Generate {

    private org.apache.log4j.Logger log;
    private UpdateListener listener;

    public Generate(org.apache.log4j.Logger log, UpdateListener lstner) {
        this.log = log;
        this.listener = lstner;
    }

    public void start(File file, int type) {
        GenerateDecoyLib gen = null;
       
        switch (type) {
            case 0:
                gen = new FixedMzShift(file, listener, log);
                break;

            case 1:
                gen = new RandomIntensityFixedMz(file, listener,log);
                break;

            case 2:
                gen = new RandomMzIntShift(file, listener, log);
                break;
        }
        if (gen != null) {
            log.info("Generating decoy spectra");
            File decoyFile = gen.Generate();
            
            log.info("Appending decoy to library");
            MergeFiles m = new MergeFiles(file, decoyFile);
            m.Merge();
            decoyFile.delete();
            log.info("Decoy generation completed");
        }

    }

}
