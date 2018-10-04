package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ComparisonResult;
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
    
    

    public abstract void InpArgs(java.lang.String... args);

    public abstract List<ComparisonResult> dispatcher(org.apache.log4j.Logger log);

    public abstract void stopMatching();

    

    

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

   
}
