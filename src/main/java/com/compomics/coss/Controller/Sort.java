/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import java.util.ArrayList;
import com.compomics.coss.Model.ComparisonResult;
import java.util.List;

/**
 *
 * @author Genet
 */
public class Sort {
    

    public Sort() {
        

    }

    public void Quicksort(List<ArrayList<ComparisonResult>> resultList, int first, int last) {

        if (first < last) {

            int mid = partition(resultList, first, last);

            Quicksort(resultList, first, mid);

            Quicksort(resultList, mid + 1, last);

        }
    }

    private int partition(List<ArrayList<ComparisonResult>> list, int first, int last) {
        
        ArrayList<ComparisonResult> resultFirst=list.get(first);

        double x = resultFirst.get(0).getScore();

        int i = first - 1;

        int j = last + 1;

        while (true) {

            do {
                j--;

            } while (list.get(j).get(0).getScore() < x);

            do {

                i++;

            } while (list.get(i).get(0).getScore() > x);

            if (i < j) {

                ArrayList<ComparisonResult> temp=list.get(i);
                
                list.set(i, list.get(j));
                list.set(j, temp);    
                
                
                

            } else {
                return j;
            }
        }
    }

}
