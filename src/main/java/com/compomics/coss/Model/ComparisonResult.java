package com.compomics.coss.Model;

import com.compomics.ms2io.Spectrum;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Genet
 */
public class ComparisonResult implements Comparable<ComparisonResult>, Serializable {

    private double topScore;
    private Spectrum expSpec;
    List<MatchedLibSpectra> matchedSpectra;
 

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

        return Double.compare(this.topScore, t.topScore);
    }

}
