/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.coss.Model.ComparisonResult;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Genet
 */
public class Validation {

    public Validation() {

    }

    public int validate(List<ArrayList<ComparisonResult>> result, double threshold) {

        int cutoff_index = 0;
        int numDecoy = 0;
        int numTarget = 0;
        double fdr = 0;
        //double threshold = 0.01;

        for (ArrayList<ComparisonResult> r : result) {
            // ArrayList<ComparisonResult> rDecoy=decoyResult.get(c++);
            if (!r.get(0).getTitle().endsWith("decoy")) {

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
