package com.compomics.matching;

import com.compomics.featureExtraction.DivideAndTopNPeaks;
import com.compomics.featureExtraction.TopNPeaks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.compomics.coss.Controller.UpdateListener;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import com.compomics.coss.Model.ComparisonResult;
import com.compomics.coss.Model.MatchedLibSpectra;
import java.util.Collections;
import com.compomics.coss.Model.ConfigData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;

/**
 *
 *
 * @author Genet
 */
public class UseMsRoben extends Matching {

    //private final double msRobinScore = 0;
    private final double massWindow = 100;
    private final ConfigData confData;

    boolean stillReading;
    UpdateListener listener;

    String resultType;

    int MsRobinOption;
    int IntensityOption;
    double fragTolerance;
    double precTlerance;
    boolean cancelled = false;
    int taskCompleted;

    public UseMsRoben(UpdateListener lstner, ConfigData cnfData, String resultType) {
        this.listener = lstner;
        this.resultType = resultType;
        cancelled = false;
        this.taskCompleted = 0;
        this.confData = cnfData;

    }

    @Override
    public void InpArgs(String... args) {

        this.MsRobinOption = Integer.parseInt(args[0]);
        this.IntensityOption = Integer.parseInt(args[1]);
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

            DoMatching match1 = new DoMatching(data, resultType, "First Matcher", log);
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

        String resType = "";
        TheData data;
        final String threadName;
        org.apache.log4j.Logger log;
        double matchedIntA;
        double matchedIntB;
        double totalIntA;
        double totalIntB;
        int matchedNumPeaks;
        double meanSqrError = 0;

        public DoMatching(TheData data, String restype, String matcherName, org.apache.log4j.Logger log) {
            this.data = data;
            this.resType = restype;
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

                            int sizeA = 0;
                            int sizeB = 0;
                            List<Double> scores=new ArrayList<>();
                            for(int topN = 1; topN < 11; topN++) {

                                // topN = 10;
                                TopNPeaks filterA = new DivideAndTopNPeaks(sp1, topN, massWindow);
                                TopNPeaks filterB = new DivideAndTopNPeaks(sp2, topN, massWindow);
                                double probability = (double) topN / (double) massWindow;
                                ArrayList<Peak> fP_spectrumA = filterA.getFilteredPeaks();
                                ArrayList<Peak> fP_spectrumB = filterB.getFilteredPeaks();
                                int lenA = fP_spectrumA.size();
                                int lenB = fP_spectrumB.size();
                                double score = 0;

                                sizeA = lenA;
                                sizeB = lenB;

                                double[] results;
                                if (lenB < lenA) {
                                    results = prepareData(fP_spectrumA, fP_spectrumB);
                                } else {
                                    results = prepareData(fP_spectrumB, fP_spectrumA);
                                }

                                fP_spectrumA.clear();
                                fP_spectrumB.clear();

                                int totalN = (int) results[0],
                                        n = (int) results[1];
                                double tmp_intensity_part = results[2];
                                MSRobin object = new MSRobin(probability, totalN, n, tmp_intensity_part, MsRobinOption);

                                score = object.getScore();//meanspqreerror;
                                scores.add(score);
//                            if(score!=0 && meanSqrError!=0){
//                                score/=meanSqrError;
//                            }

                                intensity_part = object.getIntensity_part();
                                 probability_part = object.getProbability_part();
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
                            mSpec.settotalFilteredNumPeaks_Exp(sizeA);
                            mSpec.settotalFilteredNumPeaks_Lib(sizeB);
                            specResult.add(mSpec);
                            // }
//                            else{
//                                if(!sp2.getTitle().contains("decoy") && sp1.getTitle().equals(sp2.getTitle())){
//                                    int z=1;
//                                    z++;
//                                }
//                            }

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
                        Collections.reverse(specResult);

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
            List<ComparisonResult> simResult = new ArrayList<>();
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

            return simResult;
        }

        private void logTransform(Spectrum spec) {

            List<Peak> pk = spec.getPeakList();
            ArrayList<Peak> newPeaks = new ArrayList<>();
            for (Peak p : pk) {
                Peak tempPeak;
                double mz = p.getMz();
                double intensity = p.getIntensity();
                mz = 10 * Math.log(mz);
                intensity = 10 * Math.log(intensity);
                tempPeak = new Peak(mz, intensity);
                newPeaks.add(tempPeak);
            }
            spec.setPeakList(newPeaks);

        }

        /**
         *
         * @return
         */
        public double getIntensity_part() {
            return intensity_part;
        }

        public void setIntensity_part(double intensity_part) {
            this.intensity_part = intensity_part;
        }

        public double getProbability_part() {
            return probability_part;
        }

        public void setProbability_part(double probability_part) {
            this.probability_part = probability_part;
        }

        private class InnerIteratorSync<T> {

            private Iterator<T> iter = null;

            public InnerIteratorSync(Iterator<T> aIterator) {
                iter = aIterator;
            }

            public synchronized T next() {
                T result = null;
                if (iter.hasNext()) {
                    result = iter.next();
                }
                return result;
            }
        }

        private double[] prepareData(ArrayList<Peak> filteredExpMS2_1, ArrayList<Peak> filteredExpMS2_2) {

            double[] results = new double[4];

            matchedIntA = 0;
            matchedIntB = 0;
            matchedNumPeaks = 0;

            HashSet<Peak> mPeaks_2 = new HashSet<Peak>(); //matched peaks from filteredExpMS2_2
            double intensities_1 = 0,
                    intensities_2 = 0,
                    explainedIntensities_1 = 0,
                    explainedIntensities_2 = 0;
            double alpha_alpha = 0,
                    beta_beta = 0,
                    alpha_beta = 0;
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
                    alpha_alpha += foundInt_1 * foundInt_1;
                    beta_beta += foundInt_2 * foundInt_2;
                    alpha_beta += foundInt_1 * foundInt_2;

                    explainedIntensities_1 += foundInt_1;
                    explainedIntensities_2 += foundInt_2;
                    double err = foundInt_1 - foundInt_2;
                    sumSqrError += err * err;

                }
                matchedIntA = explainedIntensities_1;
                matchedIntB = explainedIntensities_2;
                matchedNumPeaks = mPeaks_2.size();
            }
            // double dot_score_intensities = calculateDot(filteredExpMS2_1, filteredExpMS2_2);
            int totalN = filteredExpMS2_1.size(),
                    n = mPeaks_2.size();
            double mse = Double.MAX_VALUE;
            if (n != 0) {
                mse = sumSqrError / (double) n;
            }
            this.meanSqrError = mse;

            double intensityPart = 0;
            if (IntensityOption == 3) {
                //Making sure that not to have NaN due to zero!
                if (n != 0) {
                    intensityPart = calculateIntensityPart(alpha_alpha, beta_beta, alpha_beta);
//                System.out.println(n + "\t" + totalN + "\t" + intensityPart);
                }
            } else {
                intensityPart = calculateIntensityPart(explainedIntensities_1, intensities_1, explainedIntensities_2, intensities_2, IntensityOption);
            }
            results[0] = totalN;
            results[1] = n;

            results[2] = intensityPart;// / meanSqrError;
            return results;
        }

        private double[] prepareData2(ArrayList<Peak> libSpectrum, ArrayList<Peak> querySpectrum) {

            double[] results = new double[4];

            matchedIntA = 0;
            matchedIntB = 0;
            totalIntA = 0;
            totalIntB = 0;
            matchedNumPeaks = 0;

            double totalIntQuery = 0,
                    totalIntLib = 0,
                    explainedIntensities_Q = 0,
                    explainedIntensities_L = 0;
            double alpha_alpha = 0,
                    beta_beta = 0,
                    alpha_beta = 0;

            GetMatchedPeaks gmp = new GetMatchedPeaks();
            List<Double> mzQuery = new ArrayList<>();
            List<Double> mzLib = new ArrayList<>();
            List<Peak> matchedQueryPeaks = new ArrayList<>();
            List<Peak> matchedLibPeaks = new ArrayList<>();
            int lenQspec = querySpectrum.size();
            int lenLibspec = libSpectrum.size();
            for (int i = 0; i < lenQspec; i++) {
                mzQuery.add(querySpectrum.get(i).getMz());
                totalIntQuery += querySpectrum.get(i).getIntensity();
            }
            for (int i = 0; i < lenLibspec; i++) {
                mzLib.add(libSpectrum.get(i).getMz());
                totalIntLib += libSpectrum.get(i).getIntensity();
            }
            int m = lenLibspec;
            int n = lenQspec;
            int k = n;
            int c = 0;
            while (n >= 0) {
                n = k - c;
                int[] d = gmp.printClosest(mzLib, mzQuery, m, n, fragTolerance);
                int s = d[0];
                int t = d[1];

                if (s != -1 && t != -1) {
                    Peak val1 = libSpectrum.get(s);
                    Peak val2 = querySpectrum.get(t);

                    if (!matchedLibPeaks.isEmpty() && matchedLibPeaks.contains(val1)) {
                        int index = matchedLibPeaks.indexOf(val1);
                        double currentDiff = Math.abs(val2.getIntensity() - val1.getIntensity());
                        double prevDiff = Math.abs(matchedLibPeaks.get(index).getIntensity() - matchedLibPeaks.get(index).getIntensity());
                        if (currentDiff < prevDiff) {
                            matchedLibPeaks.set(index, val1);
                            matchedQueryPeaks.set(index, val2);
                        }

                    } else {
                        matchedLibPeaks.add(val1);
                        matchedQueryPeaks.add(val2);
                    }

                }
                c++;

            }
            int lenMatchedpeaks = matchedQueryPeaks.size();
            for (int i = 0; i < lenMatchedpeaks; i++) {
                double foundInt_1 = matchedQueryPeaks.get(i).getIntensity();
                double foundInt_2 = matchedLibPeaks.get(i).getIntensity();

                alpha_alpha += foundInt_1 * foundInt_1;
                beta_beta += foundInt_2 * foundInt_2;
                alpha_beta += foundInt_1 * foundInt_2;

                explainedIntensities_Q += foundInt_1;
                explainedIntensities_L += foundInt_2;
            }

            int totalN = querySpectrum.size();

            matchedIntA = explainedIntensities_Q;
            matchedIntB = explainedIntensities_L;
            totalIntA = totalIntQuery;
            totalIntB = totalIntLib;
            matchedNumPeaks = lenMatchedpeaks;

            double intensityPart = 0;
            if (IntensityOption == 3) {
                //Making sure that not to have NaN due to zero!
                if (lenMatchedpeaks != 0) {
                    intensityPart = calculateIntensityPart(alpha_alpha, beta_beta, alpha_beta);
//                System.out.println(n + "\t" + totalN + "\t" + intensityPart);
                }
            } else {
                intensityPart = calculateIntensityPart(explainedIntensities_Q, totalIntQuery, explainedIntensities_Q, totalIntLib, IntensityOption);
            }

            double mse = meanSqrError(matchedQueryPeaks, matchedLibPeaks);
            this.meanSqrError = mse;
            results[0] = totalN;
            results[1] = lenMatchedpeaks;
            results[2] = intensityPart;
//            if (sum_sqr_error != 0 && results[2] != 0) {
//                results[2] /= sum_sqr_error;
//            }
            return results;
        }

        private double meanSqrError(List<Peak> v1, List<Peak> v2) {
            int len = v1.size();
            double sumSqrErr = 0;
            for (int i = 0; i < len; i++) {
                double d1 = v1.get(i).getIntensity();
                double d2 = v2.get(i).getIntensity();
                double diff = d1 - d2;
                sumSqrErr += (diff * diff);

            }
            double mse = Double.MAX_VALUE;
            if (len != 0) {
                mse = sumSqrErr / (double) len;
            }
            this.meanSqrError = mse;
            return mse; //return mean square error
        }

        private double calculateIntensityPart(double explainedIntensities_1, double intensities_1, double explainedIntensities_2, double intensities_2, int intensityOption) {
            double int_part = 0;
            double tmp_part_1 = explainedIntensities_1 / intensities_1,
                    tmp_part_2 = explainedIntensities_2 / intensities_2;
            if (intensities_1 == 0 || intensities_2 == 0) {
                return 0;
            }

            switch (intensityOption) {
                case 0:
                    int_part = (0.5 * tmp_part_1) + (0.5 * tmp_part_2);
                    break;
                case 1:
                    int_part = tmp_part_1 * tmp_part_2;
                    break;
                case 2:
                    int_part = Math.pow(10, (1 - (tmp_part_1 * tmp_part_2)));
                    break;
                default:
                    break;
            }
            return int_part;
        }

        private double calculateIntensityPart(double alpha_alpha, double beta_beta, double alpha_beta) {
            double intensityPart = alpha_beta / (Math.sqrt(alpha_alpha * beta_beta));
            return intensityPart;
        }

    }

}
