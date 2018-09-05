/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.coss.controller.UpdateListener;
import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigData;
import com.compomics.coss.model.MatchedLibSpectra;
import com.compomics.coss.controller.featureExtraction.DivideAndTopNPeaks;
import com.compomics.coss.controller.featureExtraction.TopNPeaks;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;

/**
 *
 * @author Genet
 */
public class MeanSquareError extends Matching {

    //private final double msRobinScore = 0;
    private final double massWindow = 100;
    private final ConfigData confData;

    boolean stillReading;
    UpdateListener listener;    
    double fragTolerance;
    double precTlerance;
    boolean cancelled = false;
    int taskCompleted;

    public MeanSquareError(UpdateListener lstner, ConfigData cnfData) {
        this.listener = lstner;
        cancelled = false;
        this.taskCompleted = 0;
        this.confData = cnfData;

    }

     public MeanSquareError(ConfigData cnfData) {
        this.listener=null;
        cancelled = false;
        this.taskCompleted = 0;
        this.confData = cnfData;

    }
    @Override
    public void InpArgs(String... args) {
        this.fragTolerance = Double.parseDouble(args[2]);
        this.fragTolerance /= 1000.0;
        this.precTlerance = Double.parseDouble(args[3]);

    }

    @Override
    public void stopMatching() {

        cancelled = true;

    }

    @Override
    public List<ComparisonResult> dispatcher(org.apache.log4j.Logger log) {

        List<ComparisonResult> simResult = new ArrayList<>();
        try {

            this.stillReading = true;

            ArrayBlockingQueue<Spectrum> expspec = new ArrayBlockingQueue<>(20, true);
            ArrayBlockingQueue<ArrayList<Spectrum>> libSelected = new ArrayBlockingQueue<>(20, true);
            TheData data = new TheData(expspec, libSelected);

            DoMatching match1 = new DoMatching(data, "First Matcher", log);
            //DoMatching match2 = new DoMatching(data, resultType, "Second Matcher", log);
            DataProducer producer1 = new DataProducer(data);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            //executor.execute(match2);
            Future future1 = executor.submit(producer1);
            Future<List<ComparisonResult>> future = executor.submit(match1);

            future1.get();
            simResult = future.get();
            executor.shutdown();

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
        }

        Collections.sort(simResult);
        return simResult;
    }

    /**
     * This class creates and holds blocking queues for experimental spectrum
     * and selected library spectrum based on precursor mass. The comparison is
     * done between experimental spectrum against corresponding spectra at the
     * same location in their respective queue.
     *
     */
    private class TheData {

        private BlockingQueue<Spectrum> expSpec = null;
        private BlockingQueue<ArrayList<Spectrum>> selectedLibSpec = null;

        public TheData(ArrayBlockingQueue<Spectrum> expS, ArrayBlockingQueue<ArrayList<Spectrum>> libS) {

            this.expSpec = expS;
            this.selectedLibSpec = libS;

        }

        private void putExpSpec(Spectrum s) throws InterruptedException {
            this.expSpec.put(s);
        }

        private void putLibSpec(ArrayList<Spectrum> s) throws InterruptedException {
            this.selectedLibSpec.put(s);
        }

        private Spectrum pollExpSpec() throws InterruptedException {
            return this.expSpec.poll(1, TimeUnit.SECONDS);
        }

        private ArrayList<Spectrum> pollLibSpec() throws InterruptedException {
            return this.selectedLibSpec.poll(1, TimeUnit.SECONDS);
        }

    }

    /**
     * this class puts spectra that are going to be compared into queue as it is
     * blocking Queue it blocks until there is free space.
     */
    private class DataProducer implements Runnable { //procucer thread

        TheData data;

        public DataProducer(TheData data) {
            this.data = data;

        }

        @Override
        public void run() {
            try {
                Spectrum expSpec = new Spectrum();
                uk.ac.ebi.pride.tools.jmzreader.model.Spectrum jmzSpec;
                double massErrorFraction = precTlerance / 1000000.0;

                if (confData.getExpSpectraIndex() == null && confData.getEbiReader() != null) {

                    /**
                     * if comparison between Decoy database .... allow to take
                     * decoy db reader from configdata*
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

                        da_error = parentMass * massErrorFraction;
                        ArrayList libSpec = confData.getLibSpecReader().readPart(expSpec.getPCMass(), da_error);
                        data.putExpSpec(expSpec);
                        data.putLibSpec(libSpec);
                        tempCount++;

                    }

                    String sss = "this string break point" + Integer.toString(tempCount);
                    System.out.print(Integer.toString(tempCount));

                } else {

                    /**
                     * if comparison between Decoy database .... allow to take
                     * decoy db reader from configdata*
                     * *****************************************************************************
                     * ******************************************************************************
                     */
                    int numTasks = confData.getExpSpectraIndex().size();
                    for (int a = 0; a < numTasks; a++) {

                        expSpec = confData.getExpSpecReader().readAt(confData.getExpSpectraIndex().get(a).getPos());
                        double mass = expSpec.getPCMass();

                        double da_error = mass * massErrorFraction;// (10 * mass) / 1000000.0;
                        ArrayList libSpec = confData.getLibSpecReader().readPart(mass, da_error);

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
                stillReading = false;
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

    private class DoMatching implements Callable<List<ComparisonResult>> {

        TheData data;
        final String threadName;
        org.apache.log4j.Logger log;

        public DoMatching(TheData data, String matcherName, org.apache.log4j.Logger log) {
            this.data = data;
            this.threadName = matcherName;
            this.log = log;

        }

        double intensity_part = 0, probability_part = 0;

        @Override
        public List<ComparisonResult> call() {

            ObjectOutputStream oos = null;
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("temp.txt");
                oos = new ObjectOutputStream(fos);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
            }

//            double maxScore=0;
            confData.setMaxScore(0);
            while (stillReading || (!data.expSpec.isEmpty() && !data.selectedLibSpec.isEmpty())) {
                try {

                    if (data.expSpec.isEmpty() || data.selectedLibSpec.isEmpty()) {
                        continue;
                    }

                    Spectrum sp1 = data.pollExpSpec();
                    ArrayList sb = data.pollLibSpec();
                    InnerIteratorSync<Spectrum> iteratorSpectra = new InnerIteratorSync(sb.iterator());
                    List<MatchedLibSpectra> specResult = new ArrayList<>();

                    while (iteratorSpectra.iter.hasNext()) {

                        try {
                            Spectrum sp2 = (Spectrum) iteratorSpectra.iter.next();
//                            if (confData.applyTransform() && confData.getTransformType() == 0) {
//                                logTransform(sp1);
//                                logTransform(sp2);
//                            }

                            List<Double> scores = new ArrayList<>();
                            int lenA = 0;
                            int lenB = 0;

                            for (int topN = 1; topN < 11; topN++) {

                                // topN = 10;
                                TopNPeaks filterA = new DivideAndTopNPeaks(sp1, topN, massWindow);
                                TopNPeaks filterB = new DivideAndTopNPeaks(sp2, topN, massWindow);

                                ArrayList<Peak> fP_spectrumA = filterA.getFilteredPeaks();
                                ArrayList<Peak> fP_spectrumB = filterB.getFilteredPeaks();
                                lenA = fP_spectrumA.size();
                                lenB = fP_spectrumB.size();
                                double score;
                                if (lenB < lenA) {
                                    score = calculateScore(fP_spectrumA, fP_spectrumB, false);
                                } else {
                                    score = calculateScore(fP_spectrumB, fP_spectrumA, true);
                                }
                             

                                fP_spectrumA.clear();
                                fP_spectrumB.clear();
                                scores.add(score);

                            }
                            // if (score > 0) {
                            MatchedLibSpectra mSpec = new MatchedLibSpectra();
                            mSpec.setScore(Collections.max(scores));
                            mSpec.setSequence(sp2.getSequence());
                            if (sp2.getTitle().contains("decoy")) {
                                mSpec.setSource("decoy");
                            } else {
                                mSpec.setSource("target");
                            }
                            mSpec.setNumMathcedPeaks(matchedNumPeaks);
                            mSpec.setSpectrum(sp2);
                            mSpec.setSumFilteredIntensity_Exp(totalIntA);
                            mSpec.setSumFilteredIntensity_Lib(totalIntB);
                            mSpec.setSumMatchedInt_Exp(matchedIntA);
                            mSpec.setSumMatchedInt_Lib(matchedIntB);
                            mSpec.settotalFilteredNumPeaks_Exp(lenA);
                            mSpec.settotalFilteredNumPeaks_Lib(lenB);
                            specResult.add(mSpec);

                        } catch (Exception ex) {

                            Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, "\n Description: " + ex);
                        }

                    }

                    taskCompleted++;
                    listener.updateprogressbar(taskCompleted);
                    //log.info(Integer.toString(taskCompleted));

                    if (!specResult.isEmpty()) {
                        ComparisonResult compResult = new ComparisonResult();
                        compResult.setExpSpectrum(sp1);
                        Collections.sort(specResult);

                        //only top 10 scores returned if exists
                        int len = specResult.size();

                        List<MatchedLibSpectra> tempMatch = new ArrayList<>();
                        int tempLen = 0;
                        int c = 0;
                        while (tempLen < len && tempLen < 10) {
                            tempMatch.add(specResult.get(c));
                            tempLen = tempMatch.size();
                            c++;
                        }
                        if (!tempMatch.isEmpty()) {
                            compResult.setMatchedLibSpec(tempMatch);
                            compResult.setTopScore(tempMatch.get(0).getScore());

                            //simResult.add(compResult);  
                            oos.writeObject(compResult);
                            oos.flush();
                        }
                    }

                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (cancelled) {
                    break;
                }
            }

            List<ComparisonResult> simResult = new ArrayList<>();

            if (!cancelled) {
                listener.updateprogressbar(confData.getExpSpectraIndex().size());
                try {
                    if (oos != null) {
                        oos.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    log.info("Search completed succesfully.");
                } catch (IOException ex) {
                    Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
                }

                log.info(
                        "Getting results.");

                FileInputStream fis = null;
                ObjectInputStream ois = null;

                try {
                    fis = new FileInputStream("temp.txt");
                    ois = new ObjectInputStream(fis);
                    ComparisonResult r = (ComparisonResult) ois.readObject();
                    while (r != null) {
                        r = (ComparisonResult) ois.readObject();
                        simResult.add(r);
                    }
                } catch (FileNotFoundException | ClassNotFoundException ex) {
                    Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    // 
                } finally {
                    try {
                        if (ois != null) {
                            ois.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }

                        File file = new File("temp.txt");
                        if (file.exists()) {
                            file.delete();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                File fis = new File("temp.txt");
                if (fis.exists()) {
                    fis.delete();

                }
            }
            return simResult;
        }

        private double calculateScore(ArrayList<Peak> filteredExpMS2_1, ArrayList<Peak> filteredExpMS2_2, boolean isReversed) {

            HashSet<Peak> mPeaks_2 = new HashSet<Peak>(); //matched peaks from filteredExpMS2_2
            double intensities_1 = 0;
            double intensities_2 = 0;
            double explainedIntensities_1 = 0;
            double explainedIntensities_2 = 0;
            boolean is_intensities2_ready = false;
            double sumSqrError = 0;
            for (int i = 0; i < filteredExpMS2_1.size(); i++) {
                Peak p1 = filteredExpMS2_1.get(i);
                double mz_p1 = p1.getMz(),
                        intensity_p1 = p1.getIntensity(),
                        diff = fragTolerance,// Based on Da.. not ppm...
                        foundInt_1 = 0,
                        foundInt_2 = 0;
                intensities_1 += intensity_p1;
                Peak matchedPeak_2 = null;
                for (Peak peak_expMS2_2 : filteredExpMS2_2) {
                    double tmp_mz_p2 = peak_expMS2_2.getMz(),
                            tmp_diff = (tmp_mz_p2 - mz_p1),
                            tmp_intensity_p2 = peak_expMS2_2.getIntensity();
                    if (!is_intensities2_ready) {
                        intensities_2 += tmp_intensity_p2;
                    }
                    if (Math.abs(tmp_diff) < diff) {
                        matchedPeak_2 = peak_expMS2_2;
                        diff = Math.abs(tmp_diff);
                        foundInt_1 = intensity_p1;
                        foundInt_2 = tmp_intensity_p2;
                    } else if (tmp_diff == diff) {
                        // so this peak is indeed in between of two peaks
                        // So, just the one on the left side is being chosen..
                    }
                }
                is_intensities2_ready = true;
                if (foundInt_1 != 0 && !mPeaks_2.contains(matchedPeak_2)) {
                    mPeaks_2.add(matchedPeak_2);

                    explainedIntensities_1 += foundInt_1;
                    explainedIntensities_2 += foundInt_2;
                    double err = foundInt_1 - foundInt_2;
                    sumSqrError += err * err;
                }

            }

             if (!isReversed) {
                totalIntA = intensities_1;
                totalIntB = intensities_2;
                matchedIntA = explainedIntensities_1;
                matchedIntB = explainedIntensities_2;

            } else {
                totalIntA = intensities_2;
                totalIntB = intensities_1;
                matchedIntA = explainedIntensities_2;
                matchedIntB = explainedIntensities_1;
            }
            matchedNumPeaks = mPeaks_2.size();
            int n = mPeaks_2.size();
            double mse = Double.MAX_VALUE;
            if (n != 0) {
                mse = sumSqrError / (double) n;
            }
            return mse;
        }
    }

}
