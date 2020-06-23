
package com.compomics.coss.controller;

import com.compomics.coss.model.ComparisonResult;
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
        
        for (ComparisonResult r : result) {
            
            if (r.getMatchedLibSpec().get(0).getSource() == 0) {

                numDecoy++;

            } else {
                numTarget++;
            }
           

            fdr_calculated = numDecoy / (double) (numTarget);
            fdr_calculated = (double)Math.round(fdr_calculated * 100000d) / 100000d;
            r.setFDR(fdr_calculated);
            
        }
         Collections.reverse(result);
         
          double fdrLocalMin = result.get(0).getFDR();
      
        for(ComparisonResult r: result){
            double fdrCurrent=r.getFDR();       
            if(fdrCurrent > fdrLocalMin){
                r.setFDR(fdrLocalMin);
            }else{
                fdrLocalMin=fdrCurrent;
            }
                   
        }  
        Collections.reverse(result);
 
       
        return null;
    }
    
   

}
