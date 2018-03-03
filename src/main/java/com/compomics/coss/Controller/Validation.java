/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.coss.Model.ComparisonResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.avro.generic.GenericData;

/**
 *
 * @author Genet
 */
public class Validation {

    public Validation() {

    }

    public List<ArrayList<ComparisonResult>> compareNmerge(List<ArrayList<ComparisonResult>> targetResult, List<ArrayList<ComparisonResult>> decoyResult) {

        List<ArrayList<ComparisonResult>> validatedResult = new ArrayList<>();
        //calculate FDR=Ndecoy/Ntargethits

        int c = 0;
        for (ArrayList<ComparisonResult> rTarget : targetResult) {
            ArrayList<ComparisonResult> rDecoy = decoyResult.get(c++);
            if (rTarget.get(0).compareTo(rDecoy.get(0)) < 0) {

                validatedResult.add(rDecoy);
                
            }
            else{
                validatedResult.add(rTarget);
            }
            

        }
        Collections.sort(validatedResult, (ArrayList<ComparisonResult> o1, ArrayList<ComparisonResult> o2) -> Double.valueOf(o1.get(0).getScore()).compareTo(o2.get(0).getScore()));

        return validatedResult;
    }

    public int validate(List<ArrayList<ComparisonResult>> result, double threshold) {

        int cutoff_index = 0;
        int numDecoy = 0;
        int numTarget = 0;
        double fdr = 0;
        //double threshold = 0.01;

        for (ArrayList<ComparisonResult> r : result) {
            // ArrayList<ComparisonResult> rDecoy=decoyResult.get(c++);
            if (r.get(0).getResultType().equals("target")) {

                numTarget++;

            } else {
                numDecoy++;
            }

            fdr = numDecoy / (double) numTarget;

            if (fdr >= threshold) {
                break;
            }
            cutoff_index++;

        }
        if(cutoff_index==0)
            cutoff_index=result.size();
        return cutoff_index;

    }

}
