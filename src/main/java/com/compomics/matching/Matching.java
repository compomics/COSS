
package com.compomics.matching;

import com.compomics.coss.Model.ComparisonResult;
import com.compomics.ms2io.IndexKey;
import com.compomics.ms2io.SpectrumReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Genet
 */
public abstract class Matching {
    
    
    
   public abstract void InpArgs(java.lang.String ... args);

   public abstract List<ArrayList<ComparisonResult> > compare(org.apache.log4j.Logger log);
    
   public abstract void stopMatching();
    
    
}
