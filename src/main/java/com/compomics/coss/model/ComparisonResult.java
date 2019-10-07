package com.compomics.coss.model;

import com.compomics.ms2io.model.Spectrum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Genet
 */
public class ComparisonResult implements Comparable<ComparisonResult>, Serializable {

    private double topScore;
    private Spectrum expSpec;
    List<MatchedLibSpectra> matchedSpectra;
    private double fdr=0;
 

    /**
     *
     * @return
     */
    public Spectrum getEspSpectrum() {
        return this.expSpec;
    }

    public void setExpSpectrum(Spectrum spec) {
        this.expSpec = spec;
    }
    
    public void setFDR(double f){
        this.fdr=f;
    }
    
    public double getFDR(){
        return this.fdr;
    }

    public void setMatchedLibSpec(List<MatchedLibSpectra> matchedSpectra) {
        this.matchedSpectra= matchedSpectra;
    }

    public List<MatchedLibSpectra> getMatchedLibSpec() {
        return this.matchedSpectra;
    }

    public void setTopScore(double d) {
        this.topScore = d;
    }

    public double getTopScore() {
        return this.topScore;
    }

   

    @Override
    public int compareTo(ComparisonResult t) {
        
         BigDecimal bd1=BigDecimal.valueOf(this.topScore);
        BigDecimal bd2=BigDecimal.valueOf(t.topScore);   
        
        int isEqual=bd1.compareTo(bd2);
        
        
        return isEqual;
    }

}
