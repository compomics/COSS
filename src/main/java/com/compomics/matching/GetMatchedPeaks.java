/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.matching;

import java.util.List;

/**
 *
 * @author Genet
 */
public class GetMatchedPeaks {
    
        // ar1[0..m-1] and ar2[0..n-1] are two given sorted
    // arrays/ and x is given number. This function prints
    // the pair from both arrays such that the sum of the
    // pair is closest to x.
    int[] printClosest(List<Double> ar1, List<Double> ar2, int m, int n, double fragTolerance) {
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
}
