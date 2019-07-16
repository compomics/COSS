package com.compomics.coss.controller.decoyGeneration;

import com.compomics.coss.controller.UpdateListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class Generate {

    private final org.apache.log4j.Logger log;

    public Generate(org.apache.log4j.Logger lg, UpdateListener lstner) {
        this.log = lg;
        //this.listener = lstner;
    }

    public void start(File file, int type) {
        GenerateDecoyLib gen = null;
       
        switch (type) {
            case 0:
                gen = new FixedMzShift(file,  log);
                break;

            case 1:
                gen = new RandomIntensityFixedMz(file, log);
                break;

            case 2:
                gen = new RandomMzIntShift(file,  log);
                break;
        }
        if (gen != null) {
            try {
                log.info("Generating decoy spectra");
                File decoyFile = gen.generate();
                
                log.info("Appending decoy to library");
                MergeFiles m = new MergeFiles(file, decoyFile);
                m.Merge();
                decoyFile.delete();
                log.info("Decoy generation completed and appended to file " + file.getName());
            } catch (InterruptedException ex) {
                Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
