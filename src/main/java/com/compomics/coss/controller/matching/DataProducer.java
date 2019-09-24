package com.compomics.coss.controller.matching;

import com.compomics.coss.controller.MappingJmzSpectrum;
import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.model.Spectrum;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;

/**
 * this class puts spectra that are going to be compared into queue as it is
 * blocking Queue it blocks until there is free space.
 *
 * @author Genet
 */
public class DataProducer implements Runnable { //procucer thread

    private final TheDataUnderComparison data;
    private final ConfigData confData;
    private final double precTolerance;
    private boolean stillReading;
    private boolean cancelled;

    public DataProducer(TheDataUnderComparison data, ConfigData confData) {

        this.data = data;
        this.confData = confData;
        this.precTolerance = confData.getPrecTol();
        stillReading = true;
        cancelled = false;

    }
    public boolean isCancelled(){
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isReading() {
        return this.stillReading;
    }

    @Override
    public void run() {
        int numTasks = confData.getExpSpecCount();
        int currPos = 0;
        try {
            Spectrum expSpec;
            ArrayList libSpec;

            //if query spectrum is mzML, ms2, mzXML, dta and pkl
            if (confData.getExpFileformat().equals("ebi")) {

                JMzReader redr = confData.getEbiReader();
                String fileType = FilenameUtils.getExtension(confData.getExperimentalSpecFile().getName());
                MappingJmzSpectrum jmzMap = new MappingJmzSpectrum(fileType);
                uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec;
                Iterator<uk.ac.ebi.pride.tools.jmzreader.model.Spectrum> ebiSpecIterator = redr.getSpectrumIterator();

                int downCount = 0;
                while (ebiSpecIterator.hasNext()) {//for(int i=1; i < numTasks ; i++){ //

                    jmzSpec = ebiSpecIterator.next();//  redr.getSpectrumByIndex(i);// 
                    if (jmzSpec.getMsLevel() != 2) {
                        downCount++;
                        continue;
                    }
                    //creating spectrum that can be read by the matcher

                    expSpec = jmzMap.getMappedSpectrum(jmzSpec);

                    double parentMass = expSpec.getPCMass();
                    double da_error = parentMass * this.precTolerance;
                    libSpec = confData.getLibSpecReader().readPart(expSpec.getPCMass(), da_error);
                    data.putExpSpec(expSpec);
                    data.putLibSpec(libSpec);

                    if (cancelled) {
                        break;
                    }

                    currPos++;

                }
                if (downCount != 0) {
                    System.out.println("query file has non MS2 spectra, number of MS2 spectra found " + Integer.toString(downCount));
                }

            } else if (confData.getExpFileformat().equals("ms2io")) {
                for (int a = 0; a < numTasks; a++) {
                   
                    expSpec = confData.getExpSpecReader().readAt(confData.getExpSpectraIndex().get(a).getPos());
                    double mass = expSpec.getPCMass();

                    double da_error = mass * this.precTolerance;

                    libSpec = confData.getLibSpecReader().readPart(mass, da_error);
                   // synchronized (data) {
                        data.putExpSpec(expSpec);
                        data.putLibSpec(libSpec);
                   // }

                    if (cancelled) {
                        break;
                    }

                }
            }
        } catch (Exception e) {
//            this.stillReading = false;
//            this.cancelled = true;
            System.out.println(e.toString() + "position error " + Integer.toString(currPos));

        } finally {
            this.stillReading = false;
        }

    }

}
