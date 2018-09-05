/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing;

/**
 *
 * @author Genet
 */
public class HighPass extends Filter {
    
   
    

    @Override
    public double[] filter(double[] originalIntensities) {
       
        int dataSize=originalIntensities.length;
         double[] filteredIntensity=new double[dataSize];
         double pi=Math.PI;
         int windowSize=10;
         double[] window = getWindow(windowSize);
         double sum;
         for(int i=0;i<dataSize;i++){
             sum=0;
             for(int j=0;j<windowSize && (i+j)<dataSize;j++){
                 sum += originalIntensities[i+j] * window[j];
             }
              
             filteredIntensity[i]=sum;
         }
         
         return filteredIntensity;
    }
    
      private int correlation(double[] data, double[] window, int len1, int len2) {
            int xCoorIndx = 0;
            double min = 0;
            double sum;
            double m = 0;

            for (int i = 0; i < len1; i++) {
                sum = 0;
                for (int j = 0; j < len2 && j + i < len1; j++) {
                    m = data[i + j] * window[j];
                    sum += m;
                }
                
            }
            return xCoorIndx;
        }
    
    private double[] getWindow(int size){
        
        double[] window=new double[size];
        double pi = Math.PI;
        
        //Generating hamming window for high pass filter
        for(int i=0;i<size;i++){
            
             window[i]=Math.sin(2*pi*0.25*(i+1))/(pi*(i+1));
        }
       return window;
        
    }
    
}
