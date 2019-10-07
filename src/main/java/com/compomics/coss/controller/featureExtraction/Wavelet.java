//package com.compomics.coss.controller.featureExtraction;
//
//import com.compomics.ms2io.Peak;
//import com.compomics.ms2io.Spectrum;
//import java.util.ArrayList;
//import org.apache.log4j.Logger;
//
///**
// * wavelet transform class
// *
// * @author Genet
// */
//public class Wavelet implements Features {
//
//    private final Spectrum expSpectrum;
//
//    public Wavelet(Spectrum expSpec) {
//        this.expSpectrum = expSpec;     
//
//    }
//
//    @Override
//    public ArrayList<Peak> getFeatures() {
//        
//        ArrayList<Peak> features = this.expSpectrum.getPeakList();
//        int datalen = features.size();
//        ArrayList<Peak> temp = new ArrayList<>();
//
//        int h = datalen >> 1; // reduce the size to the nearest half
//        while (h > 0) {
//            for (int i = 0; i < h; i++) {
//
//                //true index of data
//                         
//                int k = (i << 1);
//                double d1 = features.get(k).getIntensity();
//                double d2 = features.get(k + 1).getIntensity();
//                double intensity1=(d1 + d2) * 0.5;// pair wise average
//                double intensity2=(d1 - d2) * 0.5;// neighbour distance 
//                d1 = features.get(k).getMz();
//                d2 = features.get(k + 1).getMz();
//                double mz1=(d1 + d2) * 0.5;// pair wise average
//                double mz2=(d1 - d2) * 0.5;// neighbour distance
//                
//                Peak pk1=new Peak(mz1, intensity1, "");
//                Peak pk2=new Peak(mz2, intensity2, "");
//                temp.set(i, pk1);//pair wise average
//                temp.set(i+h, pk2);//neighbour distance                 
//                           
//            }
//
//            int newlen = h << 1;
//            for (int j = 0; j < newlen; j++) {
//                features.set(j, temp.get(j));
//            }
//            h = h >> 1;
//        }
//
//        return features;
//    }
//
//}
