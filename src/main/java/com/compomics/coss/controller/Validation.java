
package com.compomics.coss.controller;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.ms2io.Spectrum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Genet This class validates the comparison result based on the fdr
 * value provided
 */
public class Validation {

    public Validation() {

    }

    public List<ComparisonResult>  validate(List<ComparisonResult> result, double fdr_given) {

        int numDecoy = 0;
        int numTarget = 0;
        double fdr_calculated = 0;
       // Spectrum spec;

        List<ComparisonResult>  validatedResult=new ArrayList<>();
        
        for (ComparisonResult r : result) {
            
            int isDecoy=0;
            if (r.getMatchedLibSpec().get(0).getSource() == 1) {

                numDecoy++;
                isDecoy=1;

            } else {
                numTarget++;
                isDecoy=0;
            }

            fdr_calculated = numDecoy / (double) (numTarget);
            r.setFDR(fdr_calculated);
            if(isDecoy==0 && fdr_calculated < fdr_given){
                validatedResult.add(r);
            }
            
        }
//        if(numDecoy==0){
//            validatedResult=result;
//        }
            
       
        return validatedResult;
    }
    
    private int testvalidated(List<ComparisonResult> result, double fdr_given){
         int cutoff_index = 0;
        int numDecoy = 0;
        int numTarget = 0;
        double fdr_calculated = 0;
        Spectrum spec;
        int len = result.size();

        List<ComparisonResult>  validatedResult=new ArrayList<>();
        for (ComparisonResult r : result) {
            // ArrayList<ComparisonResult> rDecoy=decoyResult.get(c++);
            spec = r.getMatchedLibSpec().get(0).getSpectrum();
            if (spec.getTitle().contains("decoy")) {

                numDecoy++;

            } else {
                numTarget++;
            }

            fdr_calculated = numDecoy / (double) (numTarget);
            r.setFDR(fdr_calculated);
            
        }
        if(numDecoy==0){
            return len-1;
        }
        
        Collections.reverse(result);
        int localMinIndex=localMinima(result, 0, len-1, len);
        double fdrLocalMin=result.get(localMinIndex).getFDR();
        Collections.reverse(result);
      
        for(ComparisonResult r: result){
            double fdrCurrent=r.getFDR();       
            if(fdrCurrent > fdrLocalMin){
                return cutoff_index;
            }
            cutoff_index++;            
        }  
        
        return cutoff_index;
    }
    
    // search and return index of the local minima
    //based on binary searching algorithm
    public int localMinima(List<ComparisonResult> resList, int low, int high, int n) 
    {           
        int mid = low + (high - low) / 2;        
        if(mid == 0 || resList.get(mid - 1).getFDR() > resList.get(mid).getFDR() && mid == n - 1 ||  
           resList.get(mid).getFDR() < resList.get(mid+1).getFDR()) 
                return mid; 
          
        else if(mid > 0 && resList.get(mid-1).getFDR()< resList.get(mid).getFDR()) 
                return localMinima(resList, low, mid - 1, n); 
          
        return localMinima(resList, mid + 1, high, n); 
    } 

}
