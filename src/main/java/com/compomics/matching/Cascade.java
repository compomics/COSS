/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.matching;

import com.compomics.coss.Controller.UpdateListener;
import com.compomics.coss.Model.ComparisonResult;
import com.compomics.featureExtraction.Peaks;
import com.compomics.featureExtraction.Features;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import com.compomics.preprocessing.Filter;
//import com.compomics.preprocessing.HighPass;
import com.compomics.featureExtraction.Wavelet;
import com.compomics.ms2io.IndexKey;
import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.SpectrumReader;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Genet
 */
public class Cascade extends Matching {

    UpdateListener listener;

    boolean cancelled = false;

    public Cascade(UpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void InpArgs(String... args) {

    }

    @Override
    public void stopMatching() {
        cancelled = true;
    }

    @Override
    public List<ArrayList<ComparisonResult>> compare(SpectrumReader rdExperiment, List<IndexKey> expIndex, SpectrumReader rdLibrary, List<IndexKey> libIndex,org.apache.log4j.Logger log) {

        int specNum = 1;
        List<ArrayList<ComparisonResult>> simResult = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future> futureList = new ArrayList<>();

        int specPos = 1;
        int taskCompleted = 0;
        double lenA = expIndex.size();
        //double lenB = specb.size();

//        for (Spectrum sp1 : speca) {
//
//            if (cancelled) {
//                executor.shutdownNow();
//                return null;
//            }
////            Cascade.DoMatching dm = new Cascade.DoMatching(specPos, sp1, specb);
//            Future future = executor.submit(dm);
//            futureList.add(future);
//            specPos++;
//
//        }

        for (Future<ArrayList<ComparisonResult>> f : futureList) {
            try {

                if (cancelled) {
                    executor.shutdownNow();
                    return null;
                }

                simResult.add(f.get());
                taskCompleted++;
                this.listener.updateprogressbar((double) taskCompleted / lenA);
                log.info("Matching Spectrum Number " + specNum + " Completed");
                specNum++;

            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, "IterruptedExeption", ex + "Interrrupted Exception");
            } catch (ExecutionException ex) {
                java.util.logging.Logger.getLogger(UseMsRoben.class.getName()).log(Level.SEVERE, "Execution Exception", ex + " from useMsRobin");
            }
        }

        executor.shutdown();
        return simResult;
    }

    private class DoMatching implements Callable<int[]> {

        public DoMatching() {
           // this.sp1 = sa;
            //this.sb = sb;
           // this.specPos = specpos;
        }

        //MSnSpectrum sp1;
       // ArrayList<MSnSpectrum> sb;
        int specPos;
        double intensity_part = 0, probability_part = 0;
        Features flt = new Wavelet();

        @Override
        public int[] call() throws Exception {

//            InnerIteratorSync<MSnSpectrum> iteratorSpectra = new InnerIteratorSync(sb.iterator());
            //the first 6 values stores scores and the last 6 values corresponds to lib. spec. indexes at which the score calculated
            int max = Integer.MAX_VALUE;
            int[] topScores = {max, max, max, max, max, max, max, max, max, max, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            int pos = 0;

            double[] mzVal2;
           
            double[] feature2;            

          //  double[] mzVal1 = sp1.getMzValuesAsArray();
            double[] peaks1=new double[10];
            int[] peaks2;
           // Peaks.findPeaks(mzVal1, peaks1, 10);
            //Arrays.sort(mzVal1);
            
           //double[] peaks1 = PickPeaks(mzVal1).stream().mapToDouble(d -> d).toArray();// sp1.getIntensityValuesAsArray();
            double[] feature1 = flt.getFeatures(peaks1);

            //while (iteratorSpectra.iter.hasNext()) {

               // MSnSpectrum sp2 = (MSnSpectrum) iteratorSpectra.iter.next();

              
               // topNpeaks2=new DivideAndTopNPeaks(sp2, 10, 10);

                //mzVal2 = sp2.getMzValuesAsArray();                
                //Arrays.sort(mzVal2);
                //peaks2 = PickPeaks(mzVal2).stream().mapToDouble(d -> d).toArray();//sp2.getIntensityValuesAsArray();
                //double[] testD=new{6, 12, 15, 15, 14, 12, 120, 116};
                //feature1=flt.getFeatures(testD);
               // feature2 = flt.getFeatures(mzVal2);

                // int coorIndx = 0;// xCorrelation(mzVal1, mzVal2, len1, len2);
                
                //double distScore =eclDistance(feature1, feature2, 10);

//                for (int i = 0; i < 10; i++) {
//                    if (distScore < topScores[i]) {
//                        for (int j = 9; j > i; j--) {
//                            topScores[j] = topScores[j - 1];
//                            topScores[j + 10] = topScores[j + 9];
//                        }
//                        topScores[i] = (int) distScore;
//                        topScores[i + 10] = pos;
//                        break;
//                    }
//
//                }
                pos++;

           // }

            return topScores;
        }

        private List<Double> PickPeaks(double[] values) {

            List<Double> peaks = new ArrayList<>();
            int len = values.length;

            for (int i = 0; i < len - 2; i++) {
                if ((values[i + 1] - values[i]) * (values[i + 2] - values[i + 1]) <= 0) { // changed sign?
                    peaks.add(values[i+1]);
                }
            }

//            List<Double> peaks = new ArrayList<>();
//            double previous = 0;
//            double previousSlope = 0;
//            int len=values.length;
//
//            for (int i=0;i<len; i++) {
//                if (previous == 0) {
//                    previous = values[i];
//                    continue;
//                }
//                double slope = values[i]- previous;
//                if (slope * previousSlope < 0) { //look for sign changes
//                    peaks.add(previous);
//                }
//                previousSlope = slope;
//                previous = values[i];
//            }
            return peaks;
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

        private int xCorrelation(double[] mz1, double[] mz2, int len1, int len2) {
            int xCoorIndx = 0;
            double min = 0;
            double sum;
            double diff = 0;

            if (len1 >= len2) {

                for (int i = 0; i < len1; i++) {
                    sum = 0;
                    for (int j = 0; j < len2 && j + i < len1; j++) {
                        diff = mz1[i + j] - mz2[j];

                        sum += (diff * diff);
                    }
                    if (min > sum) {
                        min = sum;
                        xCoorIndx = i;
                    }
                }
            } else {
                for (int i = 0; i < len2; i++) {
                    sum = 0;
                    for (int j = 0; j < len1 && j + i < len2; j++) {
                        diff = mz2[i + j] - mz1[j];

                        sum += (diff * diff);
                    }
                    if (min > sum) {
                        min = sum;
                        xCoorIndx = i;
                    }
                }
            }

            return xCoorIndx;
        }

        private double eclDistance(double[] feature1, double[] feature2, int len) {
            double dist = 0;

            double diff = 0;

            for (int i = 0; i < len; i++) {

                diff = feature1[i] - feature2[i];

                dist += diff * diff;
            }
            dist = Math.sqrt(dist);
            return dist;
        }

//        private int simulatedannealing(double startTemp, double coolingRate) {
//
//            double state=0;
//           // Solution x = createRandomSolution();
//            double t= startTemp;
//
//            while(state>0) {
//                double f = calculatedist(targ, libcurrent);
//                double newf=calculatedist(targ, libnext);
//                
//                
//                
//                if (newFf< f) {
//                    double p = PR(); // no idea what you're talking about here
//                    if (p > UR(0, 1)) { // likewise
//                        // then do nothing
//                    } else {
//                        x = mutatedX;
//                    }
//                    ti = t * coolingRate;
//                }
//            }
//            return x;
//            return 0;
//        }
    }

}
