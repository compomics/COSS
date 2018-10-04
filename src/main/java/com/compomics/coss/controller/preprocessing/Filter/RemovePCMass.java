package com.compomics.coss.controller.preprocessing.Filter;

import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.util.ArrayList;
/**
 *
 * @author Genet
 */
public abstract class RemovePCMass implements IFilter{
    

    @Override
    public Spectrum removePrecursor(Spectrum spec, double fragmentTolerance){

//        ArrayList<Double> precursorPeaksMZ = new ArrayList<>();
//        ArrayList<Peak> peaksToRemove = new ArrayList<>();
//        ArrayList<Peak> peaks = spec.getPeakList();
        // get a precursor charge 
//        double precursorMZ = spec.getPCMass();
//        int charge = ms.getPrecursor().getPossibleCharges().get(ms.getPrecursor().getPossibleCharges().size() - 1).value;
//        precursorPeaksMZ.add(precursorMZ);
//        
//          // first select peaks may derive from a precursor
//        double precursorMass = ms.getPrecursor().getMass(charge),
//                protonTheoMass = ElementaryIon.proton.getTheoreticMass();
//        if (charge >= 1) {
//            while (charge >= 1) {
//                double tmpMZ = (precursorMass + (protonTheoMass * charge)) / charge;
//                precursorPeaksMZ.add(tmpMZ);
//                charge--;
//            }
//        }
//        // Now check actual peaks to get them
//        
//       
//        int startIndex = 0;
//        for (Double possiblePrecursorMZ : precursorPeaksMZ) {
//            Peak removedPeak = null;
//            boolean found_a_close_peak = false;
//            // to find a closest peak
//            double tmpFragmentTolerance = fragmentTolerance;
//            for (int i = startIndex; i < peaks.size(); i++) {
//                Peak tmpPeak = peaks.get(i);
//                double diffMZ = Math.abs(tmpPeak.getMz() - possiblePrecursorMZ);
//                if (diffMZ <= tmpFragmentTolerance) {
//                    tmpFragmentTolerance = diffMZ;
//                    removedPeak = ms.getPeakMap().get(tmpPeak.getMz());
//                    if (i > startIndex && !found_a_close_peak) {
//                        startIndex = i;
//                        found_a_close_peak = true;
//                    }
//                }
//            }
//            if (removedPeak != null) {
//                peaksToRemove.add(removedPeak);
//            }
//        }
//        // now clear peak list from possibly derived from precursor peaks
//        peaks.removeAll(peaksToRemove);
//        ms.getPeakList().clear();
//       // ms.setMzOrdered(false);
//        ms.setPeaks(peaks);

          return spec;
    }
    
}
