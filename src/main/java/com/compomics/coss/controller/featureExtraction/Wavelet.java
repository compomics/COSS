/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.featureExtraction;

/**
 * wavelet transform class
 *
 * @author Genet
 */
public class Wavelet {

    /**
     * The method transform and return wavelet transformed data
     *
     * @param data the data to be transformed
     * @return wavelet transform of the given data
     */
   
    public double[] getFeatures(double[] data) {

         int datalen = data.length;
         double[] features=new double[datalen];
         for(int i=0;i<datalen;i++){
             features[i]=data[i];
         }
         double[] temp = new double[datalen];
       
        int h = datalen >> 1; // reduce the size to the nearest half
        while (h > 0) {
            for (int i = 0; i < h; i++) {

                //true index of data
                int k = (i << 1);
                double d1=features[k];
                double d2=features[k+1];

                temp[i] = (d1 + d2) * 0.5; // pair wise average
                temp[i + h] = (d1 - d2) * 0.5;//neighbour distance 
            }
            
            int newlen=h<<1;
            for(int j=0;j<newlen;j++){
                features[j]=temp[j];
            }
            h = h >> 1;
        }

        return features;
    }

}
