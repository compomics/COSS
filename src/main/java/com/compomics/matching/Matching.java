package com.compomics.matching;

import com.compomics.coss.Model.ComparisonResult;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Genet
 */
public abstract class Matching {
    
    protected double matchedIntA;
    protected double matchedIntB;
    protected double totalIntA;
    protected double totalIntB;
    protected int matchedNumPeaks;

    public abstract void InpArgs(java.lang.String... args);

    public abstract List<ComparisonResult> dispatcher(org.apache.log4j.Logger log);

    public abstract void stopMatching();

    /**
     * This class creates and holds blocking queues for experimental spectrum
     * and selected library spectrum based on precursor mass. The comparison is
     * done between experimental spectrum against corresponding spectra at the
     * same location in their respective queue.
     *
     */
    protected class TheData {

        protected BlockingQueue<Spectrum> expSpec = null;
        protected BlockingQueue<ArrayList<Spectrum>> selectedLibSpec = null;

        protected TheData(ArrayBlockingQueue<Spectrum> expS, ArrayBlockingQueue<ArrayList<Spectrum>> libS) {

            this.expSpec = expS;
            this.selectedLibSpec = libS;

        }

        protected void putExpSpec(Spectrum s) throws InterruptedException {
            this.expSpec.put(s);
        }

        protected void putLibSpec(ArrayList<Spectrum> s) throws InterruptedException {
            this.selectedLibSpec.put(s);
        }

        protected Spectrum pollExpSpec() throws InterruptedException {
            return this.expSpec.poll(1, TimeUnit.SECONDS);
        }

        protected ArrayList<Spectrum> pollLibSpec() throws InterruptedException {
            return this.selectedLibSpec.poll(1, TimeUnit.SECONDS);
        }

    }

    protected class InnerIteratorSync<T> {

        protected Iterator<T> iter = null;

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

    protected double meanSqrError(List<Peak> v1, List<Peak> v2) {
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

        return mse;
    }

    protected void logTransform(Spectrum spec) {

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

}
