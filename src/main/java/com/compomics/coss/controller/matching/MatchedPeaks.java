package com.compomics.coss.controller.matching;

import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Genet
 */
public class MatchedPeaks {
    
        // ar1[0..m-1] and ar2[0..n-1] are two given sorted
    // arrays/ and x is given number. This function prints
    // the pair from both arrays such that the sum of the
    // pair is closest to x.
    public int[] matchedPeaks(List<Double> ar1, List<Double> ar2, int m, int n, double fragTolerance) {
        // Initialize the diff between pair sum and x.
        double diff = fragTolerance;

        int[] d = new int[2];
        // res_l and res_r are result indexes from ar1[] and ar2[]
        // respectively
        int res_l = -1, res_r = -1;

        // Start from left side of ar1[] and right side of ar2[]
        int l = 0, r = n - 1;
        while (l < m && r >= 0) {
            // If this pair is closer to x than the previously
            // found closest, then update res_l, res_r and diff
            if (Math.abs(ar1.get(l) - ar2.get(r)) < diff) {
                res_l = l;
                res_r = r;
                diff = Math.abs(ar1.get(l) - ar2.get(r));
                
            }

            // If sum of this pair is more than x, move to smaller
            // side
            if (ar1.get(l) - ar2.get(r) > 0) {
                r--;
            } else // move to the greater side
            {
                l++;
            }
            
            if(diff==0){
                    break;
                }
        }

        d[0] = res_l;
        d[1] = res_r;
        return d;
    }
    
    public Map getMatchedPeaks(ArrayList<Peak> filteredExpMS2_1, ArrayList<Peak> filteredExpMS2_2, double fragTolerance){
        List<Peak> mPeaks_2 = new ArrayList<>(); //matched peaks from filteredExpMS2_2
        List<Peak> mPeaks_1 = new ArrayList<>(); //matched peaks from filteredExpMS2_1
        Map<String, List<Peak>> map=new TreeMap<>();

        for (int i = 0; i < filteredExpMS2_1.size(); i++) {
            Peak p1 = filteredExpMS2_1.get(i);
            double mz_p1 = p1.getMz();
            double diff = fragTolerance;// Based on Da.. not ppm...
            boolean found = false;
            Peak matchedPeak_2 = null;
            for (Peak peak_expMS2_2 : filteredExpMS2_2) {
                double tmp_mz_p2 = peak_expMS2_2.getMz(),
                        tmp_diff = (tmp_mz_p2 - mz_p1);
             
                if (Math.abs(tmp_diff) < diff) {
                    matchedPeak_2 = peak_expMS2_2;
                    diff = Math.abs(tmp_diff);
                    found = true;
                } else if (diff == tmp_diff) {
                    // so this peak is indeed in between of two peaks
                    // So, just the one on the left side is being chosen..
                }
            }
            if (found && !mPeaks_2.contains(matchedPeak_2)) {
                mPeaks_2.add(matchedPeak_2);
                mPeaks_1.add(p1);
            }

        }

      map.put("Matched Peaks1", mPeaks_1);
      map.put("Matched Peaks2", mPeaks_2);
      
      return map;
    }
}
