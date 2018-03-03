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
import org.apache.commons.math3.util.Precision;
import com.compomics.coss.Controller.UpdateListener;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.SpectraReader;
import com.compomics.coss.Model.ComparisonResult;

import com.compomics.ms2io.IndexKey;
import java.util.Collections;
/**
 * 
 *
 * @author Genet
 */
public class UseMsRoben extends Matching {

    //private final double msRobinScore = 0;
    private final double massWindow = 100;

    UpdateListener listener;

    SpectraReader rdExperiment;
    SpectraReader rdLibrary;
    List<IndexKey> expIndex;
    String resultType;
    
    int MsRobinOption;
    int IntensityOption;
    double fragTolerance;
    boolean cancelled = false;

    public UseMsRoben(UpdateListener lstner, SpectraReader rdExperiment, List<IndexKey> expIndex, SpectraReader rdLibrary, String resultType) {
        this.listener = lstner;
        this.rdExperiment=rdExperiment;
        this.rdLibrary=rdLibrary;
        this.resultType=resultType;
        this.expIndex=expIndex;
        cancelled = false;
    }

    @Override
    public void InpArgs(String... args) {

        this.MsRobinOption = Integer.parseInt(args[0]);
        this.IntensityOption = Integer.parseInt(args[1]);
        this.fragTolerance = Double.parseDouble(args[2]);
    }

    @Override
    public void stopMatching() {
        //executor.shutdownNow();
        cancelled = true;

    }

    @Override
    public List<ArrayList<ComparisonResult>> compare(org.apache.log4j.Logger log) {
       // specA = spa;, libIndex
                  
      
      
        int specNum = 1;
        List<ArrayList<ComparisonResult>> simResult = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future> futureList = new ArrayList<>();

        int taskCompleted = 0;
        double numTasks = expIndex.size();
        Spectrum expSpec;
        for(int a=0;a<numTasks;a++){// (Spectrum sp1 : specA) {

            if (cancelled) {
                executor.shutdownNow();
                return null;
            }
            
            expSpec=rdExperiment.readAt(expIndex.get(a).getPos());   
            double mass=expSpec.getPCMass();
            ArrayList libSpec = rdLibrary.readPart(mass, 0.05);
            DoMatching dm = new DoMatching(expSpec, libSpec, resultType);
            Future future = executor.submit(dm);
            futureList.add(future);
           

        }

        for (Future<ArrayList<ComparisonResult>> f : futureList) {
            try {

                if (cancelled) {
                    executor.shutdownNow();
                    return null;
                }

                simResult.add(f.get());
                taskCompleted++;
                this.listener.updateprogressbar((double) taskCompleted / numTasks);
                log.info("Matching Spectrum Number " + specNum + " Completed");
                specNum++;

            } catch (InterruptedException ex) {
                Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, "IterruptedExeption", ex + "Interrrupted Exception");
            } catch (ExecutionException ex) {
                Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, "Execution Exception", ex + " from useMsRobin");
            }
        }

        executor.shutdown();
        return simResult;
    }

    private class DoMatching implements Callable<ArrayList<ComparisonResult> > {

        Spectrum sp1;
        ArrayList<Spectrum> sb;        
        String resType="";
        
        public DoMatching(Spectrum sa, ArrayList<Spectrum> sb, String restype) {
            this.sp1 = sa;
            this.sb = sb;
            
           
        }

        
        
        double intensity_part = 0, probability_part = 0;

        @Override
        public ArrayList<ComparisonResult>  call() throws Exception {

            InnerIteratorSync<Spectrum> iteratorSpectra = new InnerIteratorSync(sb.iterator());

            //the first 6 values stores scores and the last 6 values corresponds to lib. spec. indexes at which the score calculated
           // double[][] topScores = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
           
            
            ArrayList<ComparisonResult> compResult=new ArrayList<>();

            
            while (iteratorSpectra.iter.hasNext()) {
                ComparisonResult res=new ComparisonResult();
                Spectrum sp2 = (Spectrum) iteratorSpectra.iter.next();
                //DoMatching dm = new DoMatching(sp1, sp2);

                //Computing all topN scores omited as the all 10 picks score more than the others - topN changed by 10 
                //for (int topN = 1; topN < 11; topN++) {
                TopNPeaks filterA = new DivideAndTopNPeaks(sp1, 10, massWindow);
                TopNPeaks filterB = new DivideAndTopNPeaks(sp2, 10, massWindow);
                double probability = (double) 10 / (double) massWindow;
                ArrayList<Peak> fP_spectrumA = filterA.getFilteredPeaks(),
                        fP_spectrumB = filterB.getFilteredPeaks();
                double[] results = new double[4];
                if (fP_spectrumB.size() < fP_spectrumA.size()) {
                    results = prepareData(fP_spectrumA, fP_spectrumB);
                } else {
                    results = prepareData(fP_spectrumB, fP_spectrumA);
                }
                int totalN = (int) results[0],
                        n = (int) results[1];
                double tmp_intensity_part = results[2];

                MSRobin object = new MSRobin(probability, totalN, n, tmp_intensity_part, MsRobinOption);
                double score = object.getScore();                
                Precision.round(score, 2);
                
                res.setCharge(sp2.getCharge());
                res.setPrecMass(sp2.getPCMass());
                res.setScanNum(sp2.getScanNumber());
                res.setScore(score);
                res.setTitle(sp2.getTitle());
                res.setSpecPosition(sp2.getIndex().getPos());
                res.setResultType(this.resType);
                
                compResult.add(res);

                intensity_part = object.getIntensity_part();
                probability_part = object.getProbability_part();
             
            }
            
            Collections.sort(compResult, Collections.reverseOrder());            
            //only top 5 scores returned 
            if(compResult.size()> 5)
                compResult.subList(5, compResult.size()).clear();

            return compResult;
        }

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
            HashSet<Peak> mPeaks_2 = new HashSet<Peak>(); //matched peaks from filteredExpMS2_2
            double intensities_1 = 0,
                    intensities_2 = 0,
                    explainedIntensities_1 = 0,
                    explainedIntensities_2 = 0;
            double alpha_alpha = 0,
                    beta_beta = 0,
                    alpha_beta = 0;
            boolean is_intensities2_ready = false;
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
                }
            }
            // double dot_score_intensities = calculateDot(filteredExpMS2_1, filteredExpMS2_2);
            int totalN = filteredExpMS2_1.size(),
                    n = mPeaks_2.size();
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
            results[2] = intensityPart;
            return results;
        }

        private double calculateIntensityPart(double explainedIntensities_1, double intensities_1, double explainedIntensities_2, double intensities_2, int intensityOption) {
            double int_part = 0;
            double tmp_part_1 = explainedIntensities_1 / intensities_1,
                    tmp_part_2 = explainedIntensities_2 / intensities_2;
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
