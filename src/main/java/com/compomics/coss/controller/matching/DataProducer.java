package com.compomics.coss.controller.matching;

import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import com.sun.jersey.api.Responses;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;

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

    public void cancel() {
        this.cancelled = true;
    }


    public boolean isReading() {
        return this.stillReading;
    }

    @Override
    public void run() {
        try {
            Spectrum expSpec = new Spectrum();
            uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec;
          

            if (confData.getExpSpectraIndex() == null && confData.getEbiReader() != null) {

                /**
                 * if comparison between Decoy database .... allow to take decoy
                 * db reader from configdata*
                 * *****************************************************************************
                 * ******************************************************************************
                 */
                double mz, intensity;
                ArrayList<Peak> peakList;
                Map map;
                Iterator entriesIterator;
                double da_error;
                double parentMass;
                int tempCount = 0;

                JMzReader redr = confData.getEbiReader();
                Iterator<uk.ac.ebi.pride.tools.jmzreader.model.Spectrum> ebiSpecIterator = redr.getSpectrumIterator();

                while (ebiSpecIterator.hasNext()) {

                    jmzSpec = ebiSpecIterator.next();
                    if (jmzSpec.getMsLevel() != 2) {
                        System.out.println("Only MS level 2 data is supported ");
                        break;
                    }

                    map = jmzSpec.getPeakList();
                    Set entries = map.entrySet();
                    entriesIterator = entries.iterator();
                    peakList = new ArrayList<>();

                    while (entriesIterator.hasNext()) {

                        Map.Entry mapping = (Map.Entry) entriesIterator.next();
                        mz = (double) mapping.getKey();
                        intensity = (double) mapping.getValue();
                        peakList.add(new Peak(mz, intensity));
                    }

                    if (jmzSpec.getPrecursorMZ() != 0) {
                        parentMass = jmzSpec.getPrecursorMZ();
                    } else {
                        parentMass = getPrecursorMass(jmzSpec);
                    }
                    if (peakList.isEmpty() || parentMass == 0) {
                        continue;
                    }

                    expSpec.setPCMass(parentMass);
                    expSpec.setPeakList(peakList);
                    expSpec.setNumPeaks(peakList.size());

                    da_error = parentMass * this.precTolerance;
                    ArrayList libSpec = confData.getLibSpecReader().readPart(expSpec.getPCMass(), da_error);
                    data.putExpSpec(expSpec);
                    data.putLibSpec(libSpec);
                    tempCount++;

                }

               
                System.out.print(Integer.toString(tempCount));

            } else {

                /**
                 * if comparison between Decoy database .... allow to take decoy
                 * db reader from configdata*
                 * *****************************************************************************
                 * ******************************************************************************
                 */
                int numTasks = confData.getExpSpectraIndex().size();

                ArrayList libSpec;
                for (int a = 0; a < numTasks; a++) {

                    expSpec = confData.getExpSpecReader().readAt(confData.getExpSpectraIndex().get(a).getPos());
                    double mass = expSpec.getPCMass();

                    double da_error = mass * this.precTolerance;
                  
                    libSpec = confData.getLibSpecReader().readPart(mass, da_error);

                    data.putExpSpec(expSpec);
                    data.putLibSpec(libSpec);
                    if (cancelled) {
                        break;
                    }

                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());

        } finally {
            this.stillReading = false;
        }

    }

    private double getPrecursorMass(uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec) {

        double precMass = 0;

        String fileType = FilenameUtils.getExtension(confData.getExperimentalSpecFile().getName());
        switch (fileType) {
            case "mzML":
                if (jmzSpec.getAdditional().getCvParams().isEmpty()) {
                    System.out.println("Additional CV parameters missing for the spectrum");
                    break;
                } else {
                    List<CvParam> params = jmzSpec.getAdditional().getCvParams();
                    for (CvParam p : params) {
                        if (p.getName().equals("base peak m/z")) {
                            precMass = Double.parseDouble(p.getValue());
                            break;
                        }
                    }
                }

            case "ms2":
                if (jmzSpec.getAdditional().getParams().isEmpty()) {
                    System.out.println("Additional parameters missing for the spectrum");
                    break;
                } else {
                    List<Param> temp = jmzSpec.getAdditional().getParams();
                    for (Param p : temp) {
                        if (p.getName().equals("BPM")) {
                            precMass = Double.parseDouble(p.getValue());
                            break;
                        }

                    }
                }

            case "mzXML":
                if (jmzSpec.getAdditional().getCvParams().isEmpty()) {
                    System.out.println("Additional CV parameters missing for the spectrum");
                    break;
                } else {
                    List<CvParam> params = jmzSpec.getAdditional().getCvParams();
                    for (CvParam p : params) {
                        if (p.getName().equals("base peak m/z")) {
                            precMass = Double.parseDouble(p.getValue());
                            break;
                        }
                    }
                }

            case "mzdata":
                precMass = 0;
                break;

            case "dta":
                precMass = 0;
                break;

            case "pkl":
                precMass = 0;
                break;
        }

        return precMass;
    }
}
