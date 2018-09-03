/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.coss.Model.ComparisonResult;
import com.compomics.ms2io.Spectrum;
import java.util.List;

/**
 *
 * @author Genet
 */
public class Validation {

    public Validation() {

    }

    public int validate(List<ComparisonResult> result, double fdr_given) {

        int cutoff_index = 0;
        int numDecoy = 0;
        int numTarget = 0;
        double fdr_calculated=0;
        Spectrum spec;
        //int total_size=result.size();
        //double threshold = 0.01;

        for (ComparisonResult r : result) {
            // ArrayList<ComparisonResult> rDecoy=decoyResult.get(c++);
            spec=r.getEspSpectrum();
            if (spec.getTitle().endsWith("decoy")) {

                numDecoy++;

            } else {
                numTarget++;
            }
            
            fdr_calculated = (double)numDecoy/(numDecoy+numTarget);
            //fdr_calculated = (numDecoy+numTarget)*fdr_given;// (double) total_size;

            
            if (fdr_calculated >= fdr_given ) {
                break;
            }
           cutoff_index++;

        }
        
        return cutoff_index;

    }

}
