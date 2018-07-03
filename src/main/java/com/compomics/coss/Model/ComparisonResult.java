package com.compomics.coss.Model;

/**
 *
 * @author Genet
 */
public class ComparisonResult implements Comparable<ComparisonResult> {

    private String title;
    private double precMass;
    private String scan_num;
    private String charge;
    private double score;
    private long spec_pos;
    private int matched_expe_Index;

    public long getSpecPosition() {
        return this.spec_pos;
    }

    public void setSpecPosition(long pos) {
        this.spec_pos = pos;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setPrecMass(double d) {
        this.precMass = d;
    }

    public void setScanNum(String s) {
        this.scan_num = s;
    }

    public void setCharge(String c) {
        this.charge = c;
    }

    public void setScore(double d) {
        this.score = d;
    }

    public String getTitle() {
        return this.title;
    }

    public double getPrecMass() {
        return this.precMass;
    }

    public String getScanNum() {
        return this.scan_num;
    }

    public String getCharge() {
        return this.charge;
    }

    public double getScore() {
        return this.score;
    }

    public int getMatchedExpIndex() {
        return this.matched_expe_Index;
    }

    public void setMatchedExpIndex(int expIndex) {
        this.matched_expe_Index = expIndex;
    }

    @Override
    public int compareTo(ComparisonResult t) {

        return Double.compare(this.score, t.score);
    }

}
